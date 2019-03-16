package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.BotUpdater
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.*
import org.springframework.stereotype.Component


@Component
class GeneralStatsPublisher(private val botUpdater: BotUpdater,
                            private val gameResultRepository: GameResultRepository,
                            private val botRepository: BotRepository,
                            private val unitEventsRepository: UnitEventsRepository,
                            private val publisher: Publisher) {
    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("stats.json")
                .use {
                    val raceCrossTable = RaceCrossTable(
                            vsRow(Race.TERRAN),
                            vsRow(Race.PROTOSS),
                            vsRow(Race.ZERG),
                            vsRow(Race.RANDOM))

                    val unitStats = unitEventsRepository.globalUnitStats().asSequence()
                            .filter { it.name != "Terran_Siege_Tank_Siege_Mode" && !it.name.startsWith("Spell") }
                            .groupBy(UnitStats::name).entries
                            .map { (name, stats) ->
                                val name = if (name == "Terran_Siege_Tank_Tank_Mode") "Terran Siege Tank"
                                else name.replace('_', ' ')
                                val stats = stats.groupBy(UnitStats::event)
                                val created = (stats[UnitEventType.UNIT_CREATE]
                                        ?: stats[UnitEventType.UNIT_MORPH])?.get(0)?.amount ?: -1
                                PublishedUnitStats(name, created, stats[UnitEventType.UNIT_DESTROY]?.get(0)?.amount
                                        ?: 0)
                            }.sortedBy { it.name }
                    writer.writeValue(it,
                            PublishedStats(botUpdater.nextBotUpdateTime,
                                    gameResultRepository.count(),
                                    botRepository.countByRace(Race.TERRAN),
                                    botRepository.countByRace(Race.ZERG),
                                    botRepository.countByRace(Race.PROTOSS),
                                    botRepository.countByRace(Race.RANDOM),
                                    gameResultRepository.countByBotACrashedIsTrueOrBotBCrashedIsTrue(),
                                    gameResultRepository.averageGameRealtime(),
                                    raceCrossTable,
                                    unitStats


                            ))
                }
    }

    private fun vsRow(winner: Race): VsRow =
            VsRow(gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.TERRAN),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.PROTOSS),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.ZERG),
                    gameResultRepository.countByWinnerRaceAndLoserRace(winner, Race.RANDOM))

    data class PublishedStats(val nextUpdateTime: Long,
                              val gamesPlayed: Long,
                              val terranBots: Int,
                              val zergBots: Int,
                              val protossBots: Int,
                              val randomBots: Int,
                              val crashes: Int,
                              val averageGameRealtime: Double,
                              val raceCrossTable: RaceCrossTable,
                              val unitStats: List<PublishedUnitStats>)

    data class PublishedUnitStats(val name: String, val created: Long, val destroyed: Long)

    data class RaceCrossTable(val terran: VsRow, val protoss: VsRow, val zerg: VsRow, val random: VsRow)

    data class VsRow(val terran: Int, val protoss: Int, val zerg: Int, val random: Int)
}