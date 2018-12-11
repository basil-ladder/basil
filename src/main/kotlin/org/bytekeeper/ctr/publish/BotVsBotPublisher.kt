package org.bytekeeper.ctr.publish;

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component;

@Component
class BotVsBotPublisher(private val gameResultRepository: GameResultRepository,
                        private val publisher: Publisher) {
    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()
        publisher.globalStatsWriter("botVsBot.json")
                .use { out ->
                    writer.writeValue(out, gameResultRepository.listBotVsBotWonGames().map { PublishedBotVsBot(it.botA.name, it.botB.name, it.won) })
                }
    }

    class PublishedBotVsBot(val winner: String, val loser: String, val won: Long)
}
