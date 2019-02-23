package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.BotEloRepository
import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.stereotype.Component

@Component
class EloPublisher(private val botEloRepository: BotEloRepository,
                   private val botRepository: BotRepository,
                   private val publisher: Publisher) {

    @CommandHandler
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAllByEnabledTrue()
        allBots.forEach { bot ->
            publisher.botStatsWriter(bot.name, "eloHistory.json")
                    .use { out ->
                        writer.writeValue(out, botEloRepository.findAllByBot(bot)
                                .map { PublishedBotEloHistoryEntry(it.time.epochSecond, it.rating, it.gameHash) })
                    }
        }
    }

}


class PublishedBotEloHistoryEntry(val epochSecond: Long, val rating: Int, val gameHash: String)
