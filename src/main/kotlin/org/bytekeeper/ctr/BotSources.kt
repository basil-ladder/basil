package org.bytekeeper.ctr

import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.Race
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.Instant

interface BotSource {
    fun allBotInfos(): List<BotInfo>
    fun refresh()
    fun downloadBwapiDLL(info: BotInfo): InputStream?
    fun downloadBinary(info: BotInfo): InputStream?
}

interface BotInfo {
    val name: String
    val publishReadDirectory: Boolean
    val authorKey: String?
    val clearReadDirectory: Boolean
    val disabled: Boolean
    val race: Race
    val botType: String
    fun lastUpdated(): Instant
}

@Service
class BotSources(private val botSources: List<BotSource>,
                 private val botRepository: BotRepository,
                 private val events: Events) {
    fun allBotInfos() = botSources.asReversed()
            .flatMap { it.allBotInfos() }
            .distinctBy { it.name }

    fun refresh() {
        botSources.forEach(BotSource::refresh)

        allBotInfos().forEach { botInfo ->
            events.post(BotInfoUpdated(
                    botInfo.name,
                    botInfo.race,
                    botInfo.botType,
                    botInfo.lastUpdated(),
                    !botInfo.disabled,
                    botInfo.publishReadDirectory,
                    botInfo.authorKey))
        }
    }

    fun downloadBwapiDLL(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBwapiDLL(botInfo) }.first()
    fun downloadBinary(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBinary(botInfo) }.first()
}