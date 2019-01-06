package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.BotSources
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Component

@Component
class RankingsPublisher(private val publisher: Publisher,
                        private val botRepository: BotRepository,
                        private val botSources: BotSources) {

    @CommandHandler
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAll()
        publisher.globalStatsWriter("ranking.json")
                .use { out ->
                    writer.writeValue(out, allBots
                            .map {
                                val lastUpdated = botSources.botInfoOf(it.name)?.lastUpdated ?: it.lastUpdated

                                PublishedBotRanking(
                                        it.name,
                                        it.rating,
                                        it.played,
                                        it.won,
                                        it.lost,
                                        it.crashed,
                                        it.race?.name,
                                        lastUpdated?.epochSecond,
                                        it.enabled,
                                        it.disabledReason)
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
                          val lastUpdated: Long?,
                          val enabled: Boolean,
                          val disabledReason: String?)
