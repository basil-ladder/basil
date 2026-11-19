package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
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
    val supportedMapPools: List<String>
    val name: String
    val publishReadDirectory: Boolean
    val authorKey: String?
    val clearReadDirectory: Boolean
    val disabled: Boolean
    val race: Race
    val botType: String
    val lastUpdated: Instant
}

// Bot names become folder names and part of scbw.play arguments ("name:race").
private val validBotName = Regex("[\\p{L}\\p{N}_ -][\\p{L}\\p{N}_ .-]{0,49}")

fun isValidBotName(name: String) = validBotName.matches(name)

@Service
class BotSources(private val botSources: List<BotSource>) {
    private val log = LogManager.getLogger()

    fun allBotInfos() = botSources
            .flatMap { it.allBotInfos() }
            .distinctBy { it.name }
            .filter { botInfo ->
                isValidBotName(botInfo.name).also { if (!it) log.warn("Ignoring bot with invalid name '${botInfo.name}'") }
            }

    fun refresh() = botSources.forEach(BotSource::refresh)


    fun downloadBwapiDLL(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBwapiDLL(botInfo) }.first()
    fun downloadBinary(botInfo: BotInfo): InputStream = botSources.asSequence().mapNotNull { it.downloadBinary(botInfo) }.first()
    fun botInfoOf(name: String) =
            if (isValidBotName(name)) botSources.asSequence().mapNotNull { it.botInfoOf(name) }.firstOrNull() else null
}

class FailedToDownloadBot(message: String, e: Throwable? = null) : RuntimeException(message, e)
class FailedToDownloadBwApi(message: String) : RuntimeException(message)
