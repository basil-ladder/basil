package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.math.Elo
import org.bytekeeper.ctr.repository.BotHistory
import org.bytekeeper.ctr.repository.BotHistoryRepository
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.getBotsForUpdate
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
class BotProjections(private val botRepository: BotRepository,
                     private val botHistoryRepository: BotHistoryRepository,
                     private val events: Events) {

    @Transactional
    @EventHandler
    fun onGameEnded(gameEnded: GameEnded) {
        val (botA, botB) = botRepository.getBotsForUpdate(listOf(gameEnded.winner, gameEnded.loser))
        botA.played++
        botB.played++
    }

    @Transactional
    @EventHandler
    fun onGameWon(gameWon: GameWon) {
        val (winner, loser) = botRepository.getBotsForUpdate(listOf(gameWon.winner, gameWon.loser))
        winner.won++
        loser.lost++

        val (newWinnerRating, newLoserRating) = Elo.calculateElos(
                winner.rating, winner.played,
                loser.rating, loser.played)

        winner.rating = newWinnerRating
        loser.rating = newLoserRating

        val time = Instant.now()
        events.post(EloUpdated(winner, newWinnerRating, time, gameWon.game))
        events.post(EloUpdated(loser, newLoserRating, time, gameWon.game))
    }

    @Transactional
    @EventHandler
    fun onGameCrashed(event: GameCrashed) {
        val (botA, botB) = botRepository.getBotsForUpdate(listOf(event.botA, event.botB))
        botA.played++
        botB.played++
        if (event.botACrashed) {
            botA.crashed++
            botA.crashesSinceUpdate++
        }
        if (event.botBCrashed) {
            botB.crashed++
            botB.crashesSinceUpdate++
        }
    }

    @Transactional
    @EventHandler
    fun onGameFailedToStart(event: GameFailedToStart) {
        val (botA, botB) = botRepository.getBotsForUpdate(listOf(event.botA, event.botB))
        botA.played++
        botB.played++
        botA.crashed++
        botB.crashed++
    }

    @Transactional
    @EventHandler
    fun onGameTimedOut(event: GameTimedOut) {
        val (botA, botB) = botRepository.getBotsForUpdate(listOf(event.botA, event.botB))
        botA.played++
        botB.played++
    }

    @Transactional
    @EventHandler
    fun onBotUpdated(botUpdated: BotBinaryUpdated) {
        val bot = botRepository.getById(botUpdated.bot.id!!)
        bot.lastUpdated = botUpdated.timestamp
        bot.crashesSinceUpdate = 0

        botHistoryRepository.save(BotHistory(bot, botUpdated.timestamp))
    }
}