package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.stereotype.Component

@Component
class RankingsPublisher(private val publisher: Publisher,
                        private val botRepository: BotRepository,
                        private val botSources: BotSources,
                        private val config: Config) {

    @CommandHandler
    @Timed
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
                                        it.crashesSinceUpdate,
                                        it.race.name,
                                        lastUpdated?.epochSecond,
                                        it.enabled,
                                        it.disabledReason,
                                        listOf(SCMapPool.poolSscait.name) + it.mapPools(),
                                        it.rank.toString(),
                                        it.previousRank.toString(),
                                        it.rankSince)
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
                          val crashesSinceUpdate: Int,
                          val race: String?,
                          val lastUpdated: Long?,
                          val enabled: Boolean,
                          val disabledReason: String?,
                          val mapPools: List<String>,
                          val rank: String,
                          val previousRanking: String,
                          val rankSince: Int)
