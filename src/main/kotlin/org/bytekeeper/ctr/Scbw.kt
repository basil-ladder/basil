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
        if (!bot!!.enabled) {
            events.post(BotEnabled(bot))
        }

        val botDir = config.botsDir.resolve(name)
        val aiDir = botDir.resolve("AI")
        val readDir = botDir.resolve("read")
        val writeDir = botDir.resolve("write")
        val additionalReadPath = botDir.resolve("additionalRead")
        val botJsonDef = botDir.resolve("bot.json")

        log.info("Bot $name was updated ${sscaitBot.lastUpdated()}${if (bot.lastUpdated != null) ", local version is from " + bot.lastUpdated else ""}, deleting AI dir")
        events.post(BotUpdated(bot, sscaitBot.lastUpdated()))

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

            cmd += if (botRepository.findByName(bots[0])?.race == Race.RANDOM) bots[0] + ":" + listOf("Z", "T", "P").random() else bots[0]
            cmd += if (botRepository.findByName(bots[1])?.race == Race.RANDOM) bots[1] + ":" + listOf("Z", "T", "P").random() else bots[1]

            addParameter("--timeout", config.realtimeTimeoutSeconds)
            addParameter("--bot_dir", config.botsDir)
            addParameter("--map", gameConfig.map)
            addParameter("--game_dir", config.gamesDir)
            addParameter("--game_speed", config.gameSpeed)
            addParameter("--game_name", gameName)
            addParameter("--docker_image", config.dockerImage)
            addParameter("--timeout_at_frame", config.frameTimeout)
//            cmd += "--log_level"
//            cmd += "DEBUG"
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
                Thread.sleep(2000)
                // Try to move results
                moveResult(config.gamesDir, gameName, config.targetDir, bots)
                if (config.deleteGamesInGameDir) {
                    deleteDirectory(config.gamesDir.resolve("GAME_$gameName"))
                } else {
                    log.warn("Deleting game dirs is switched off, the ${config.gamesDir.toFile().absolutePath} might run full.")
                }
            }
        }

        private fun cleanUpBotContainers(): Int {
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
            return remaining
        }

        private fun moveResult(gameDir: Path, gameName: String, targetPath: Path, bots: List<String>) {
            val mapper = jacksonObjectMapper()
            val gamePath = gameDir.resolve("GAME_$gameName")
            val botsPath = targetPath.resolve("bots")
            val botResults = bots.mapIndexed { index, name ->
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
                                moveLog(crashLog, targetLogsPath.resolve("${gameName}_${crashLog.fileName}"))
                            }
                }
                val logDir = gamePath.resolve("logs_$index")
                val frameCount = logDir.resolve("frames.csv").let { frameFile ->
                    if (frameFile.toFile().exists()) {
                        Files.newBufferedReader(frameFile).useLines { lines ->
                            var lastLine = ""
                            var sumFrameTime: Double? = null
                            for (line in lines) {
                                lastLine = line
                                if (sumFrameTime == null)
                                    sumFrameTime = 0.0
                                else sumFrameTime += line.split(',')[1].toDouble()

                            }
                            FrameResult(
                                    lastLine.substringBefore(',').toInt(),
                                    sumFrameTime ?: 0.0)
                        }
                    } else null
                }

                val scores = logDir.resolve("scores.json").let { scoresFile ->
                    if (scoresFile.toFile().exists()) {
                        val scores = mapper.readValue<ScoresJson>(scoresFile.toFile())
                        if (scores.is_crashed) {
                            moveLog(logDir.resolve("game.log"), targetLogsPath.resolve("${gameName}_game.log"))
                            moveLog(logDir.resolve("bot.log"), targetLogsPath.resolve("${gameName}_bot.log"))
                        }
                        scores
                    } else null
                }

                return@mapIndexed BotResult(scores, frameCount)
            }
            val frameCount = botResults.mapNotNull { it.frames?.frameCount }.min()
            val resultFile = gamePath.resolve("result.json").toFile()
            if (resultFile.exists()) {
                val result = mapper.readValue<ResultJson>(resultFile)
                if (result.winner != null && result.loser != null) {
                    val winnerBot = botRepository.findByName(result.winner)
                            ?: throw BotNotFoundException("Could not find ${result.winner}")
                    val loserBot = botRepository.findByName(result.loser)
                            ?: throw BotNotFoundException("Could not find ${result.loser}")
                    events.post(GameEnded(winnerBot, loserBot, gameConfig.map, Instant.now(), result.game_time, gameConfig.gameName, frameCount))
                } else {
                    val botA = botRepository.findByName(bots[0])
                            ?: throw BotNotFoundException("Could not find ${bots[0]}")
                    val botB = botRepository.findByName(bots[1])
                            ?: throw BotNotFoundException("Could not find ${bots[1]}")

                    if (result.is_crashed) {
                        events.post(GameCrashed(
                                botA,
                                botB,
                                gameConfig.map,
                                botResults[0].scores?.is_crashed != false,
                                botResults[1].scores?.is_crashed != false,
                                Instant.now(),
                                result.game_time,
                                gameConfig.gameName,
                                frameCount))
                    } else {
                        require(result.is_gametime_outed || result.is_realtime_outed)
                        val slowerBot =
                                if (botResults.any { it.frames == null }) null
                                else {
                                    val frameSumA = botResults[0].frames!!.sumFrameTime
                                    val frameSumB = botResults[1].frames!!.sumFrameTime
                                    if (frameSumA > frameSumB) botA
                                    else if (frameSumA < frameSumB) botB
                                    else null
                                }

                        events.post(GameTimedOut(
                                botA,
                                botB,
                                slowerBot,
                                botResults[0].scores?.let { it.kill_score + it.razing_score } ?: 0,
                                botResults[1].scores?.let { it.kill_score + it.razing_score } ?: 0,
                                gameConfig.map,
                                Instant.now(),
                                result.is_realtime_outed,
                                result.is_gametime_outed,
                                result.game_time,
                                gameConfig.gameName,
                                frameCount))
                    }
                }
            } else {
                val botA = botRepository.findByName(bots[0])
                        ?: throw BotNotFoundException("Could not find ${bots[0]}")
                val botB = botRepository.findByName(bots[1])
                        ?: throw BotNotFoundException("Could not find ${bots[1]}")
                events.post(GameFailedToStart(botA, botB, gameConfig.map, Instant.now(), gameConfig.gameName))
            }
        }

        private fun moveLog(source: Path, dest: Path) {
            if (Files.size(source) > LOG_LIMIT) {
                dest.toFile().writeText("Log file exceeds limit of $LOG_LIMIT bytes!")
            } else
                Files.move(source, dest)
        }


        private fun updateResourceConstraints() {
            val botsParticipating = "${bots[0]} vs ${bots[1]}"
            val started = System.currentTimeMillis()
            while (System.currentTimeMillis() - started < 10000) {
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
        val is_gametime_outed: Boolean,
        val is_realtime_outed: Boolean,
        val game_time: Double,
        val winner: String? = null,
        val loser: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
class ScoresJson(
        val is_winner: Boolean,
        val is_crashed: Boolean,
        val building_score: Int,
        val kill_score: Int,
        val razing_score: Int,
        val unit_score: Int
)

class FrameResult(val frameCount: Int, val sumFrameTime: Double)
class BotResult(val scores: ScoresJson?, val frames: FrameResult?)

class FailedToLimitResources(message: String) : RuntimeException(message)
class FailedToKillContainer(message: String) : RuntimeException(message)
class FailedToKillSCBW(message: String) : RuntimeException(message)
class FailedToDownloadBot(message: String) : RuntimeException(message)
class FailedToDownloadBwApi(message: String) : RuntimeException(message)
class BotDisabledException(message: String) : RuntimeException(message)