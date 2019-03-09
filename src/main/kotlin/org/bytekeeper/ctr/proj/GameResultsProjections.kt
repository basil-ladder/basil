package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.GameResult
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class GameResultsProjections(private val gameResultRepository: GameResultRepository,
                             private val events: Events) {

    @Transactional
    @EventHandler
    fun gameEnded(gameEnded: GameEnded) {
        val gameResult = gameResultRepository.save(GameResult(
                id = gameEnded.id,
                time = gameEnded.timestamp,
                winner = gameEnded.winner,
                loser = gameEnded.loser,
                gameRealtime = gameEnded.gameTime,
                botA = gameEnded.winner,
                raceA = gameEnded.winnerRace,
                botB = gameEnded.loser,
                raceB = gameEnded.loserRace,
                map = gameEnded.map,
                gameHash = gameEnded.gameHash,
                frameCount = gameEnded.frameCount))
        events.post(GameWon(gameResult, gameEnded.winner, gameEnded.loser))
    }

    @Transactional
    @EventHandler
    fun gameCrashed(gameCrashed: GameCrashed) {
        val (winner, loser) =
                if (gameCrashed.botACrashed != gameCrashed.botBCrashed) {
                    (if (gameCrashed.botBCrashed) gameCrashed.botA else gameCrashed.botB) to
                            (if (gameCrashed.botBCrashed) gameCrashed.botB else gameCrashed.botA)
                } else null to null
        val botA = winner ?: gameCrashed.botA
        val botB = loser ?: gameCrashed.botB
        val gameResult = gameResultRepository.save(GameResult(
                id = gameCrashed.id,
                time = gameCrashed.timestamp,
                botA = botA,
                raceA = if (botA == gameCrashed.botA) gameCrashed.botARace else gameCrashed.botBRace,
                botB = botB,
                raceB = if (botB == gameCrashed.botB) gameCrashed.botBRace else gameCrashed.botARace,
                winner = winner,
                loser = loser,
                gameRealtime = gameCrashed.gameTime,
                map = gameCrashed.map,
                botACrashed = if (botA == gameCrashed.botA) gameCrashed.botACrashed else gameCrashed.botBCrashed,
                botBCrashed = if (botB == gameCrashed.botB) gameCrashed.botBCrashed else gameCrashed.botACrashed,
                gameHash = gameCrashed.gameHash,
                frameCount = gameCrashed.frameCount))
        if (winner != null && loser != null)
            events.post(GameWon(gameResult, winner, loser))
    }

    @Transactional
    @EventHandler
    fun gameFailedToStart(gameFailedToStart: GameFailedToStart) {
        gameResultRepository.save(GameResult(
                id = gameFailedToStart.id,
                time = gameFailedToStart.timestamp,
                realtimeTimeout = false,
                botA = gameFailedToStart.botA,
                raceA = gameFailedToStart.botARace,
                botB = gameFailedToStart.botB,
                raceB = gameFailedToStart.botBRace,
                gameRealtime = 0.0,
                map = gameFailedToStart.map,
                botACrashed = true,
                botBCrashed = true,
                gameHash = gameFailedToStart.gameHash))
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
        val botA = winner ?: gameTimedOut.botA
        val botB = loser ?: gameTimedOut.botB

        val gameResult = gameResultRepository.save(GameResult(
                id = gameTimedOut.id,
                time = gameTimedOut.timestamp,
                realtimeTimeout = gameTimedOut.realTimedOut,
                frameTimeout = gameTimedOut.gameTimedOut,
                botA = botA,
                raceA = if (gameTimedOut.botA == botA) gameTimedOut.botARace else gameTimedOut.botBRace,
                botB = botB,
                raceB = if (gameTimedOut.botB == botB) gameTimedOut.botBRace else gameTimedOut.botARace,
                winner = winner,
                loser = loser,
                gameRealtime = gameTimedOut.gameTime,
                map = gameTimedOut.map,
                botACrashed = false,
                botBCrashed = false,
                gameHash = gameTimedOut.gameHash,
                frameCount = gameTimedOut.frameCount))
        if (winner != null && loser != null) events.post(GameWon(gameResult, winner, loser))
    }
}