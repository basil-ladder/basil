package org.bytekeeper.ctr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.Race
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

const val killTimeout = 20L
const val LOG_LIMIT = 200 * 1024

@Service
class Scbw(private val botRepository: BotRepository,
           private val sscaitClient: SscaitClient,
           private val config: ScbwConfig,
           private val events: Events,
           private val commands: Commands,
           private val maps: Maps) {
    private val log = LogManager.getLogger()

    fun setupOrUpdateBot(sscaitBot: BotInfo) {
        val name = sscaitBot.name

        log.info("Checking $name for updates...")
        val bot = botRepository.findByName(name)

        if (bot?.lastUpdated?.isBefore(sscaitBot.lastUpdated()) == false) {
            log.info("Bot $name is already up-to-date.")
            return
        }

        if (bot == null) {
            log.info("Bot $name not yet registered, creating it.")
            commands.handle(CreateBot(sscaitBot.name, parseRace(sscaitBot.race), sscaitBot.botType, sscaitBot.lastUpdated()))
        }

        if (sscaitBot.isDisabled || sscaitBot.isDisabledForBasil) {
            log.info("Bot is disabled on SSCAIT: ${sscaitBot.isDisabled}, disabled for BASIL: ${sscaitBot.isDisabledForBasil}")
            val disabledBot = bot ?: botRepository.findByName(name)!!
            events.post(BotDisabled(disabledBot))
            throw BotDisabledException("${disabledBot.name} is disabled")
        }

        val botDir = config.botsDir.resolve(name)
        val aiDir = botDir.resolve("AI")
        val readDir = botDir.resolve("read")
        val writeDir = botDir.resolve("write")
        val additionalReadPath = botDir.resolve("additionalRead")
        val botJsonDef = botDir.resolve("bot.json")

        log.info("Bot $name was updated ${sscaitBot.lastUpdated()}${if (bot?.lastUpdated != null) ", local version is from " + bot.lastUpdated else ""}, deleting AI dir")
        deleteDirectory(aiDir)

        if (sscaitBot.clearReadDirectory) {
            log.info("Clearing READ of bot as requested.")
            deleteDirectory(readDir)
        }

        log.info("Setting up SCBW.play directory structure for $name")
        Files.createDirectories(botDir)
        Files.createDirectories(aiDir)
        Files.createDirectories(readDir)
        Files.createDirectories(writeDir)
        Files.createDirectories(botDir.resolve("write"))

        log.info("Downloading $name's BWAPI.dll")
        val bwApiDll = sscaitClient.downloadBwapiDLL(sscaitBot)
                .block(Duration.ofSeconds(10))
                ?: throw FailedToDownloadBwApi("Could not download BWAPI.dll for bot $name")

        Files.copy(bwApiDll.inputStream, botDir.resolve("BWAPI.dll"), StandardCopyOption.REPLACE_EXISTING)
        log.info("Downloading $name's binary")
        val botBinary = sscaitClient.downloadBinary(sscaitBot)
                .block(Duration.ofSeconds(30))
                ?: throw FailedToDownloadBot("Could not download bot binary for bot $name")
        ZipInputStream(botBinary.inputStream)
                .use { zipIn ->
                    while (true) {
                        val nextEntry = zipIn.nextEntry ?: return@use
                        if (nextEntry.isDirectory) {
                            Files.createDirectories(aiDir.resolve(nextEntry.name))
                        } else {
                            Files.copy(zipIn, aiDir.resolve(nextEntry.name), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
        jacksonObjectMapper().writeValue(botJsonDef.toFile(), BotJson(name, sscaitBot.race, sscaitBot.botType))
        if (additionalReadPath.toFile().exists() && Files.isDirectory(additionalReadPath)) {
            log.info("There are additional 'read' files that will be copied to the read directory")
            Files.list(additionalReadPath)
                    .forEach {
                        Files.copy(it, readDir.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
                    }
        }
        log.info("Successfully setup $name")
    }

    private fun parseRace(race: String): Race? =
            when (race) {
                "Terran" -> Race.TERRAN
                "Zerg" -> Race.ZERG
                "Protoss" -> Race.PROTOSS
                "Random" -> Race.RANDOM
                else -> null
            }

    fun runGame(gameConfig: GameConfig) {
        ScbwGameRunner(gameConfig, config).run()
    }

    data class GameConfig(val bots: List<String>, val map: String, val gameName: String)

    private inner class ScbwGameRunner(val gameConfig: GameConfig,
                                       val config: ScbwConfig) {
        private val log = LogManager.getLogger()
        private val dockerPrefix = "GAME_${gameConfig.gameName}"
        private val bots = gameConfig.bots
        private val gameName = gameConfig.gameName

        fun run() {
            val botsParticipating = "${bots[0]} vs ${bots[1]}"
            log.info("Upcoming: $gameName: $botsParticipating")
            val cmd = mutableListOf("scbw.play", "--headless")

            fun addParameter(par: String, vararg value: Any?) {
                val notNull = value.filterNotNull()
                if (notNull.isEmpty()) return
                cmd += par
                cmd += value.map { it.toString() }.joinToString(" ")
            }

            cmd += "--bots"
            cmd += bots
            addParameter("--timeout", config.realtimeTimeoutSeconds)
            addParameter("--bot_dir", config.botsDir)
            addParameter("--map", gameConfig.map)
            addParameter("--game_dir", config.gamesDir)
            addParameter("--game_speed", config.gameSpeed)
            addParameter("--game_name", gameName)
            addParameter("--docker_image", config.dockerImage)
            if (config.readOverWrite == true) cmd += "--read_overwrite"
            val process = ProcessBuilder(cmd)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start()
            try {
                updateResourceConstraints()
                val hardTimeLimit = config.realtimeTimeoutSeconds + 30L
                val exited = process.waitFor(hardTimeLimit, TimeUnit.SECONDS)
                if (!exited) {
                    log.error("Timeout - killing game $gameName, $botsParticipating")
                    return
                }
                log.info("Successfully completed game $botsParticipating")

            } finally {
                if (process.isAlive) {
                    process.destroy()
                    val killed = process.waitFor(killTimeout, TimeUnit.SECONDS)
                    if (!killed) {
                        throw FailedToKillSCBW("Could not kill scbw.play after $killTimeout seconds")
                    }
                }
                cleanUpBotContainers()
                // Wait for a short while to make sure scores and results are written
                Thread.sleep(5000)
                // Try to move results
                moveResult(config.gamesDir, gameName, config.targetDir, bots)
                if (config.deleteGamesInGameDir) {
                    deleteDirectory(config.gamesDir.resolve("GAME_$gameName"))
                } else {
                    log.warn("Deleting game dirs is switched off, the ${config.gamesDir.toFile().absolutePath} might run full.")
                }
            }
        }

        private fun cleanUpBotContainers() {
            val started = System.currentTimeMillis()
            var remaining = 2
            while (System.currentTimeMillis() - started < 10000 && remaining > 0) {
                Docker.retrieveContainersWithName(dockerPrefix)
                        .forEach {
                            log.info("Killing game ${gameConfig.gameName}, bot container $it")
                            val killed = Docker.killContainer(it)
                                    .waitFor(killTimeout, TimeUnit.SECONDS)
                            if (!killed) {
                                throw FailedToKillContainer("Couldn't kill container $it after $killTimeout seconds")
                            }
                            remaining--
                        }
                Thread.sleep(1000)
            }
        }

        private fun moveResult(gameDir: Path, gameName: String, targetPath: Path, bots: List<String>) {
            val mapper = jacksonObjectMapper()
            val gamePath = gameDir.resolve("GAME_$gameName")
            val botsPath = targetPath.resolve("bots")
            val scores = bots.mapIndexed { index, name ->
                val botPath = botsPath.resolve(name)
                Files.createDirectories(botPath)
                val replayPath = gamePath.resolve("player_$index.rep")
                if (replayPath.toFile().exists()) {
                    val enemies = bots.filterNot { it == name }.joinToString(" ")
                    val mapName = maps.mapName(gameConfig.map)
                    Files.move(replayPath, botPath.resolve("$name vs $enemies $mapName $gameName.rep"))
                }

                // Save crashlogs
                val targetLogsPath = botPath.resolve("logs")
                Files.createDirectories(targetLogsPath)
                val crashLogSourceDir = gamePath.resolve("crashes_$index")
                if (crashLogSourceDir.toFile().exists() && Files.isDirectory(crashLogSourceDir)) {
                    Files.list(crashLogSourceDir)
                            .forEach { crashLog ->
                                if (Files.size(crashLog) <= LOG_LIMIT) {
                                    Files.move(crashLog, targetLogsPath.resolve("${gameName}_${crashLog.fileName}"))
                                }
                            }
                }
                val logDir = gamePath.resolve("logs_$index")
                val scoresFile = logDir.resolve("scores.json")
                if (Files.exists(scoresFile)) {
                    val scores = mapper.readValue<ScoresJson>(scoresFile.toFile())
                    if (scores.is_crashed) {
                        val gameLog = logDir.resolve("game.log")
                        if (gameLog.toFile().exists() && Files.size(gameLog) <= LOG_LIMIT) {
                            Files.move(gameLog, targetLogsPath.resolve("${gameName}_game.log"))
                        }
                        val botLog = logDir.resolve("bot.log")
                        if (botLog.toFile().exists() && Files.size(botLog) <= LOG_LIMIT) {
                            Files.move(botLog, targetLogsPath.resolve("${gameName}_bot.log"))
                        }
                    }
                    return@mapIndexed scores
                }
                return@mapIndexed null
            }
            val resultFile = gamePath.resolve("result.json").toFile()
            if (resultFile.exists()) {
                val result = mapper.readValue<ResultJson>(resultFile)
                if (result.winner != null && result.loser != null) {
                    val winnerBot = botRepository.findByName(result.winner)
                            ?: throw BotNotFoundException("Could not find ${result.winner}")
                    val loserBot = botRepository.findByName(result.loser)
                            ?: throw BotNotFoundException("Could not find ${result.loser}")
                    events.post(GameEnded(winnerBot, loserBot, gameConfig.map, Instant.now(), result.game_time, gameConfig.gameName))
                } else {
                    val botA = botRepository.findByName(bots[0])
                            ?: throw BotNotFoundException("Could not find ${bots[0]}")
                    val botB = botRepository.findByName(bots[1])
                            ?: throw BotNotFoundException("Could not find ${bots[1]}")
                    // Marked as crashed if scores says so or no scores file is present
                    events.post(GameCrashed(botA, botB, gameConfig.map, scores[0]?.is_crashed != false, scores[1]?.is_crashed != false, Instant.now(),
                            result.is_realtime_outed, result.is_gametime_outed, result.game_time, gameConfig.gameName))
                }
            } else {
                val botA = botRepository.findByName(bots[0])
                        ?: throw BotNotFoundException("Could not find ${bots[0]}")
                val botB = botRepository.findByName(bots[1])
                        ?: throw BotNotFoundException("Could not find ${bots[1]}")
                events.post(GameCrashed(botA, botB, gameConfig.map, scores[0]?.is_crashed != false, scores[1]?.is_crashed != false, Instant.now(), false, false, 0.0, gameConfig.gameName))
            }
        }

        private fun updateResourceConstraints() {
            val botsParticipating = "${bots[0]} vs ${bots[1]}"
            val started = System.currentTimeMillis()
            while (System.currentTimeMillis() - started < 15000) {
                val lines = Docker.retrieveContainersWithName("GAME_$gameName")
                if (lines.size == bots.size) {
                    lines.forEach { id ->
                        log.info("Limiting container $id to 1 CPU and 1G memory")
                        Docker.updateResourceConstraints(id)
                    }
                    return
                }
                Thread.sleep(1000)
            }
            throw FailedToLimitResources("Failed to limit resources for $botsParticipating")
        }
    }
}


class BotJson(
        val name: String,
        val race: String,
        val botType: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
class ResultJson(
        val bots: List<String>,
        val is_crashed: Boolean = false,
        val is_gametime_outed: Boolean = false,
        val is_realtime_outed: Boolean,
        val game_time: Double,
        val winner: String? = null,
        val loser: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
class ScoresJson(
        val is_winner: Boolean,
        val is_crashed: Boolean,
        val building_score: Long,
        val kill_score: Long,
        val razing_score: Long,
        val unit_score: Long
)

class FailedToLimitResources(message: String) : RuntimeException(message)
class FailedToKillContainer(message: String) : RuntimeException(message)
class FailedToKillSCBW(message: String) : RuntimeException(message)
class FailedToDownloadBot(message: String) : RuntimeException(message)
class FailedToDownloadBwApi(message: String) : RuntimeException(message)
class BotDisabledException(message: String) : RuntimeException(message)