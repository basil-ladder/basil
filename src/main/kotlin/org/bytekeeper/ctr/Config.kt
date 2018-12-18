package org.bytekeeper.ctr

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.file.Path

@ConfigurationProperties("basil")
@Component
class Config {
    var botUpdateTimer: Long = 6 * 60
    var publishTimer: Long = 30
    lateinit var publishCommand: String
    lateinit var publishBasePath: Path
    var gameResultsHours: Long = 48
    lateinit var dataBasePath: Path
}

@ConfigurationProperties("scbw")
@Component
class ScbwConfig(@Value("\${user.home}") private val userHome: Path) {
    private val defaultScbwPath = userHome.resolve(".scbw")
    var botsDir: Path = defaultScbwPath.resolve("bots")
    var gamesDir: Path = defaultScbwPath.resolve("games")
    var realtimeTimeoutSeconds: Int = 1800
    var frameTimeout: Int = 60 * 60 * 24
    var gameSpeed: Int? = null
    var dockerImage: String = "starcraft:game"
    var readOverWrite: Boolean = true
    var deleteGamesInGameDir: Boolean = true
}
