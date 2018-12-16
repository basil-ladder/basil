package org.bytekeeper.ctr.publish

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.time.Instant

@Component
class ReadDirectoryPublisher(private val botRepository: BotRepository,
                             private val publisher: Publisher,
                             private val scbw: Scbw) {
    private val log = LogManager.getLogger()

    @CommandHandler
    fun handle(command: PreparePublish) {
        val relevantUpdateTime = Instant.now().minusSeconds(86400)
        botRepository.findAllByEnabledTrueAndPublishReadTrue()
                .forEach { publish(it, relevantUpdateTime) }
    }

    private fun publish(bot: Bot, relevantUpdateTime: Instant?) {
        val readDir = scbw.readDirectoryOf(bot)
        val readSize = Files.walk(readDir)
                .filter { Files.isRegularFile(it) }
                .mapToLong { Files.size(it) }
                .sum()
        if (readSize > 100 * 1024 * 1024)
            return

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
        } finally {
            Files.delete(compressedFile)
        }
    }
}