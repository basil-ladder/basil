package org.bytekeeper.ctr.proj

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.GameCrashed
import org.bytekeeper.ctr.GameEnded
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.GameResult
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class GameResultsProjections(private val gameResultRepository: GameResultRepository,
                             private val publisher: Publisher) {

    @Transactional
    fun gameEnded(gameEnded: GameEnded) {
        gameResultRepository.save(GameResult(time = gameEnded.timestamp, winner = gameEnded.winner, loser = gameEnded.loser,
                gameRealtime = gameEnded.gameTime, botA = gameEnded.winner, botB = gameEnded.loser, map = gameEnded.map,
                gameHash = gameEnded.gameHash))
    }

    @Transactional
    fun gameCrashed(gameCrashed: GameCrashed) {
        gameResultRepository.save(GameResult(time = gameCrashed.timestamp, realtimeTimeout = gameCrashed.realTimedOut,
                botA = gameCrashed.botA, botB = gameCrashed.botB, gameRealtime = gameCrashed.gameTime, map = gameCrashed.map,
                botACrashed = gameCrashed.botACrashed, botBCrashed = gameCrashed.botBCrashed, gameHash = gameCrashed.gameHash))
    }

    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("game results - all games.json")
                .use { out ->
                    writer.writeValue(out, gameResultRepository.findAll()
                            .map {
                                PublishedGameResult(it.botA.name, it.botB.name, it.winner?.name, it.loser?.name, it.realtimeTimeout, it.time.epochSecond,
                                        it.map, it.botACrashed, it.botBCrashed, it.gameHash)
                            })
                }

    }

    class PublishedGameResult(val botA: String, val botB: String, val winner: String?, val loser: String?, val realTimeout: Boolean, val endedAt: Long, val map: String,
                              val botACrashed: Boolean, val botBCrashed: Boolean, val gameHash: String)
}