package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.EloUpdated
import org.bytekeeper.ctr.EventHandler
import org.bytekeeper.ctr.repository.BotElo
import org.bytekeeper.ctr.repository.BotEloRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class EloProjections(private val botEloRepository: BotEloRepository) {

    @Transactional
    @EventHandler
    fun onEloUpdated(event: EloUpdated) {
        botEloRepository.save(BotElo(bot = event.bot, time = event.timestamp, rating = event.newRating, game = event.game))
    }

}
