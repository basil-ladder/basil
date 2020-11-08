package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.UnitEventsRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class GameResultsPublisher(private val gameResultRepository: GameResultRepository,
                           private val unitEventsRepository: UnitEventsRepository,
                           private val publisher: Publisher,
                           private val maps: Maps,
                           private val config: Config) {

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("games_24h.json.gz")
                .use { out ->
                    val relevantGames = gameResultRepository.findByTimeGreaterThan(Instant.now().minus(config.gameResultsHours * 5, ChronoUnit.HOURS))
                    if (relevantGames.isEmpty()) return
                    val relevantGameEvents = unitEventsRepository.aggregateGameEventsWith8OrMoreEvents(relevantGames)
                            .groupBy { it.game }
                    val bots = relevantGames.flatMap { listOf(it.botA, it.botB) }
                            .distinct()
                            .mapIndexed { index, bot -> bot to index }
                            .toMap()
                    val playedMaps = relevantGames.map { it.map }
                            .distinct()
                            .mapIndexed { index, map -> map to index }
                            .toMap()

                    writer.writeValue(out,
                            PublishedGameList(
                                    bots.entries.sortedBy { it.value }.map { (bot, _) -> PublishedBotInfo(bot.name, bot.race.short, bot.rank.short, bot.rating) },
                                    playedMaps.entries.sortedBy { it.value }.map {
                                        maps.getMap(it.key).mapName ?: it.key
                                    },
                                    relevantGames.map { gameResult ->
                                        val resultA = gameResult.botA
                                        val resultB = gameResult.botB
                                        val botA = PublishedBotResult(
                                                bots[gameResult.botA] ?: -1,
                                                gameResult.raceA.short,
                                                resultA == gameResult.winner,
                                                resultA == gameResult.loser,
                                                gameResult.botACrashed
                                        )
                                        val botB = PublishedBotResult(
                                                bots[gameResult.botB] ?: -1,
                                                gameResult.raceB.short,
                                                resultB == gameResult.winner,
                                                resultB == gameResult.loser,
                                                gameResult.botBCrashed
                                        )
                                        val gameEvents = relevantGameEvents[gameResult.id]
                                                ?.filter {
                                                    when (it.unitType) {
                                                        UnitType.TERRAN_BUNKER, UnitType.PROTOSS_DARK_ARCHON,
                                                        UnitType.TERRAN_NUCLEAR_MISSILE, UnitType.TERRAN_BATTLECRUISER,
                                                        UnitType.ZERG_GUARDIAN, UnitType.ZERG_QUEEN, UnitType.ZERG_DEFILER,
                                                        UnitType.PROTOSS_ARBITER, UnitType.PROTOSS_CARRIER -> it.amount >= 10
                                                        UnitType.PROTOSS_REAVER, UnitType.PROTOSS_OBSERVER, UnitType.ZERG_ULTRALISK,
                                                        UnitType.ZERG_HATCHERY, UnitType.ZERG_LAIR, UnitType.ZERG_HIVE,
                                                        UnitType.PROTOSS_PHOTON_CANNON, UnitType.ZERG_SUNKEN_COLONY, UnitType.ZERG_SPORE_COLONY,
                                                        UnitType.TERRAN_BARRACKS, UnitType.TERRAN_MISSILE_TURRET, UnitType.PROTOSS_PYLON,
                                                        UnitType.PROTOSS_GATEWAY, UnitType.PROTOSS_ARCHON -> it.amount >= 30
                                                        UnitType.TERRAN_SIEGE_TANK_SIEGE_MODE,
                                                        UnitType.ZERG_LARVA, UnitType.ZERG_EGG, UnitType.ZERG_LURKER_EGG -> false
                                                        else -> it.amount >= 110
                                                    }
                                                }?.map { PublishedGameEvent(it.unitType.ordinal, it.event.ordinal, it.amount) }
                                                ?: emptyList()
                                        PublishedGameResult(
                                                botA,
                                                botB,
                                                gameResult.winner == null || gameResult.realtimeTimeout,
                                                gameResult.realtimeTimeout,
                                                gameResult.frameTimeout ?: false,
                                                gameResult.time.epochSecond,
                                                playedMaps[gameResult.map] ?: -1,
                                                gameResult.gameHash,
                                                gameResult.frameCount,
                                                if (gameEvents.isNotEmpty()) gameEvents else null)
                                    }
                            )
                    )
                }

    }

    data class PublishedGameList(val bots: List<PublishedBotInfo>, val maps: List<String>, val results: List<PublishedGameResult>)
    data class PublishedBotInfo(val name: String, val race: String, val rank: String, val rating: Int)

    class PublishedBotResult(val botIndex: Int,
                             val race: String,
                             val winner: Boolean,
                             val loser: Boolean,
                             val crashed: Boolean)

    class PublishedGameResult(val botA: PublishedBotResult,
                              val botB: PublishedBotResult,
                              val invalidGame: Boolean,
                              val realTimeout: Boolean,
                              val frameTimeout: Boolean,
                              val endedAt: Long,
                              val mapIndex: Int,
                              val gameHash: String,
                              val frameCount: Int?,
                              val gameEvents: List<PublishedGameEvent>?)

    data class PublishedGameEvent(val unit: Int,
                                  val event: Int,
                                  val amount: Long)
}