package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class RankingsPublisher(
    private val publisher: Publisher,
    private val botRepository: BotRepository,
    private val gameResultRepository: GameResultRepository,
    private val botSources: BotSources,
    private val config: Config
) {
    private val log = LogManager.getLogger()

    @CommandHandler
    @Timed
    fun handle(preparePublish: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        val allBots = botRepository.findAll()
        val botStats = gameResultRepository.listWinsAndLosses(Instant.now() - Duration.ofDays(7))
            .map { it.bot to Pair(it.won, it.lost) }.toMap()
        publisher.globalStatsWriter("ranking.json")
            .use { out ->
                writer.writeValue(
                    out, allBots
                        .map {
                            val lastUpdated = botSources.botInfoOf(it.name)?.lastUpdated ?: it.lastUpdated
                            val lastUpdatedEpoch = lastUpdated?.epochSecond
                            if (lastUpdatedEpoch != null && lastUpdatedEpoch < 0) {
                                log.warn("Update timestamp of '{it.name}' is invalid: {lastUpdated} -> {lastUpdatedEpoch}")
                            }
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
                                it.isRankLocked(config.ranking),
                                botStats[it.id]?.first ?: 0,
                                botStats[it.id]?.second ?: 0
                            )
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
                          val previousRank: String,
                          val rankProtection: Boolean,
                          val wonInWindow: Long,
                          val lostInWindow: Long
)
