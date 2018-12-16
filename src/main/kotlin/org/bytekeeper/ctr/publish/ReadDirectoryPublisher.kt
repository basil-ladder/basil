package org.bytekeeper.ctr.publish

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.Scbw
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class ReadDirectoryPublisher(private val botRepository: BotRepository,
                             private val publisher: Publisher,
                             private val scbw: Scbw) {
    private val log = LogManager.getLogger()

    @CommandHandler
    fun handle(command: PreparePublish) {
        val relevantUpdateTime = Instant.now().minusSeconds(86400)
        botRepository.findAllByEnabledTrueAndPublishReadTrue()
                .forEach { bot ->
                    val readDir = scbw.readDirectoryOf(bot)
                    val readSize = Files.walk(readDir)
                            .filter { Files.isRegularFile(it) }
                            .mapToLong { Files.size(it) }
                            .sum()
                    if (readSize <= 100 * 1024 * 1024) {
                        val targetFile = publisher.botDataPath(bot.name).resolve("read.7z")
                        if (!targetFile.toFile().exists() ||
                                Files.getLastModifiedTime(targetFile).toInstant().isBefore(relevantUpdateTime)) {

                            val compressedFile = if (bot.authorKeyId != null) Files.createTempFile("read-${bot.name}", ".7z") else targetFile

                            SevenZOutputFile(compressedFile.toFile())
                                    .use { outFile ->
                                        Files.walk(readDir)
                                                .filter { Files.isRegularFile(it) }
                                                .forEach { path ->
                                                    val entry = outFile.createArchiveEntry(path.toFile(), readDir.relativize(path).toString())
                                                    outFile.putArchiveEntry(entry)
                                                    outFile.write(Files.readAllBytes(path))
                                                    outFile.closeArchiveEntry()
                                                }
                                    }

                            if (bot.authorKeyId != null) {
                                try {
                                    val checkForExistingKeyProcess = ProcessBuilder(listOf(
                                            "gpg",
                                            "--batch",
                                            "-k", bot.authorKeyId
                                    )).start().also {
                                        val stopped = it.waitFor(5, TimeUnit.SECONDS)
                                        if (!stopped) {
                                            log.error("Failed to download key ${bot.authorKeyId} for ${bot.name}")
                                            return@forEach
                                        }
                                    }
                                    if (checkForExistingKeyProcess.exitValue() != 0) {
                                        log.info("Key for ${bot.name} not yet downloaded, grabbing it.")
                                        val downloadAuthorKeyProcess = ProcessBuilder(listOf(
                                                "gpg",
                                                "--batch",
                                                "--keyserver", "pgp.mit.edu",
                                                "--recv-keys", bot.authorKeyId
                                        )).start().also {
                                            val stopped = it.waitFor(10, TimeUnit.SECONDS)
                                            if (!stopped && it.exitValue() != 0) {
                                                log.error("Could not retrieve key for ${bot.name}, key with id ${bot.authorKeyId} was not found!")
                                                return@forEach
                                            }
                                        }
                                        log.info("Key for ${bot.name} retrieved.")
                                    }
                                    ProcessBuilder(listOf(
                                            "gpg",
                                            "--batch",
                                            "--trust-model", "ALWAYS",
                                            "--output", targetFile.toString(),
                                            "--encrypt",
                                            "--recipient", bot.authorKeyId,
                                            compressedFile.toString()))
                                            .start()
                                            .also {
                                                val stopped = it.waitFor(1, TimeUnit.MINUTES)
                                                if (!stopped) {
                                                    log.error("Encryption still ran after 1min for ${bot.name}, aborting.")
                                                    return@forEach
                                                }
                                            }
                                    log.info("Successfully encrypted read file for ${bot.name}")
                                } finally {
                                    Files.delete(compressedFile)
                                }
                            }
                        }
                    }
                }
    }
}