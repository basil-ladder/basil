package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class GameResult(@Id @GeneratedValue var id: Long? = null,
                 var time: Instant,
                 var gameRealtime: Double,
                 var realtimeTimeout: Boolean = false,
                 var frameTimeout: Boolean? = false,
                 var map: String,
                 @ManyToOne var botA: Bot,
                 @ManyToOne var botB: Bot,
                 @ManyToOne var winner: Bot? = null,
                 @ManyToOne var loser: Bot? = null,
                 var botACrashed: Boolean = false,
                 var botBCrashed: Boolean = false,
                 var gameHash: String,
                 var frameCount: Int? = null)

class BotVsBotWonGames(val botA: Bot,
                       val botB: Bot,
                       val won: Long)

class BotStat(val bot: Bot, val won: Long, val lost: Long)
class MapStat(val map: String, val won: Long, val lost: Long)

interface GameResultRepository : CrudRepository<GameResult, Long> {
    @EntityGraph(attributePaths = ["botA", "botB", "winner", "loser"])
    @Timed
    fun findByTimeGreaterThan(time: Instant): MutableIterable<GameResult>

    @Timed
    fun countByWinnerRaceAndLoserRace(winner: Race, loser: Race): Int

    @Timed
    fun countByBotACrashedIsTrueOrBotBCrashedIsTrue(): Int
    @Query("SELECT AVG(g.gameRealtime) FROM GameResult g WHERE g.winner <> NULL")
    @Timed
    fun averageGameRealtime(): Double

    @Query("SELECT new org.bytekeeper.ctr.repository.BotVsBotWonGames(r.winner, r.loser, COUNT(r)) FROM GameResult r GROUP BY r.winner, r.loser")
    @Timed
    fun listBotVsBotWonGames(): List<BotVsBotWonGames>

    @Query("SELECT new org.bytekeeper.ctr.repository.BotStat(bot, SUM(CASE WHEN (r.winner = bot) THEN 1 ELSE 0 END)," +
            " SUM(CASE WHEN (r.loser = bot) THEN 1 else 0 END)) FROM GameResult r, Bot bot WHERE r.time > bot.lastUpdated AND (r.winner = bot OR r.loser = bot) AND bot.enabled = TRUE" +
            " GROUP BY bot")
    @Timed
    fun gamesSinceLastUpdate(): List<BotStat>

    @Query("SELECT new org.bytekeeper.ctr.repository.MapStat(r.map, SUM(CASE WHEN (r.winner = ?1) THEN 1 ELSE 0 END)," +
            " SUM(CASE WHEN (r.loser = ?1) THEN 1 else 0 END)) FROM GameResult r WHERE r.winner = ?1 OR r.loser = ?1" +
            " GROUP BY map")
    @Timed
    fun botStatsPerMap(bot: Bot): List<MapStat>
}