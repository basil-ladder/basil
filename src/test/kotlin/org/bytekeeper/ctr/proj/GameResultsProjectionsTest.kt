package org.bytekeeper.ctr.proj

import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.GameResultRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
internal class GameResultsProjectionsTest {
    private lateinit var sut: GameResultsProjections

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var events: Events

    private val botA = Bot(name = "A", enabled = true)
    private val botB = Bot(name = "B", enabled = true)

    @Before
    fun setup() {
        sut = GameResultsProjections(gameResultRepository, events)
    }

    @Test
    fun `should select winner by score on frame-timeout`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(botA, botB, botA, 1, 0, "", Instant.now(), false, true, 0.0, "", null))

        // THEN
        verify(events).post(GameWon(botA, botB, ""))
    }

    @Test
    fun `should select loser by slowerBot on real-timeout`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(botA, botB, botA, 1, 0, "", Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events).post(GameWon(botB, botA, ""))
    }

    @Test
    fun `should count as draw if neither bot was slower and score was equal`() {
        // GIVEN

        // WHEN
        sut.gameTimedOut(GameTimedOut(botA, botB, null, 0, 0, "", Instant.now(), true, false, 0.0, "", null))

        // THEN
        verify(events, never()).post(any())
    }

    @Test
    fun `should count as win if only the enemy crashed`() {
        // GIVEN

        // WHEN
        sut.gameCrashed(GameCrashed(botA, botB, "", true, false, Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(botB, botA, ""))
    }

    @Test
    fun `should select winner if game ended normally`() {
        // GIVEN

        // WHEN
        sut.gameEnded(GameEnded(botB, botA, "", Instant.now(), 0.0, "", 0))

        // THEN
        verify(events).post(GameWon(botB, botA, ""))
    }
}