package org.bytekeeper.ctr.proj

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class BotProjectionsTest {
    private lateinit var sut: BotProjections

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var events: Events

    private val botA = Bot(id = 1, name = "A", enabled = true)
    private val botB = Bot(id = 2, name = "B", enabled = true)

    @BeforeEach
    fun setup() {
        sut = BotProjections(botRepository, events)
        given(botRepository.getById(ArgumentMatchers.anyLong()))
                .willAnswer {
                    when (it.arguments[0]) {
                        1L -> botA
                        2L -> botB
                        else -> throw IllegalStateException()
                    }
                }
    }

    @Test
    fun shouldWriteEloHistory() {

        (1..100).map {
            for (i in 1..50) {
                if (Math.random() < 0.5)
                    sut.onGameWon(GameWon(botA, botB, ""))
                else
                    sut.onGameWon(GameWon(botB, botA, ""))
            }
        }

        verify(events, times(100 * 50 * 2)).post(any<EloUpdated>())
    }

    @Test
    fun shouldRegisterCrash() {
        // GIVEN

        // WHEN
        sut.onGameCrashed(GameCrashed(botA, botB, "", true, false, Instant.now(), 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("crashesSinceUpdate", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashesSinceUpdate", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameEnded() {
        // GIVEN

        // WHEN
        sut.onGameEnded(GameEnded(botA, botB, "", Instant.now(), 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameFailedToStart() {
        // GIVEN

        // WHEN
        sut.onGameFailedToStart(GameFailedToStart(botA, botB, "", Instant.now(), ""))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGametimeOut() {
        // GIVEN

        // WHEN
        sut.onGameTimedOut(GameTimedOut(botA, botB, botA, 1, 0, "", Instant.now(), false, true, 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameWon() {
        // GIVEN

        // WHEN
        sut.onGameWon(GameWon(botA, botB, ""))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 1)
    }

    @Test
    fun `should handle bot update`() {
        // GIVEN
        sut.onGameCrashed(GameCrashed(botA, botB, "", true, false, Instant.now(), 0.0, "", null))

        // WHEN
        sut.onBotUpdated(BotBinaryUpdated(botA, Instant.now()))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("crashesSinceUpdate", 0)
    }
}