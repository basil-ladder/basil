package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.EventHandler
import org.bytekeeper.ctr.GameCrashed
import org.bytekeeper.ctr.GameEnded
import org.bytekeeper.ctr.entity.GameResult
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class GameResultsProjections(private val gameResultRepository: GameResultRepository) {

    @Transactional
    @EventHandler
    fun gameEnded(gameEnded: GameEnded) {
        gameResultRepository.save(GameResult(time = gameEnded.timestamp, winner = gameEnded.winner, loser = gameEnded.loser,
                gameRealtime = gameEnded.gameTime, botA = gameEnded.winner, botB = gameEnded.loser, map = gameEnded.map,
                gameHash = gameEnded.gameHash))
    }

    @Transactional
    @EventHandler
    fun gameCrashed(gameCrashed: GameCrashed) {
        gameResultRepository.save(GameResult(time = gameCrashed.timestamp, realtimeTimeout = gameCrashed.realTimedOut,
                botA = gameCrashed.botA, botB = gameCrashed.botB, gameRealtime = gameCrashed.gameTime, map = gameCrashed.map,
                botACrashed = gameCrashed.botACrashed, botBCrashed = gameCrashed.botBCrashed, gameHash = gameCrashed.gameHash))
    }
}