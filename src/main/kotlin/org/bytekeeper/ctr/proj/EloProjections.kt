package org.bytekeeper.ctr.proj

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.EloUpdated
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotElo
import org.bytekeeper.ctr.entity.BotEloRepository
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class EloProjections(private val botEloRepository: BotEloRepository,
                     private val botRepository: BotRepository,
                     private val publisher: Publisher) {

    @Transactional
    fun onEloUpdated(event: EloUpdated) {
        botEloRepository.save(BotElo(bot = event.bot, time = event.timestamp, rating = event.newRating, gameHash = event.gameHash))
    }

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
