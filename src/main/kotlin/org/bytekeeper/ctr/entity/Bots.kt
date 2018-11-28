package org.bytekeeper.ctr.entity

import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.*

enum class Race {
    ZERG,
    TERRAN,
    PROTOSS,
    RANDOM
}

@Entity
class BotElo(@Id @GeneratedValue var id: Long? = null,
             @ManyToOne val bot: Bot,
             var time: Instant,
             var rating: Int,
             var gameHash: String)

@Entity
class Bot(@Id @GeneratedValue var id: Long? = null,
          val enabled: Boolean = true,
          var name: String,
          @Enumerated(EnumType.STRING) var race: Race? = null,
          var botType: String? = null,
          var lastUpdated: Instant? = null,
          var played: Int = 0,
          var rating: Int = 2000,
          var crashed: Int = 0,
          var won: Int = 0,
          var lost: Int = 0)

interface BotRepository : CrudRepository<Bot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getById(id: Long): Bot

    fun findByName(name: String): Bot?

    fun findAllByEnabledTrue(): List<Bot>
    fun countByRace(race: Race): Int
}

interface BotEloRepository : CrudRepository<BotElo, Long> {
    fun findAllByBot(bot: Bot): List<BotElo>
}
