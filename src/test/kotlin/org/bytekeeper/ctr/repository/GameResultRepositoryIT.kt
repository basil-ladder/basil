package org.bytekeeper.ctr.repository

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

// Covers query and type behavior that changed between Hibernate 5 and 7.
@DataJpaTest
@Testcontainers
@ImportAutoConfiguration(CacheAutoConfiguration::class)
class GameResultRepositoryIT {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var sut: GameResultRepository

    private val botA = Bot(name = "botA", race = Race.TERRAN, botType = "JAVA_JNI")
    private val botB = Bot(name = "botB", race = Race.TERRAN, botType = "JAVA_JNI")

    @Test
    fun `should average game time of decided games only`() {
        // GIVEN
        game(100.0, winner = botA, loser = botB)
        game(200.0, winner = botB, loser = botA)
        game(900.0)

        // WHEN
        val average = sut.averageGameRealtime()

        // THEN
        assertThat(average).isEqualTo(150.0)
    }

    @Test
    fun `should store times in the JVM's local time like Hibernate 5 did`() {
        // GIVEN
        val time = Instant.parse("2026-11-17T21:00:00Z")
        val game = game(100.0, time = time)
        entityManager.flush()

        // WHEN
        val stored = entityManager.createNativeQuery("SELECT time FROM game_result WHERE id = :id", LocalDateTime::class.java)
            .setParameter("id", game.id)
            .singleResult

        // THEN
        assertThat(stored).isEqualTo(LocalDateTime.ofInstant(time, ZoneId.systemDefault()))
    }

    private fun game(gameRealtime: Double, winner: Bot? = null, loser: Bot? = null, time: Instant = Instant.now()): GameResult {
        if (botA.id == null) {
            entityManager.persist(botA)
            entityManager.persist(botB)
        }
        return GameResult(
            id = UUID.randomUUID(),
            time = time,
            gameRealtime = gameRealtime,
            mapPool = "SSCAIT",
            map = "map",
            botA = botA,
            raceA = Race.TERRAN,
            botB = botB,
            raceB = Race.TERRAN,
            winner = winner,
            loser = loser,
            gameHash = "hash"
        ).also { entityManager.persist(it) }
    }
}
