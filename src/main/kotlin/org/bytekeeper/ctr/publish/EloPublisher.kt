package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.*
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class EloPublisher(
    private val botEloRepository: BotEloRepository,
    private val publisher: Publisher,
    private val botHistoryRepository: BotHistoryRepository
) {

    @CommandHandler
    @Timed
    @Transactional
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val updateHistory = botHistoryRepository.findAllByOrderByTimeAsc()
        val allElos = botEloRepository.findEnabledOrderByBotAndTimeAsc().iterator()
        val elos = mutableListOf<BotElo>()
        while (allElos.hasNext()) {
            val next = allElos.next()
            if (elos.isEmpty() || elos.first().bot == next.bot) {
                elos += next
            } else {
                val bot = elos.first().bot
                publish(bot, elos, updateHistory.filter { it.bot == bot }, writer)
                elos.clear()
                elos.add(next)
            }
        }
        if (elos.isNotEmpty()) {
            val bot = elos.first().bot
            publish(bot, elos, updateHistory.filter { it.bot == bot }, writer)
        }
    }

    private fun publish(bot: Bot, elos: List<BotElo>, updateHistory: List<BotHistory>, writer: ObjectWriter) {
        publisher.botStatsWriter(bot.name, "eloHistory.json")
            .use { out ->
                val histIt = updateHistory.iterator()
                var historyEntry = if (histIt.hasNext()) histIt.next() else null
                val result = mutableListOf<PublishedBotEloHistoryEntry>()
                for (elo in elos) {
                    val updated =
                        if (historyEntry != null && elo.time >= historyEntry.time) {
                            while (historyEntry != null && historyEntry.time <= elo.time) {
                                historyEntry = if (histIt.hasNext()) histIt.next() else null
                            }
                            true
                        } else null
                    result += PublishedBotEloHistoryEntry(elo.time.epochSecond, elo.rating, updated)
                }

                writer.writeValue(out, result)
            }
    }

}


class PublishedBotEloHistoryEntry(val epochSecond: Long, val rating: Int, @JsonInclude(JsonInclude.Include.NON_NULL) val updated: Boolean?)
