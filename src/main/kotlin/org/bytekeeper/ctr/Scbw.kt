package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

const val killTimeout = 20L
const val LOG_LIMIT = 200 * 1024

class ScbwRunner(
    val bots: List<String>,
    val timeout: Int? = Config.timeout ?: 1200,
    val botDir: String? = Config.botDir,
    val gameDir: String = Config.gameDir,
    val readOverwrite: Boolean? = true,
    val gameSpeed: Int? = null,
    val gameName: String,
    val dockerImage: String? = "starcraft:game",
    val targetDir: String = Config.targetDir
) {
    private val log = LogManager.getLogger()

    fun run() {
        val botsParticipating = "${bots[0]} vs ${bots[1]}"
        log.info("Upcoming: $gameName: $botsParticipating")
        val cmd = mutableListOf("scbw.play", "--headless")

        fun addParameter(par: String, vararg value: Any?) {
            val notNull = value.filterNotNull()
            if (notNull.isEmpty()) return
            cmd += par;
            cmd += value.map { it.toString() }.joinToString(" ")
        }

        cmd += "--bots"
        cmd += bots
        addParameter("--timeout", timeout)
        addParameter("--bot_dir", botDir)
        addParameter("--game_dir", gameDir)
        addParameter("--game_speed", gameSpeed)
        addParameter("--game_name", gameName)
        addParameter("--docker_image", dockerImage)
        if (readOverwrite == true) cmd += "--read_overwrite"
        val process = ProcessBuilder(cmd)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
        try {
            updateResourceConstraints()
            val hardTimeLimit = (timeout?.plus(30) ?: 15 * 60).toLong()
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
            // Try to move results
            moveResult(gameDir, gameName, targetDir, bots)
            if (Config.deleteGamesInGameDir) {
                deleteDirectory(Paths.get(gameDir, "GAME_$gameName"))
            } else {
                log.warn("Deleting game dirs is switched off, the $gameDir might run full.")
            }
        }
    }

    private fun cleanUpBotContainers() {
        val started = System.currentTimeMillis()
        var remaining = 2
        while (System.currentTimeMillis() - started < 10000 && remaining > 0) {
            Docker.retrieveContainersWithName("GAME_$gameName")
                .forEach {
                    log.info("Killing game $gameName, bot container $it")
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

    private fun moveResult(gameDir: String, gameName: String, targetDir: String, bots: List<String>) {
        val gamePath = Paths.get(gameDir, "GAME_$gameName")
        val targetPath = Paths.get(targetDir)
        val botsPath = targetPath.resolve("bots")
        bots.forEachIndexed { index, name ->
            val botPath = botsPath.resolve(name)
            Files.createDirectories(botPath)
            val replayPath = gamePath.resolve("player_$index.rep")
            if (replayPath.toFile().exists()) {
                val enemies = bots.filterNot { it == name }.joinToString(" ")
                Files.move(replayPath, botPath.resolve("$name vs $enemies $gameName.rep"))
            }

            // Save crashlogs
            val targetLogsPath = botPath.resolve("logs")
            Files.createDirectories(targetLogsPath)
            val crashLogSourceDir = gamePath.resolve("crashes_$index")
            if (crashLogSourceDir.toFile().exists() && Files.isDirectory(crashLogSourceDir)) {
                var crashLogExists = false
                Files.list(crashLogSourceDir)
                    .forEach { crashLog ->
                        crashLogExists = true
                        if (Files.size(crashLog) <= LOG_LIMIT) {
                            Files.move(crashLog, targetLogsPath.resolve("${gameName}_${crashLog.fileName}"))
                        }
                    }
                if (crashLogExists) {
                    val gameLog = gamePath.resolve("logs_$index").resolve("game.log")
                    if (gameLog.toFile().exists() && Files.size(gameLog) <= LOG_LIMIT) {
                        Files.move(gameLog, targetLogsPath.resolve("${gameName}_game.log"))
                    }
                    val botLog = gamePath.resolve("logs_$index").resolve("bot.log")
                    if (botLog.toFile().exists() && Files.size(botLog) <= LOG_LIMIT) {
                        Files.move(botLog, targetLogsPath.resolve("${gameName}_bot.log"))
                    }
                }
            }
        }
        val resultFile = gamePath.resolve("result.json").toFile()
        if (resultFile.exists()) {
            val result = klaxon.parse<ResultJson>(resultFile)!!
            if (result.winner != null && result.loser != null) {
                Elo.updateElo(result.winner, result.loser)
            }
            val resultsPath = targetPath.resolve("results")
            Files.createDirectories(resultsPath)
            Files.write(resultsPath.resolve("$gameName.json"), klaxon.toJsonString(result).toByteArray())
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
                    Runtime.getRuntime().exec(arrayOf("docker", "update", "--cpus", "1", "--memory", "1G", id))
                    return
                }
            }
            Thread.sleep(1000)
        }
        throw FailedToLimitResources("Failed to limit resources for $botsParticipating")
    }
}

class BotJson(
    val name: String,
    val race: String,
    val botType: String,
    val timestamp: Long? = null
)

class ResultJson(
    val bots: List<String>,
    val is_crashed: Boolean = false,
    val is_gametime_outed: Boolean = false,
    val is_realtime_outed: Boolean,
    val game_time: Double,
    val winner: String? = null,
    val loser: String? = null
)

class FailedToLimitResources(message: String) : RuntimeException(message)
class FailedToKillContainer(message: String) : RuntimeException(message)
class FailedToKillSCBW(message: String) : RuntimeException(message)

private val botBaseDir = Config.botDir ?: "$userHome/.scbw/bots"

fun BotInfo.ensureBot() {
    val log = LogManager.getLogger()

    val botDir = Paths.get(botBaseDir, name)
    val aiDir = botDir.resolve("AI")
    val botJsonDef = botDir.resolve("bot.json")
    if (botDir.toFile().exists()) {
        if (botJsonDef.toFile().exists()) {
            val botJson = klaxon.parse<BotJson>(botJsonDef.toFile())!!
            if (botJson.timestamp == null || !Instant.ofEpochMilli(botJson.timestamp).isBefore(lastUpdated()))
                log.info("$name already downloaded")
            return
        } else {
            log.info("Bot was updated ${lastUpdated()}, deleting AI dir")
            deleteDirectory(aiDir)
        }
    }
    log.info("Creating directory structure for $name")
    Files.createDirectories(botDir)
    Files.createDirectories(aiDir)
    val readDir = botDir.resolve("read")
    Files.createDirectories(readDir)
    Files.createDirectories(botDir.resolve("write"))

    log.info("Downloading $name's BWAPI.dll")
    Files.copy(downloadBwapiDll(), botDir.resolve("BWAPI.dll"), StandardCopyOption.REPLACE_EXISTING)
    log.info("Downloading $name's binary")
    ZipInputStream(downloadBinary())
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
    Files.write(
        botJsonDef,
        listOf(klaxon.toJsonString(BotJson(name, race, botType, Instant.now().toEpochMilli())))
    )
    val additionalReadPath = botDir.resolve("additionalRead")
    if (additionalReadPath.toFile().exists() && Files.isDirectory(additionalReadPath)) {
        log.info("There are additional 'read' files that will be copied to the read directory")
        Files.list(additionalReadPath)
            .forEach {
                Files.copy(it, readDir.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
            }
    }
    log.info("Successfully setup $name")
}