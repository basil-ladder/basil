package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.Maps
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.Publisher.Companion.bool2Short
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PerBotGamesPublisher(
    private val gameResultRepository: GameResultRepository,
    private val publisher: Publisher,
    private val maps: Maps
) {
    @CommandHandler
    @Timed
    @Transactional(readOnly = true)
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer().without(JsonWriteFeature.QUOTE_FIELD_NAMES)

        val maptoIndex = maps.allMaps().mapIndexed { index, s -> s.fileName to index }.toMap()
        val botToIndex = mutableMapOf<String, Int>()
        var maxBotIndex = 0
        var lastBot: String? = null
        val aggregate = mutableListOf<PublishedBotGameResult>()
        for (result in gameResultRepository.findAllGamesSummarized()) {
            if (result.bot != lastBot) {
                publish(lastBot, writer, aggregate, botToIndex)
                aggregate.clear()

                lastBot = result.bot
            }
            val botIndex = botToIndex[result.enemy] ?: let {
                botToIndex[result.enemy] = ++maxBotIndex
                maxBotIndex
            }
            aggregate += PublishedBotGameResult(
                result.botRace.short, botIndex, result.enemyRace.short, maptoIndex[result.map]
                    ?: 0, (result.time.epochSecond / 3600).toString(16), result.frameCount, bool2Short(result.won)
            )
        }
        publish(lastBot, writer, aggregate, botToIndex)
    }

    private fun publish(
        bot: String?,
        writer: ObjectWriter,
        aggregate: List<PublishedBotGameResult>,
        botToIndex: Map<String, Int>
    ) {
        bot?.let {
            publisher.botStatsWriter(it, "allGameResults.json").use { out ->
                writer.writeValue(out, PublishedBotGameResults(maps.allMaps().map {
                    it.mapName ?: it.fileName
                }, botToIndex.entries.sortedBy { it.value }.map { it.key }, aggregate))
            }
        }
    }

    data class PublishedBotGameResults(
        val maps: List<String>,
        val bots: List<String>,
        val results: List<PublishedBotGameResult>
    )

    data class PublishedBotGameResult(
        @JsonProperty("r") val race: String,
        @JsonProperty("e") val enemyIndex: Int,
        @JsonProperty("eR") val enemyRace: String,
        @JsonProperty("m") val map: Int,
        @JsonProperty("t") val epochHours: String,
        @JsonProperty("fc") @JsonInclude(JsonInclude.Include.NON_NULL) val frameCount: Int?,
        @JsonProperty("w") @JsonInclude(JsonInclude.Include.NON_NULL) val won: Short?
    )
}