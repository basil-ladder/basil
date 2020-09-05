package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.Publisher.Companion.bool2Short
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
        val writer = jacksonObjectMapper().writer().without(JsonGenerator.Feature.QUOTE_FIELD_NAMES)

        publisher.globalStatsWriter("games_24h.json")
                .use { out ->
                    val relevantGames = gameResultRepository.findByTimeGreaterThan(Instant.now().minus(config.gameResultsHours, ChronoUnit.HOURS))
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
                                    bots.entries.sortedBy { it.value }.map { (bot, _) -> PublishedBotInfo(bot.name, bot.race.short, bot.rank.short) },
                                    playedMaps.entries.sortedBy { it.value }.map {
                                        maps.getMap(it.key).mapName ?: it.key
                                    },
                                    relevantGames.map { gameResult ->
                                        val resultA = gameResult.botA
                                        val resultB = gameResult.botB
                                        val botA = PublishedBotResult(
                                                bots[gameResult.botA] ?: -1,
                                                if (gameResult.raceA != gameResult.botA.race) gameResult.raceA.short else null,
                                                bool2Short(resultA == gameResult.winner),
                                                bool2Short(resultA == gameResult.loser),
                                                bool2Short(gameResult.botACrashed)
                                        )
                                        val botB = PublishedBotResult(
                                                bots[gameResult.botB] ?: -1,
                                                if (gameResult.raceB != gameResult.botB.race) gameResult.raceB.short else null,
                                                bool2Short(resultB == gameResult.winner),
                                                bool2Short(resultB == gameResult.loser),
                                                bool2Short(gameResult.botBCrashed)
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
                                                bool2Short(gameResult.winner == null || gameResult.realtimeTimeout),
                                                bool2Short(gameResult.realtimeTimeout),
                                                bool2Short(gameResult.frameTimeout),
                                                (gameResult.time.epochSecond / 60).toString(16),
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
    data class PublishedBotInfo(val name: String, val race: String, val rank: String)

    class PublishedBotResult(@JsonProperty("b") val botIndex: Int,
                             @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("r") val race: String?,
                             @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("w") val winner: Short?,
                             @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("l") val loser: Short?,
                             @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("c") val crashed: Short?)

    class PublishedGameResult(@JsonProperty("a") val botA: PublishedBotResult,
                              @JsonProperty("b") val botB: PublishedBotResult,
                              @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("iv") val invalidGame: Short?,
                              @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("to") val realTimeout: Short?,
                              @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("fo") val frameTimeout: Short?,
                              @JsonProperty("e") val endedAt: String,
                              @JsonProperty("m") val map: Int,
                              @JsonProperty("h") val gameHash: String,
                              @JsonProperty("fc") val frameCount: Int?,
                              @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("ev") val gameEvents: List<PublishedGameEvent>?)

    data class PublishedGameEvent(@JsonProperty("u") val unit: Int,
                                  @JsonProperty("e") val event: Int,
                                  @JsonProperty("c") val amount: Long)
}