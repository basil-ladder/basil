package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.hibernate.Hibernate
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.util.*
import java.util.stream.Stream
import javax.persistence.*

@Entity
data class GameResult(
    @Id val id: UUID,
    val time: Instant,
    val gameRealtime: Double,
    val realtimeTimeout: Boolean = false,
    val frameTimeout: Boolean? = false,
    val mapPool: String,
    val map: String,
    @ManyToOne(fetch = FetchType.LAZY) val botA: Bot,
    @Enumerated(EnumType.STRING) val raceA: Race,
    @ManyToOne(fetch = FetchType.LAZY) val botB: Bot,
    @Enumerated(EnumType.STRING) val raceB: Race,
    @ManyToOne(fetch = FetchType.LAZY) val winner: Bot? = null,
    @ManyToOne(fetch = FetchType.LAZY) val loser: Bot? = null,
    val botACrashed: Boolean = false,
    val botBCrashed: Boolean = false,
    val gameHash: String,
    val frameCount: Int? = null
)

class BotVsBotWonGames(
    val botA: Bot,
    val botB: Bot,
    val won: Long
) {
    init {
        Hibernate.initialize(botA)
        Hibernate.initialize(botB)
    }
}

class BotStat(val bot: Bot, val won: Long, val lost: Long) {
    init {
        Hibernate.initialize(bot)
    }
}

interface BS {
    val bot: Long
    val won: Long
    val lost: Long
}

data class MapRaceWinStat(val race: Race, val map: String, val won: Long)

data class BotGameResult(
    val time: Instant,
    val frameCount: Int?,
    val bot: String,
    val botRace: Race,
    val enemy: String,
    val enemyRace: Race,
    val won: Boolean,
    val map: String
)

interface GameResultRepository : CrudRepository<GameResult, Long> {
    @EntityGraph(attributePaths = ["botA", "botB", "winner", "loser"])
    @Timed
    fun findByTimeGreaterThan(time: Instant): List<GameResult>

    @Timed
    fun countByWinnerRaceAndLoserRace(winner: Race, loser: Race): Int

    @Timed
    fun countByBotACrashedIsTrueOrBotBCrashedIsTrue(): Int

    @Query("SELECT AVG(g.gameRealtime) FROM GameResult g WHERE g.winner <> NULL")
    @Timed
    fun averageGameRealtime(): Double

    @Query(
        """SELECT new org.bytekeeper.ctr.repository.BotVsBotWonGames(winner, loser, COUNT(r))
        FROM GameResult r
        JOIN r.winner winner
        JOIN r.loser loser
        WHERE r.time >= ?1
        GROUP BY winner, loser"""
    )
    @Timed
    fun listBotVsBotWonGames(after: Instant): List<BotVsBotWonGames>

    @Query(
        "SELECT new org.bytekeeper.ctr.repository.BotStat(bot, SUM(CASE WHEN (r.winner = bot) THEN 1 ELSE 0 END)," +
                " SUM(CASE WHEN (r.loser = bot) THEN 1 else 0 END)) FROM GameResult r, Bot bot WHERE r.time > bot.lastUpdated AND (r.winner = bot OR r.loser = bot) AND bot.enabled = TRUE" +
                " GROUP BY bot"
    )
    @Timed
    fun gamesSinceLastUpdate(): List<BotStat>

    @Query(
        "SELECT new org.bytekeeper.ctr.repository.BotGameResult(r.time, r.frameCount, b.name," +
                " CASE WHEN b = r.winner THEN r.raceA else r.raceB END," +
                " CASE WHEN b = r.winner THEN r.loser.name else r.winner.name END," +
                " CASE WHEN b = r.winner THEN r.raceB else r.raceA END, b = r.winner, r.map)" +
                " FROM GameResult r join Bot b on r.winner = b or r.loser = b ORDER BY b, r.time"
    )
    @Timed
    fun findAllGamesSummarized(): Stream<BotGameResult>

    @Query(
        "SELECT new org.bytekeeper.ctr.repository.MapRaceWinStat(r.raceA, r.map, count(*))" +
                " FROM GameResult r where r.winner = r.botA" +
                " GROUP BY r.map, r.raceA"
    )
    @Timed
    fun mapRaceWinStats(): List<MapRaceWinStat>

    @Timed
    @Query(
        nativeQuery = true, value = """select b.id as bot, 
        count(*) FILTER (WHERE x.winner_id = b.id) as won, 
        count(*) FILTER (WHERE x.loser_id = b.id) as lost 
        from (select * from game_result where time > ?1) x 
        join bot b on x.winner_id = b.id or x.loser_id = b.id group by b.id"""
    )
    fun listWinsAndLosses(after: Instant): List<BS>
}