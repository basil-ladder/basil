package org.bytekeeper.ctr.proj

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.getBotsForUpdate
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
class BotProjections(private val botRepository: BotRepository,
                     private val events: Events) {
    private val log = LogManager.getLogger()

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
        events.post(EloUpdated(winner, newWinnerRating, time, gameWon.gameHash))
        events.post(EloUpdated(loser, newLoserRating, time, gameWon.gameHash))
    }

    @Transactional
    @EventHandler
    fun onGameCrashed(event: GameCrashed) {
        val (botA, botB) = botRepository.getBotsForUpdate(listOf(event.botA, event.botB))
        botA.played++
        botB.played++
        if (event.botACrashed) botA.crashed++
        if (event.botBCrashed) botB.crashed++
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
    fun handle(event: BotInfoUpdated) {
        val bot = botRepository.findByName(event.name)?.also { bot ->
            bot.enabled = event.enabled
            bot.lastUpdated = event.lastUpdated
            bot.publishRead = event.publishReadDirectory
            bot.authorKeyId = event.authorKey
        } ?: kotlin.run {
            log.info("Bot ${event.name} not yet registered, creating it.")
            Bot(null,
                    event.enabled,
                    event.name,
                    event.race,
                    event.botType,
                    event.lastUpdated,
                    event.publishReadDirectory,
                    event.authorKey)
        }
        botRepository.save(bot)
    }

    @Transactional
    @EventHandler
    fun handle(botUpdated: BotBinaryUpdated) {
        val bot = botRepository.getById(botUpdated.bot.id!!)
        bot.lastUpdated = botUpdated.timestamp
    }
}