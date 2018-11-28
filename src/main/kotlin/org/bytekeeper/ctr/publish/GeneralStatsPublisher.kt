package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.GameResultRepository
import org.bytekeeper.ctr.entity.Race
import org.springframework.stereotype.Component


@Component
class GeneralStatsPublisher(private val gameResultRepository: GameResultRepository,
                            private val botsRepository: BotRepository,
                            private val publisher: Publisher) {
    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("stats.json")
                .use {
                    writer.writeValue(it,
                            PublishedStats(gameResultRepository.count(),
                                    botsRepository.countByRace(Race.TERRAN),
                                    botsRepository.countByRace(Race.ZERG),
                                    botsRepository.countByRace(Race.PROTOSS),
                                    botsRepository.countByRace(Race.RANDOM),
                                    gameResultRepository.countByWinnerRace(Race.TERRAN),
                                    gameResultRepository.countByWinnerRace(Race.ZERG),
                                    gameResultRepository.countByWinnerRace(Race.PROTOSS),
                                    gameResultRepository.countByWinnerRace(Race.RANDOM),
                                    gameResultRepository.countByBotACrashedIsTrueOrBotBCrashedIsTrue(),
                                    gameResultRepository.averageGameRealtime()
                            ))
                }
    }

    class PublishedStats(val gamesPlayed: Long,
                         val terranBots: Int,
                         val zergBots: Int,
                         val protossBots: Int,
                         val randomBots: Int,
                         val terranWins: Int,
                         val zergWins: Int,
                         val protossWins: Int,
                         val randomWins: Int,
                         val crashes: Int,
                         val averageGameRealtime: Double)
}