package org.bytekeeper.ctr.scbw

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.*
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.annotation.PreDestroy
import javax.persistence.EntityManager
import kotlin.concurrent.thread
import kotlin.streams.asSequence

const val killTimeout = 20L
const val LOG_LIMIT = 200 * 1024

@Service
class Scbw(private val botRepository: BotRepository,
           private val unitEventRepository: UnitEventsRepository,
           private val entityManager: EntityManager,
           private val botSources: BotSources,
           private val scbwConfig: ScbwConfig,
           private val config: Config,
           private val events: Events,
           private val publisher: Publisher,
           private val maps: Maps,
           private val botService: BotService) {
    private val log = LogManager.getLogger()
    @Volatile
    private var shuttingDown: Boolean = false


    @PreDestroy
    fun killContainers() {
        log.info("Killing remaining containers...")
        shuttingDown = true
        Docker.retrieveContainersWithName("GAME_CTR")
                .parallelStream()
                .forEach { Docker.killContainer(it) }
    }

    fun checkBotDirectory(bot: Bot) {
        val name = bot.name
        val botDir = scbwConfig.botsDir.resolve(name)
        val aiDir = botDir.resolve("AI")
        val readDir = botDir.resolve("read")
        val writeDir = botDir.resolve("write")
        val botJsonDef = botDir.resolve("bot.json")
        val bwapiDll = botDir.resolve("BWAPI.dll")

        val extension = BotType.valueOf(bot.botType).extension
        val missingFiles = arrayOf(botDir, aiDir, readDir, writeDir, botJsonDef, bwapiDll).filter { !it.toFile().exists() }
        val aiFiles = if (aiDir.toFile().exists()) Files.list(aiDir).use {
            it.asSequence().map {
                it.toString()
            }.filter {
                !it.contains("bwapi", true)
            }.count { it.endsWith(".$extension", true) }
        } else 0
        if (missingFiles.isNotEmpty() || aiFiles == 0 || aiFiles > 1) {
            botService.disableBot(bot, "Invalid file structure!")
            throw BotFolderInvalid("Bot $name has an invalid data dir, the following files are missing: ${missingFiles.joinToString()}; number of AI files: $aiFiles.")
        }
    }


    fun setupOrUpdateBot(botInfo: BotInfo) {
        check(!botInfo.disabled)

        val name = botInfo.name

        val botDir = scbwConfig.botsDir.resolve(name)
        val aiDir = botDir.resolve("AI")
        val readDir = botDir.resolve("read")
        val writeDir = botDir.resolve("write")
        val additionalReadPath = botDir.resolve("additionalRead")
        val botJsonDef = botDir.resolve("bot.json")
        val bwapiDll = botDir.resolve("BWAPI.dll")

        log.info("Checking $name for updates...")
        val bot = botRepository.findByName(name)!!

        if (bot.lastUpdated?.isBefore(botInfo.lastUpdated) == false) {
            log.info("Bot $name is already up-to-date.")
            return
        }


        log.info("Bot $name was updated ${botInfo.lastUpdated}${if (bot.lastUpdated != null) ", local version is from " + bot.lastUpdated else ""}, deleting AI dir")

        deleteDirectory(aiDir)

        if (botInfo.clearReadDirectory) {
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
        botSources.downloadBwapiDLL(botInfo).use {
            Files.copy(it, bwapiDll, StandardCopyOption.REPLACE_EXISTING)
        }
        log.info("Downloading $name's binary")
        ZipInputStream(botSources.downloadBinary(botInfo))
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
        jacksonObjectMapper().writeValue(botJsonDef.toFile(), BotJson(name,
                when (botInfo.race) {
                    Race.TERRAN -> "Terran"
                    Race.ZERG -> "Zerg"
                    Race.PROTOSS -> "Protoss"
                    Race.RANDOM -> "Random"
                }, botInfo.botType))
        if (additionalReadPath.toFile().exists() && Files.isDirectory(additionalReadPath)) {
            log.info("There are additional 'read' files that will be copied to the read directory")
            Files.newDirectoryStream(additionalReadPath).use {
                it.forEach {
                    Files.copy(it, readDir.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        log.info("Successfully setup $name")
        events.post(BotBinaryUpdated(bot, botInfo.lastUpdated))
    }

    fun runGame(gameConfig: GameConfig) {
        ScbwGameRunner(gameConfig).run()
    }

    fun readDirectoryOf(bot: Bot): Path {
        val botDir = scbwConfig.botsDir.resolve(bot.name)
        return botDir.resolve("read")
    }

    data class GameConfig(val bots: List<String>, val map: String, val gameName: String)

    private inner class ScbwGameRunner(val gameConfig: GameConfig) {
        private val log = LogManager.getLogger()
        private val bots = gameConfig.bots
        private val gameName = gameConfig.gameName
        private val dockerPrefix = "GAME_${gameName}"
        private val selectableRaces = listOf(Race.ZERG, Race.TERRAN, Race.PROTOSS)

        fun Race.toScbw() = when (this) {
            Race.ZERG -> "Z"
            Race.TERRAN -> "T"
            Race.PROTOSS -> "P"
            Race.RANDOM -> "R"
        }

        fun run() {
            val botsParticipating = "${bots[0]} vs ${bots[1]}"
            log.info("Upcoming: $gameName: $botsParticipating")
            val cmd = mutableListOf("scbw.play", "--headless")

            fun addParameter(par: String, vararg value: Any?) {
                val notNull = value.filterNotNull()
                if (notNull.isEmpty()) return
                cmd += par
                cmd += value.joinToString(" ") { it.toString() }
            }

            cmd += "--bots"

            val botA = botRepository.findByName(bots[0])!!
            val botB = botRepository.findByName(bots[1])!!
            val botARace = if (botA.race == Race.RANDOM) selectableRaces.random() else botA.race
            val botBRace = if (botB.race == Race.RANDOM) selectableRaces.random() else botB.race
            cmd += "${bots[0]}:${botARace.toScbw()}"
            cmd += "${bots[1]}:${botBRace.toScbw()}"

            addParameter("--timeout", scbwConfig.realtimeTimeoutSeconds)
            addParameter("--bot_dir", scbwConfig.botsDir)
            addParameter("--map", gameConfig.map)
            addParameter("--game_dir", scbwConfig.gamesDir)
            addParameter("--game_speed", scbwConfig.gameSpeed)
            addParameter("--game_name", gameName)
            addParameter("--docker_image", scbwConfig.dockerImage)
            addParameter("--timeout_at_frame", scbwConfig.frameTimeout)
            addParameter("--nano_cpus", 1000000000)
            addParameter("--mem_limit", "1G")
//            cmd += "--log_level"
//            cmd += "DEBUG"
            if (scbwConfig.readOverWrite) cmd += "--read_overwrite"
            val process = ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start()
            thread(true) {
                process.inputStream.bufferedReader().use { reader ->
                    reader.lineSequence().forEach { log.info("$gameName - $it") }
                }
            }

            try {
                val hardTimeLimit = scbwConfig.realtimeTimeoutSeconds + 30L
                val exited = process.waitFor(hardTimeLimit, TimeUnit.SECONDS)

                if (!exited) {
                    log.error("Timeout - killing game $gameName, $botsParticipating")
                    return
                }
                if (shuttingDown) {
                    throw InterruptedException()
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
                // Wait for a short while to make sure scores and results are written
                Thread.sleep(2000)
                cleanUpBotContainers()
                // Try to move results
                publisher.preparePublish {
                    moveResult(scbwConfig.gamesDir, gameName, bots, botA, botARace, botB, botBRace)
                }
                if (scbwConfig.deleteGamesInGameDir) {
                    deleteDirectory(scbwConfig.gamesDir.resolve("GAME_$gameName"))
                } else {
                    log.warn("Deleting game dirs is switched off, the ${scbwConfig.gamesDir.toFile().absolutePath} might run full.")
                }
            }
        }

        private fun cleanUpBotContainers() {
            Docker.retrieveContainersWithName(dockerPrefix)
                    .forEach {
                        log.info("Killing game ${gameConfig.gameName}, bot container $it")
                        val killed = Docker.killContainer(it)
                                .waitFor(killTimeout, TimeUnit.SECONDS)
                        if (!killed) {
                            throw FailedToKillContainer("Couldn't kill container $it after $killTimeout seconds")
                        }
                    }
        }

        private fun moveResult(gameDir: Path, gameName: String, bots: List<String>, botA: Bot, botARace: Race, botB: Bot, botBRace: Race) {
            val mapper = jacksonObjectMapper()
            val gamePath = gameDir.resolve("GAME_$gameName")
            val botsPath = config.dataBasePath.resolve("bots")
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
                    Files.newDirectoryStream(crashLogSourceDir).use {
                        it.forEach { crashLog ->
                            moveLog(crashLog, targetLogsPath.resolve("${gameName}_${crashLog.fileName}"))
                        }
                    }
                }
                val logDir = gamePath.resolve("logs_$index")
                val frameCount = logDir.resolve("frames.csv").let { frameFile ->
                    if (frameFile.toFile().exists()) {
                        Files.newBufferedReader(frameFile).useLines { lines ->
                            var lastLine = ""
                            var sumFrameTime: Double? = null
                            for (line in lines) {
                                lastLine = if (line.isNotBlank()) line else lastLine
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

                val unitEvents = logDir.resolve("unit_events.csv").let { unitEventFile ->
                    if (unitEventFile.toFile().exists()) {
                        Files.readAllLines(unitEventFile).drop(1)
                    } else emptyList()
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

                return@mapIndexed BotResult(scores, frameCount, unitEvents)
            }
            val frameCount = botResults.mapNotNull { it.frames?.frameCount }.min()
            val resultFile = gamePath.resolve("result.json").toFile()
            val gameId = UUID.randomUUID()
            persistUnitEvents(gameId, botA, botResults[0].unitEvents, botB, botResults[1].unitEvents)
            if (resultFile.exists()) {
                val result = mapper.readValue<ResultJson>(resultFile)
                if (result.winner != null && result.loser != null) {
                    val winnerBot = botRepository.findByName(result.winner)
                            ?: throw BotNotFoundException("Could not find ${result.winner}")
                    val loserBot = botRepository.findByName(result.loser)
                            ?: throw BotNotFoundException("Could not find ${result.loser}")
                    events.post(GameEnded(
                            gameId,
                            winnerBot,
                            if (winnerBot == botA) botARace else botBRace,
                            loserBot,
                            if (loserBot == botB) botBRace else botARace,
                            gameConfig.map,
                            Instant.now(),
                            result.game_time,
                            gameConfig.gameName,
                            frameCount))
                } else {
                    if (result.is_crashed) {
                        events.post(GameCrashed(
                                gameId,
                                botA,
                                botARace,
                                botB,
                                botBRace,
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
                                    when {
                                        frameSumA > frameSumB -> botA
                                        frameSumA < frameSumB -> botB
                                        else -> null
                                    }
                                }

                        events.post(GameTimedOut(
                                gameId,
                                botA,
                                botARace,
                                botB,
                                botBRace,
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
                events.post(GameFailedToStart(
                        gameId,
                        botA,
                        botARace,
                        botB,
                        botBRace,
                        gameConfig.map,
                        Instant.now(),
                        gameConfig.gameName))
            }
        }

        private fun persistUnitEvents(gameId: UUID, botA: Bot, botAEvents: List<String>, botB: Bot, botBEvents: List<String>) {
            val game = entityManager.getReference(GameResult::class.java, gameId)
            fun preprocess(bot: Bot, events: List<String>): List<UnitEvent> {
                fun toUnitEvent(fld: List<String>): UnitEvent? {
                    if (!fld[2].toBoolean()) return null
                    val pos = fld[5].split('(', ')', ',')
                    val unitTypeString = fld[4].replace(' ', '_')
                    val unitType = UnitType.fromLogEvent(unitTypeString)
                    if (unitType == UnitType.UNKNOWN)
                        log.error("Could not found unit type for $unitTypeString!")
                    return UnitEvent(fld[0].toInt(),
                            UnitEventType.fromLogEvent(fld[1]),
                            game,
                            bot,
                            fld[3].toShort(),
                            unitType,
                            pos[1].toShort(), pos[2].toShort())
                }
                return events.map(CSV::parseLine)
                        .mapNotNull(::toUnitEvent)
                        .filter {
                            it.event != UnitEventType.UNIT_RENEGADE
                        }
            }
            unitEventRepository.saveAll(preprocess(botA, botAEvents))
            unitEventRepository.saveAll(preprocess(botB, botBEvents))
        }

        private fun moveLog(source: Path, dest: Path) {
            if (Files.size(source) > LOG_LIMIT) {
                dest.toFile().writeText("Log file exceeds limit of $LOG_LIMIT bytes!")
            } else
                Files.move(source, dest)
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
class BotResult(val scores: ScoresJson?, val frames: FrameResult?, val unitEvents: List<String>)

class BotFolderInvalid(message: String) : java.lang.RuntimeException(message)
class FailedToLimitResources(message: String) : RuntimeException(message)
class FailedToKillContainer(message: String) : RuntimeException(message)
class FailedToKillSCBW(message: String) : RuntimeException(message)
class BotNotFoundException(message: String) : RuntimeException(message)
