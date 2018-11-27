package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.GameResultRepository
import org.springframework.stereotype.Component

@Component
class GameResultsPublisher(private val gameResultRepository: GameResultRepository,
                           private val publisher: Publisher) {

    @CommandHandler
    fun handle(command: PreparePublish) {
        val writer = jacksonObjectMapper().writer()

        publisher.globalStatsWriter("game results - all games.json")
                .use { out ->
                    writer.writeValue(out, gameResultRepository.findAll()
                            .map {
                                PublishedGameResult(it.botA.name, it.botB.name, it.winner?.name, it.loser?.name, it.realtimeTimeout, it.time.epochSecond,
                                        it.map, it.botACrashed, it.botBCrashed, it.gameHash)
                            })
                }

    }

    class PublishedGameResult(val botA: String, val botB: String, val winner: String?, val loser: String?, val realTimeout: Boolean, val endedAt: Long, val map: String,
                              val botACrashed: Boolean, val botBCrashed: Boolean, val gameHash: String)

}