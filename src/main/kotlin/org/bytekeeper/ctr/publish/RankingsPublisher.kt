package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component

@Component
class RankingsPublisher(private val publisher: Publisher,
                        private val botRepository: BotRepository) {

    @CommandHandler
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAllByEnabledTrue()
        publisher.globalStatsWriter("ranking.json")
                .use { out ->
                    writer.writeValue(out, allBots
                            .map {
                                PublishedBotRanking(
                                        it.name,
                                        it.rating,
                                        it.played,
                                        it.won,
                                        it.lost,
                                        it.crashed,
                                        it.race?.name,
                                        it.lastUpdated?.epochSecond)
                            })
                }
    }

}

class PublishedBotRanking(val botName: String,
                          val rating: Int,
                          val played: Int,
                          val won: Int,
                          val lost: Int,
                          val crashed: Int,
                          val race: String?,
                          val lastUpdated: Long?)
