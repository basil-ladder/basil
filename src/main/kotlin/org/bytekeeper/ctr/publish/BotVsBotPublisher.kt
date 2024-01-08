package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class BotVsBotPublisher(private val gameResultRepository: GameResultRepository,
                        private val publisher: Publisher) {
    private val log = LogManager.getLogger()

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()
        for (daysBeforeNow in 30L..180 step 30) {
            publish(writer, daysBeforeNow)
        }
        publish(writer)
        publish(writer, 7)
        publish(writer, 14)
    }

    private fun publish(writer: ObjectWriter, daysBeforeNow: Long? = null) {
        val after = daysBeforeNow?.let { Instant.now() - Duration.ofDays(it) } ?: Instant.EPOCH
        val wonGames = gameResultRepository.listBotVsBotWonGames(after)
        val sortedBotList = wonGames
                .flatMap { listOf(it.botA, it.botB) }
                .distinct()
                .sortedByDescending { it.rating }
        val suffix = if (daysBeforeNow != null) "_$daysBeforeNow" else ""
        publisher.globalStatsWriter("botVsBot$suffix.json")
                .use { out ->
                    val botVsBotWinsMap = wonGames.map { (it.botA to it.botB) to it.won }.toMap()
                    writer.writeValue(out, PublishedBotVsBot(
                            sortedBotList.map {
                                PublishedBotinfo(it.name, it.race.name, it.rating, it.enabled,
                                        sortedBotList.map { enemy -> botVsBotWinsMap[it to enemy] ?: 0 }
                                )
                            }
                    ))
                }
        publisher.globalStatsWriter("botVsBot$suffix.csv")
                .use { out ->
                    val wonMap = wonGames.map { Coord(it.botA.name, it.botB.name) to it.won }.toMap()

                    out.append((listOf("Bot", "ELO") + sortedBotList.map { it.name }).joinToString(", "))
                    out.newLine()
                    for (botA in sortedBotList) {
                        val row = (listOf(botA.name, botA.rating) + sortedBotList.map { botB ->
                            wonMap[Coord(botA.name, botB.name)] ?: "0"
                        })
                                .joinToString()
                        out.append(row)
                        out.newLine()
                    }

                }
    }

    data class Coord(val botA: String, val botB: String)
    class PublishedBotinfo(val name: String, val race: String?, val rating: Int, val enabled: Boolean, val vsBotIdxWon: List<Long>)
    class PublishedBotVsBot(val botinfos: List<PublishedBotinfo>)
}
