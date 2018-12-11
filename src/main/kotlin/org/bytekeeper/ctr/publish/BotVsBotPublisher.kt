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
                    val wonGames = gameResultRepository.listBotVsBotWonGames()
                    writer.writeValue(out, PublishedBotVsBot(wonGames
                            .flatMap { listOf(it.botA, it.botB) }
                            .distinct()
                            .sortedBy { it.name.toLowerCase() }
                            .map { PublishedBotinfo(it.name, it.race?.name) },
                            wonGames
                                    .map { PublishedBotVsBotStat(it.botA.name, it.botB.name, it.won) }
                    ))
                }
    }


    class PublishedBotinfo(val name: String, val race: String?)
    class PublishedBotVsBotStat(val winner: String, val loser: String, val won: Long)
    class PublishedBotVsBot(val botinfos: List<PublishedBotinfo>, val botVsBotStat: List<PublishedBotVsBotStat>)
}
