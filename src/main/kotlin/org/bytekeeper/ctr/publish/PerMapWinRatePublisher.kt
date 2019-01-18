package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.Maps
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component

@Component
class PerMapWinRatePublisher(private val botRepository: BotRepository,
                             private val gameResultRepository: GameResultRepository,
                             private val publisher: Publisher,
                             private val maps: Maps) {
    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAllByEnabledTrue()
        allBots.forEach { bot ->
            publisher.botStatsWriter(bot.name, "perMapStats.json")
                    .use { out ->
                        writer.writeValue(out, gameResultRepository.botStatsPerMap(bot)
                                .map { PublishedPerMapStats(maps.mapName(it.map) ?: it.map, it.won, it.lost) })
                    }
        }

    }
}

class PublishedPerMapStats(val map: String, val won: Long, val lost: Long)