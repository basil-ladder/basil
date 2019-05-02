package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.*

enum class Race(val short: String) {
    ZERG("Z"),
    TERRAN("T"),
    PROTOSS("P"),
    RANDOM("R");
}

@Entity
class BotElo(@Id @GeneratedValue var id: Long? = null,
             @ManyToOne(fetch = FetchType.LAZY) val bot: Bot,
             val time: Instant,
             val rating: Int,
             @ManyToOne(fetch = FetchType.LAZY) val game: GameResult)

@Entity
class Bot(@Id @GeneratedValue var id: Long? = null,
          var enabled: Boolean = true,
          var disabledReason: String? = null,
          var name: String,
          @Enumerated(EnumType.STRING) var race: Race,
          var botType: String,
          var lastUpdated: Instant? = null,
          var publishRead: Boolean = false,
          var authorKeyId: String? = null,
          var played: Int = 0,
          var rating: Int = 2000,
          var crashed: Int = 0,
          var crashesSinceUpdate: Int = 0,
          var won: Int = 0,
          var lost: Int = 0,
          var mapPools: String = "") {
    fun mapPools() = mapPools.split(",").filter { it.isNotBlank() }
}

@Entity
class BotHistory(@ManyToOne(fetch = FetchType.LAZY) val bot: Bot,
                 val time: Instant,
                 val mapPools: String) {
    @Id
    @GeneratedValue
    var id: Long? = null
}

interface BotRepository : CrudRepository<Bot, Long> {
    @Timed
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getById(id: Long): Bot

    @Timed
    fun findByName(name: String): Bot?

    @Timed
    fun findAllByEnabledTrue(): List<Bot>

    @Timed
    fun findAllByEnabledTrueAndPublishReadTrue(): List<Bot>

    @Timed
    fun countByRace(race: Race): Int
}

fun BotRepository.getBotsForUpdate(bots: List<Bot>) =
        bots.mapIndexed { index, bot -> index to bot }
                .sortedBy { it.second.name }
                .map {
                    it.first to getById(it.second.id!!)
                }
                .sortedBy { it.first }
                .map { it.second }



interface BotEloRepository : CrudRepository<BotElo, Long> {
    @Timed
    fun findAllByBot(bot: Bot): List<BotElo>
}


interface BotHistoryRepository : CrudRepository<BotHistory, Long>