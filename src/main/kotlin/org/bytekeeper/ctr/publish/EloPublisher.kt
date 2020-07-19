package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.BotEloRepository
import org.bytekeeper.ctr.repository.BotHistoryRepository
import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.stereotype.Component

@Component
class EloPublisher(private val botEloRepository: BotEloRepository,
                   private val botRepository: BotRepository,
                   private val publisher: Publisher,
                   private val botHistoryRepository: BotHistoryRepository) {

    @CommandHandler
    @Timed
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAllByEnabledTrue()
        allBots.forEach { bot ->
            publisher.botStatsWriter(bot.name, "eloHistory.json")
                    .use { out ->
                        val elos = botEloRepository.findAllByBotOrderByTimeAsc(bot);
                        val updateHistory = botHistoryRepository.findAllByBotOrderByTimeAsc(bot)
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

}


class PublishedBotEloHistoryEntry(val epochSecond: Long, val rating: Int, @JsonInclude(JsonInclude.Include.NON_NULL) val updated: Boolean?)
