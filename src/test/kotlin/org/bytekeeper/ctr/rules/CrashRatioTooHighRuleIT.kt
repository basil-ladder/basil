package org.bytekeeper.ctr.rules

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.BotService
import org.bytekeeper.ctr.Config
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.GameResult
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Duration
import java.time.Instant
import java.util.*

// Runs against Postgres because the query's crash counting is the part most likely to break.
@DataJpaTest
@Testcontainers
@Import(CrashRatioTooHighRule::class, BotService::class, Config::class)
@ImportAutoConfiguration(CacheAutoConfiguration::class)
class CrashRatioTooHighRuleIT {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var sut: CrashRatioTooHighRule

    private val lastUpload = Instant.now().minus(Duration.ofDays(1))
    private val afterUpload = Instant.now().minus(Duration.ofHours(1))
    private val beforeUpload = lastUpload.minus(Duration.ofDays(1))

    @Test
    fun `should disable only bots crashing in at least 25 percent of games since their last upload`() {
        // GIVEN
        val opponent = bot("opponent", enabled = false)

        val crasher = bot("crasher")
        games(15, crasher, opponent)
        games(3, crasher, opponent, crasherCrashed = true)
        games(2, crasher, opponent, crasherCrashed = true, opponentCrashed = true)

        val crashedBeforeUpload = bot("crashedBeforeUpload")
        games(16, crashedBeforeUpload, opponent)
        games(4, crashedBeforeUpload, opponent, crasherCrashed = true)
        games(50, crashedBeforeUpload, opponent, crasherCrashed = true, time = beforeUpload)

        val tooFewGames = bot("tooFewGames")
        games(10, tooFewGames, opponent, crasherCrashed = true)

        val failedToStart = bot("failedToStart")
        games(20, failedToStart, opponent, crasherCrashed = true, opponentCrashed = true, gameRealtime = 0.0)
        games(2, failedToStart, opponent)

        // WHEN
        sut.checkRule()

        // THEN
        assertThat(crasher.enabled).isFalse()
        assertThat(crasher.disabledReason).isEqualTo("Crashed in 5 of the last 20 games")
        assertThat(listOf(crashedBeforeUpload, tooFewGames, failedToStart)).allMatch { it.enabled }
    }

    private fun bot(name: String, enabled: Boolean = true) =
        Bot(name = name, enabled = enabled, race = Race.TERRAN, botType = "JAVA_JNI", lastUpdated = lastUpload)
            .also { entityManager.persist(it) }

    private fun games(
        count: Int,
        bot: Bot,
        opponent: Bot,
        crasherCrashed: Boolean = false,
        opponentCrashed: Boolean = false,
        gameRealtime: Double = 100.0,
        time: Instant = afterUpload
    ) = repeat(count) {
        val crashed = crasherCrashed || opponentCrashed
        entityManager.persist(
            GameResult(
                id = UUID.randomUUID(),
                time = time,
                gameRealtime = gameRealtime,
                mapPool = "SSCAIT",
                map = "map",
                botA = bot,
                raceA = Race.TERRAN,
                botB = opponent,
                raceB = Race.TERRAN,
                winner = if (crashed) null else bot,
                loser = if (crashed) null else opponent,
                botACrashed = crasherCrashed,
                botBCrashed = opponentCrashed,
                gameHash = "hash"
            )
        )
    }
}
