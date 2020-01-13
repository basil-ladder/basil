package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.SCMapPool.Companion.poolSscait
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.GameResult
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class GameResultsProjectionsTest {
    private lateinit var sut: GameResultsProjections

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var events: Events

    private val botA = Bot(name = "A", race = Race.RANDOM, botType = "")
    private val botB = Bot(name = "B", race = Race.RANDOM, botType = "")
    private val pool = poolSscait

    private val map = pool.maps[0]
    private lateinit var gameResult: GameResult;

    @BeforeEach
    fun setup() {
        sut = GameResultsProjections(gameResultRepository, events)
        given(gameResultRepository.save(any<GameResult>()))
                .willAnswer {
                    gameResult = it.arguments[0] as GameResult
                    gameResult
                }
    }

    @Test
    fun `should select winner by score on frame-timeout`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.TERRAN, botB, Race.ZERG, 1, 0, pool, map, Instant.now(), false, true, 0.0, "", null))

        // THEN
        verify(events).post(GameWon(gameResult, botA, botB))
    }

    @Test
    fun `should skip real-timeout game results`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.TERRAN, botB, Race.PROTOSS, 1, 0, pool, map, Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events, never()).post(GameWon(gameResult, botB, botA))
    }

    @Test
    fun `should count as draw if neither bot was slower and score was equal`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.PROTOSS, botB, Race.ZERG, 0, 0, pool, map, Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events, never()).post(any())
    }

    @Test
    fun `should count as win if only the enemy crashed`() {
        // GIVEN

        // WHEN
        sut.gameCrashed(GameCrashed(UUID.randomUUID(), botA, Race.PROTOSS, botB, Race.TERRAN, pool, map, true, false, Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(gameResult, botB, botA))
    }

    @Test
    fun `should select winner if game ended normally`() {
        // GIVEN

        // WHEN
        sut.gameEnded(GameEnded(UUID.randomUUID(), botB, Race.ZERG, botA, Race.TERRAN, pool, map, Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(gameResult, botB, botA))
    }

    @Test
    fun `should save correct race`() {
        // GIVEN
        val id = UUID.randomUUID()
        val timestamp = Instant.now()

        // WHEN
        sut.gameEnded(GameEnded(id, botB, Race.ZERG, botA, Race.TERRAN, pool, map, timestamp, 0.0, "", 0))

        // THEN
        verify(gameResultRepository).save(eq(GameResult(id, timestamp, 0.0, false, false, pool.name, map.fileName, botB, Race.ZERG,
                botA, Race.TERRAN, botB, botA, false, false, "", 0)))
    }
}