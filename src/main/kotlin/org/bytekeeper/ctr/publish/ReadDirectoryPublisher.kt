package org.bytekeeper.ctr.publish

import io.micrometer.core.annotation.Timed
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.scbw.Scbw
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.time.Instant

@Component
class ReadDirectoryPublisher(private val botRepository: BotRepository,
                             private val publisher: Publisher,
                             private val scbw: Scbw,
                             private val config: Config) {
    private val log = LogManager.getLogger()

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val relevantUpdateTime = Instant.now().minusSeconds(86400)
        botRepository.findAllByEnabledTrueAndPublishReadTrue()
                .parallelStream()
                .forEach { publish(it, relevantUpdateTime) }
    }

    private fun publish(bot: Bot, relevantUpdateTime: Instant?) {
        val readDir = scbw.readDirectoryOf(bot)
        if (!readDir.toFile().exists()) {
            log.info("Bot ${bot.name}'s read directory is missing, not publishing it")
            return
        }

        val readSize = Files.walk(readDir)
                .filter { Files.isRegularFile(it) }
                .mapToLong { Files.size(it) }
                .sum()
        if (readSize > config.publishing.maxUncompressedRead * 1024 * 1024) {
            log.warn("Bot ${bot.name}'s read directory exceeds ${config.publishing.maxUncompressedRead} MB uncompressed and will not be published.")
            return
        }

        val targetFile = publisher.botDataPath(bot.name).resolve("read.7z")
        if (targetFile.toFile().exists() &&
                !Files.getLastModifiedTime(targetFile).toInstant().isBefore(relevantUpdateTime))
            return

        val compressedFile = if (bot.authorKeyId != null) Files.createTempFile("read-${bot.name}", ".7z") else targetFile

        createCompressedFile(compressedFile, readDir)

        if (bot.authorKeyId == null)
            return

        try {

            Gpg.ensureKeyIsPresent(bot.authorKeyId!!)
            Gpg.encryptFile(compressedFile, targetFile, bot.authorKeyId!!)

            log.info("Successfully encrypted read file for ${bot.name}")
        } catch (e: FailedToEncrypt) {
            log.error(e)
        } catch (e: FailedToEnsureKey) {
            log.error(e)
        } finally {
            Files.delete(compressedFile)
        }
    }
}