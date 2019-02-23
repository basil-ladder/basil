package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component

@Component
class BotVsBotPublisher(private val gameResultRepository: GameResultRepository,
                        private val publisher: Publisher) {
    private val log = LogManager.getLogger()

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()
        val wonGames = gameResultRepository.listBotVsBotWonGames()
        val sortedBotList = wonGames
                .flatMap { listOf(it.botA, it.botB) }
                .distinct()
                .sortedByDescending { it.rating }
        publisher.globalStatsWriter("botVsBot.json")
                .use { out ->
                    writer.writeValue(out, PublishedBotVsBot(
                            sortedBotList.map { PublishedBotinfo(it.name, it.race?.name, it.rating, it.enabled) },
                            wonGames
                                    .map { PublishedBotVsBotStat(it.botA.name, it.botB.name, it.won) }
                    ))
                }
        publisher.globalStatsWriter("botVsBot.csv")
                .use { out ->
                    val wonMap = wonGames.map { Coord(it.botA.name, it.botB.name) to it.won }.toMap()

                    out.append((listOf("Bot", "ELO") + sortedBotList.map { it.name }).joinToString(", "))
                    out.newLine()
                    for (botA in sortedBotList) {
                        val row = (listOf(botA.name, botA.rating) + sortedBotList.map { botB ->
                            wonMap[Coord(botA.name, botB.name)] ?: "0"
                        })
                                .joinToString(", ")
                        out.append(row)
                        out.newLine()
                    }

                }
    }

    data class Coord(val botA: String, val botB: String)
    class PublishedBotinfo(val name: String, val race: String?, val rating: Int, val enabled: Boolean)
    class PublishedBotVsBotStat(val winner: String, val loser: String, val won: Long)
    class PublishedBotVsBot(val botinfos: List<PublishedBotinfo>, val botVsBotStat: List<PublishedBotVsBotStat>)
}
