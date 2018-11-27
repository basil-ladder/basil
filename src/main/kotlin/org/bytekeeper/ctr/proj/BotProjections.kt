package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
class BotProjections(private val botService: BotService,
                     private val events: Events) {
    @Transactional
    @EventHandler
    fun onGameEnded(gameEnded: GameEnded) {
        val (winnerBot, loserBot) = botService.getBotsForUpdate(listOf(gameEnded.winner, gameEnded.loser))
        handleAsCompleted(winnerBot, loserBot, gameEnded.gameHash)
    }

    private fun handleAsCompleted(winnerBot: Bot, loserBot: Bot, gameHash: String) {
        winnerBot.played++
        winnerBot.won++
        loserBot.played++
        loserBot.lost++

        val (newWinnerRating, newLoserRating) = Elo.calculateElos(
                winnerBot.rating, winnerBot.played,
                loserBot.rating, loserBot.played)

        winnerBot.rating = newWinnerRating
        loserBot.rating = newLoserRating

        val time = Instant.now()
        events.post(EloUpdated(winnerBot, newWinnerRating, time, gameHash))
        events.post(EloUpdated(loserBot, newLoserRating, time, gameHash))
    }

    @Transactional
    @EventHandler
    fun onGameCrashed(event: GameCrashed) {
        val (botA, botB) = botService.getBotsForUpdate(listOf(event.botA, event.botB))
        if (event.botACrashed == event.botBCrashed) {
            botA.played++
            botB.played++
            if (event.botACrashed) botA.crashed++
            if (event.botBCrashed) botB.crashed++
        } else {
            handleAsCompleted(if (event.botACrashed) botB else botA,
                    if (event.botBCrashed) botB else botA, event.gameHash)
        }
    }

    @Transactional
    @CommandHandler
    fun handle(command: CreateBot) {
        val bot = botService.save(Bot(name = command.name, race = command.race, botType = command.botType, lastUpdated = command.lastUpdated))
        events.post(BotCreated(bot))
    }
}