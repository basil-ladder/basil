package org.bytekeeper.ctr

import org.bytekeeper.ctr.repository.Race
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.Instant

interface BotSource {
    fun allBotInfos(): List<BotInfo>
    fun refresh()
    fun downloadBwapiDLL(info: BotInfo): InputStream?
    fun downloadBinary(info: BotInfo): InputStream?
    fun botInfoOf(name: String): BotInfo?
}

interface BotInfo {
    val name: String
    val publishReadDirectory: Boolean
    val authorKey: String?
    val clearReadDirectory: Boolean
    val disabled: Boolean
    val race: Race
    val botType: String
    val lastUpdated: Instant
}

@Service
class BotSources(private val botSources: List<BotSource>) {
    fun allBotInfos() = botSources
            .flatMap { it.allBotInfos() }
            .distinctBy { it.name }

    fun refresh() = botSources.forEach(BotSource::refresh)


    fun downloadBwapiDLL(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBwapiDLL(botInfo) }.first()
    fun downloadBinary(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBinary(botInfo) }.first()
    fun botInfoOf(name: String) = botSources.asSequence().mapNotNull { it.botInfoOf(name) }.firstOrNull()
}

class FailedToDownloadBot(message: String, e: Throwable? = null) : RuntimeException(message, e)
class FailedToDownloadBwApi(message: String) : RuntimeException(message)
