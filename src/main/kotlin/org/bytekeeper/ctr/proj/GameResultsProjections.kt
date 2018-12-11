package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.GameResult
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class GameResultsProjections(private val gameResultRepository: GameResultRepository,
                             private val events: Events) {

    @Transactional
    @EventHandler
    fun gameEnded(gameEnded: GameEnded) {
        gameResultRepository.save(GameResult(time = gameEnded.timestamp, winner = gameEnded.winner, loser = gameEnded.loser,
                gameRealtime = gameEnded.gameTime, botA = gameEnded.winner, botB = gameEnded.loser, map = gameEnded.map,
                gameHash = gameEnded.gameHash, frameCount = gameEnded.frameCount))
        events.post(GameWon(gameEnded.winner, gameEnded.loser, gameEnded.gameHash))
    }

    @Transactional
    @EventHandler
    fun gameCrashed(gameCrashed: GameCrashed) {
        val (winner, loser) =
                if (gameCrashed.botACrashed != gameCrashed.botBCrashed) {
                    (if (gameCrashed.botBCrashed) gameCrashed.botA else gameCrashed.botB) to
                            (if (gameCrashed.botBCrashed) gameCrashed.botB else gameCrashed.botA)
                } else null to null
        gameResultRepository.save(GameResult(
                time = gameCrashed.timestamp,
                botA = gameCrashed.botA,
                botB = gameCrashed.botB,
                winner = winner,
                loser = loser,
                gameRealtime = gameCrashed.gameTime,
                map = gameCrashed.map,
                botACrashed = gameCrashed.botACrashed,
                botBCrashed = gameCrashed.botBCrashed,
                gameHash = gameCrashed.gameHash,
                frameCount = gameCrashed.frameCount))
        if (winner != null && loser != null)
            events.post(GameWon(winner, loser, gameCrashed.gameHash))
    }

    @Transactional
    @EventHandler
    fun gameFailedToStart(gameFailedToStart: GameFailedToStart) {
        gameResultRepository.save(GameResult(time = gameFailedToStart.timestamp, realtimeTimeout = false,
                botA = gameFailedToStart.botA, botB = gameFailedToStart.botB, gameRealtime = 0.0, map = gameFailedToStart.map,
                botACrashed = true, botBCrashed = true, gameHash = gameFailedToStart.gameHash))
    }

    @Transactional
    @EventHandler
    fun gameTimedOut(gameTimedOut: GameTimedOut) {
        var winner: Bot? = null
        var loser: Bot? = null

        if (gameTimedOut.gameTimedOut && gameTimedOut.scoreA != gameTimedOut.scoreB) {
            if (gameTimedOut.scoreA > gameTimedOut.scoreB) {
                winner = gameTimedOut.botA
                loser = gameTimedOut.botB
            } else if (gameTimedOut.scoreA < gameTimedOut.scoreB) {
                winner = gameTimedOut.botB
                loser = gameTimedOut.botA
            }
        }

        if (gameTimedOut.slowerBot != null && winner == null && loser == null) {
            if (gameTimedOut.botA == gameTimedOut.slowerBot) {
                winner = gameTimedOut.botB
                loser = gameTimedOut.botA
            } else {
                winner = gameTimedOut.botA
                loser = gameTimedOut.botB
            }
        }
        if (winner != null && loser != null) events.post(GameWon(winner, loser, gameTimedOut.gameHash))

        gameResultRepository.save(GameResult(
                time = gameTimedOut.timestamp,
                realtimeTimeout = gameTimedOut.realTimedOut,
                frameTimeout = gameTimedOut.gameTimedOut,
                botA = if (winner != null) winner else gameTimedOut.botA,
                botB = if (loser != null) loser else gameTimedOut.botB,
                winner = winner,
                loser = loser,
                gameRealtime = gameTimedOut.gameTime,
                map = gameTimedOut.map,
                botACrashed = false,
                botBCrashed = false,
                gameHash = gameTimedOut.gameHash,
                frameCount = gameTimedOut.frameCount))
    }
}