package org.bytekeeper.ctr.publish

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.Scbw
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.time.Instant

@Component
class ReadDirectoryPublisher(private val botRepository: BotRepository,
                             private val publisher: Publisher,
                             private val scbw: Scbw) {
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

                            SevenZOutputFile(targetFile.toFile())
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
                        }
                    }
                }
    }
}