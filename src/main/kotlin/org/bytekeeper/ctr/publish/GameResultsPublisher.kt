package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class GameResultsPublisher(private val gameResultRepository: GameResultRepository,
                           private val publisher: Publisher,
                           private val maps: Maps,
                           private val config: Config) {

    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("game_results.json")
                .use { out ->
                    writer.writeValue(out, gameResultRepository.findByTimeGreaterThan(Instant.now().minus(config.gameResultsHours, ChronoUnit.HOURS))
                            .map {
                                PublishedGameResult(it.botA.name, it.botA.race?.name, it.botB.name, it.botB.race?.name, it.winner?.name, it.loser?.name, it.realtimeTimeout, it.time.epochSecond,
                                        maps.mapName(it.map) ?: it.map, it.botACrashed, it.botBCrashed, it.gameHash)
                            })
                }

    }

    class PublishedGameResult(val botA: String, val botARace: String?, val botB: String, val botBRace: String?, val winner: String?, val loser: String?, val realTimeout: Boolean, val endedAt: Long, val map: String,
                              val botACrashed: Boolean, val botBCrashed: Boolean, val gameHash: String)

}