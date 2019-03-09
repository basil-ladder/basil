package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
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
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.TERRAN, botB, Race.ZERG, botA, 1, 0, "", Instant.now(), false, true, 0.0, "", null))

        // THEN
        verify(events).post(GameWon(gameResult, botA, botB))
    }

    @Test
    fun `should select loser by slowerBot on real-timeout`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.TERRAN, botB, Race.PROTOSS, botA, 1, 0, "", Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events).post(GameWon(gameResult, botB, botA))
    }

    @Test
    fun `should count as draw if neither bot was slower and score was equal`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.PROTOSS, botB, Race.ZERG, null, 0, 0, "", Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events, never()).post(any())
    }

    @Test
    fun `should count as win if only the enemy crashed`() {
        // GIVEN

        // WHEN
        sut.gameCrashed(GameCrashed(UUID.randomUUID(), botA, Race.PROTOSS, botB, Race.TERRAN, "", true, false, Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(gameResult, botB, botA))
    }

    @Test
    fun `should select winner if game ended normally`() {
        // GIVEN

        // WHEN
        sut.gameEnded(GameEnded(UUID.randomUUID(), botB, Race.ZERG, botA, Race.TERRAN, "", Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(gameResult, botB, botA))
    }
}