package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class GameResultsPublisher(private val gameResultRepository: GameResultRepository,
                           private val publisher: Publisher,
                           private val maps: Maps,
                           private val config: Config) {

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("game_results.json")
                .use { out ->
                    writer.writeValue(out, gameResultRepository.findByTimeGreaterThan(Instant.now().minus(config.gameResultsHours, ChronoUnit.HOURS))
                            .map { gameResult ->
                                val resultA = gameResult.botA
                                val resultB = gameResult.botB
                                val botA = PublishedBotResult(
                                        resultA.name,
                                        resultA.race.name,
                                        resultA == gameResult.winner,
                                        resultA == gameResult.loser,
                                        gameResult.botACrashed
                                )
                                val botB = PublishedBotResult(
                                        resultB.name,
                                        resultB.race.name,
                                        resultB == gameResult.winner,
                                        resultB == gameResult.loser,
                                        gameResult.botBCrashed
                                )
                                PublishedGameResult(
                                        botA,
                                        botB,
                                        gameResult.winner != null,
                                        gameResult.realtimeTimeout,
                                        gameResult.frameTimeout ?: false,
                                        gameResult.time.epochSecond,
                                        maps.mapName(gameResult.map) ?: gameResult.map,
                                        gameResult.gameHash,
                                        gameResult.frameCount)
                            })
                }

    }

    class PublishedBotResult(val name: String,
                             val race: String?,
                             val winner: Boolean,
                             val loser: Boolean,
                             val crashed: Boolean)

    class PublishedGameResult(val botA: PublishedBotResult,
                              val botB: PublishedBotResult,
                              val validGame: Boolean,
                              val realTimeout: Boolean,
                              val frameTimeout: Boolean,
                              val endedAt: Long,
                              val map: String,
                              val gameHash: String,
                              val frameCount: Int?)

}