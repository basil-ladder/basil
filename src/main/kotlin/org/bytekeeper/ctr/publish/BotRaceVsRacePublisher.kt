package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
import org.springframework.stereotype.Component

@Component
class BotRaceVsRacePublisher(private val gameResultRepository: GameResultRepository,
                             private val publisher: Publisher) {
    private val log = LogManager.getLogger()

    @CommandHandler
    @Timed
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        gameResultRepository.listBotRaceVsRace()
                .groupBy { it.bot }
                .map {
                    it.key to it.value.sortedWith(compareBy({ it.race }, { it.enemyRace }))
                            .map { PublishedBotRaceVsRace(it.race, it.enemyRace, it.won, it.lost) }
                }
                .forEach {
                    val (bot, record) = it;
                    publisher.botStatsWriter(bot.name, "botRaceVsRace.json")
                            .use { out ->
                                writer.writeValue(out, record)
                            }
                }
    }

    class PublishedBotRaceVsRace(val race: Race, val enemyRace: Race, val won: Long, val lost: Long)
}