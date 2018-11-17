package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

object Config {
    private val log = LogManager.getLogger()
    val gameDir: String
    val botDir: String?
    val timeout: Int?
    val botUpdateTimer: Int?
    val targetDir: String
    val publishCommand: String
    val deleteGamesInGameDir: Boolean
    val parallelGames: Int?
    var workDir: String?

    init {
        val properties = Properties()
        try {
            properties.load(
                InputStreamReader(
                    javaClass.getResourceAsStream("/application.properties"), StandardCharsets.UTF_8
                )
            )
        } catch (e: NullPointerException) {
            log.warn("No application.properties found, using default values")
        }

        gameDir = properties.getProperty("scbw.game_dir") ?: "$userHome/.scbw/games"
        botDir = properties.getProperty("scbw.bot_dir") ?: "$userHome/.scbw/bots"
        timeout = properties.getProperty("scbw.timeout")?.toInt()
        deleteGamesInGameDir = properties.getProperty("scbw.deleteGamesInGameDir")?.toBoolean() ?: true
        targetDir = properties.getProperty("ctr.targetDir")
        parallelGames = properties.getProperty("ctr.parallelGames")?.toInt()
        publishCommand = properties.getProperty("ctr.publishCommand")
        botUpdateTimer = properties.getProperty("sscait.botUpdateTimer")?.toInt()
        workDir = properties.getProperty("ctr.workDir") ?: "$userHome/.ctr"
    }
}