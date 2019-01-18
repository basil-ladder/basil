package org.bytekeeper.ctr

import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Service
class Publisher(config: Config) {
    private final val statsPath = config.publishBasePath.resolve("stats")
    private final val botsDataPath = config.dataBasePath.resolve("bots")

    init {
        Files.createDirectories(statsPath)
    }

    private fun botStatsPath(bot: String): Path {
        val botPath = statsPath.resolve(bot)
        Files.createDirectories(botPath)
        return botPath
    }

    fun botDataPath(bot: String): Path {
        val botPath = botsDataPath.resolve(bot)
        Files.createDirectories(botPath)
        return botPath
    }

    fun botStatsWriter(bot: String, file: String): Writer =
            Files.newBufferedWriter(botStatsPath(bot).resolve(file), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)

    fun globalStatsWriter(file: String): BufferedWriter =
            Files.newBufferedWriter(statsPath.resolve(file), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

}