package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.read
import kotlin.concurrent.write

@Service
class Publisher(private val config: Config,
                private val commands: Commands) {
    private val log = LogManager.getLogger()
    private val statsPath = config.publishBasePath.resolve("stats")
    private val botsDataPath = config.dataBasePath.resolve("bots")
    private val publishLock = ReentrantReadWriteLock()

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
            if (file.endsWith(".gz")) {
                BufferedWriter(OutputStreamWriter(GZIPOutputStream(Files.newOutputStream(botStatsPath(bot).resolve(file), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))))
            } else
                Files.newBufferedWriter(botStatsPath(bot).resolve(file), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)

    fun globalStatsWriter(file: String): BufferedWriter =
            if (file.endsWith(".gz")) {
                BufferedWriter(OutputStreamWriter(GZIPOutputStream(Files.newOutputStream(statsPath.resolve(file), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))))
            } else
                Files.newBufferedWriter(statsPath.resolve(file), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)


    @Scheduled(cron = "0 */30 * * * *")
    fun publish() {
        log.info("Publishing results")
        commands.handle(PreparePublish())

        // Write because concurrently finished games should wait before copying their state to the publish dir
        publishLock.write {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("/bin/bash", config.publishCommand))
                val exited = process
                        .waitFor(15, TimeUnit.MINUTES)
                if (!exited) {
                    log.error("Publishing of games still running after 15 minutes, killing it...")
                    process.destroyForcibly()
                }
            } catch (e: Exception) {
                log.error("Failed to publish results", e)
            }
        }
    }

    fun preparePublish(run: () -> Unit) {
        // Read because: Multiple parallel games can copy their state to the publish directory at the same time
        publishLock.read(run)
    }

    companion object {
        fun bool2Short(condition: Boolean?): Short? = if (condition == true) 1 else null
    }
}