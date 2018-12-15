package org.bytekeeper.ctr

import org.bytekeeper.ctr.SscaitClient.Companion.SSCAIT_DATE_FORMAT
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class SscaitClient {
    private val webClient = WebClient.builder().baseUrl("https://sscaitournament.com/").build()

    fun retrieveListOfBots(): Flux<BotInfo> =
            webClient.get().uri("api/bots.php").retrieve().bodyToFlux()

    fun downloadBinary(botInfo: BotInfo): Mono<ByteArrayResource> =
            webClient.get().uri(botInfo.botBinary)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono()

    fun downloadBwapiDLL(botInfo: BotInfo): Mono<ByteArrayResource> =
            webClient.get().uri(botInfo.bwapiDLL)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono()

    companion object {
        val SSCAIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    }
}

class BotInfo(
        val name: String,
        val race: String,
        val wins: String? = "0",
        val losses: String? = "0",
        val status: String? = "Disabled",
        private val update: String? = null,
        val botBinary: String,
        val bwapiDLL: String,
        val botType: String,
        val description: String? = ""
) {
    private val basilCommands: List<String>
        get() {
            val matcher = BASIL_COMMAND_MATCHER.matcher(description)
            if (matcher.find()) {
                return commands(matcher.group(1))
            }
            return emptyList()
        }

    val isDisabled = status == "Disabled"

    val isDisabledForBasil = basilCommands.contains("DISABLED")

    val clearReadDirectory = basilCommands.contains("RESET")

    val publishReadDirectory = basilCommands.contains("PUBLISH-READ")

    fun lastUpdated(): Instant =
            if (update == null) Instant.MIN else LocalDateTime.parse(update, SSCAIT_DATE_FORMAT).toInstant(ZoneOffset.UTC)

    companion object {
        internal val BASIL_COMMAND_MATCHER = "BASIL:\\s*((?:\\S+\\s*,\\s*)*\\S+)".toPattern()
        internal fun commands(basicCommand: String) = basicCommand.split("\\s*,\\s*".toRegex())
    }
}