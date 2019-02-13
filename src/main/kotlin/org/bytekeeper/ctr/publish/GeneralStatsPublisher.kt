package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.GameRunner
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.GameResultRepository
import org.bytekeeper.ctr.entity.Race
import org.springframework.stereotype.Component


@Component
class GeneralStatsPublisher(private val gameRunner: GameRunner,
                            private val gameResultRepository: GameResultRepository,
                            private val botRepository: BotRepository,
                            private val publisher: Publisher) {
    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("stats.json")
                .use {
                    val raceCrossCable = RaceCrossTable(
                            vsRow(Race.TERRAN),
                            vsRow(Race.PROTOSS),
                            vsRow(Race.ZERG),
                            vsRow(Race.RANDOM))

                    writer.writeValue(it,
                            PublishedStats(gameRunner.nextBotUpdateTime,
                                    gameResultRepository.count(),
                                    botRepository.countByRace(Race.TERRAN),
                                    botRepository.countByRace(Race.ZERG),
                                    botRepository.countByRace(Race.PROTOSS),
                                    botRepository.countByRace(Race.RANDOM),
                                    gameResultRepository.countByBotACrashedIsTrueOrBotBCrashedIsTrue(),
                                    gameResultRepository.averageGameRealtime(),
                                    raceCrossCable
                            ))
                }
    }

    private fun vsRow(winner: Race): VsRow =
            VsRow(gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.TERRAN),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.PROTOSS),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.ZERG),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.RANDOM))

    class PublishedStats(val nextUpdateTime: Long,
                         val gamesPlayed: Long,
                         val terranBots: Int,
                         val zergBots: Int,
                         val protossBots: Int,
                         val randomBots: Int,
                         val crashes: Int,
                         val averageGameRealtime: Double,
                         val raceCrossTable: RaceCrossTable
    )

    class RaceCrossTable(val terran: VsRow, val protoss: VsRow, val zerg: VsRow, val random: VsRow)

    class VsRow(val terran: Int, val protoss: Int, val zerg: Int, val random: Int)
}