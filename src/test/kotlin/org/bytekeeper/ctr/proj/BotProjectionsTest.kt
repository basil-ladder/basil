package org.bytekeeper.ctr.proj

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.entity.Bot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
internal class BotProjectionsTest {
    private lateinit var sut: BotProjections

    @Mock
    private lateinit var botService: BotService

    @Mock
    private lateinit var events: Events

    private val botA = Bot(name = "A", enabled = true)
    private val botB = Bot(name = "B", enabled = true)

    @Before
    fun setup() {
        sut = BotProjections(botService, events)
        given(botService.getBotsForUpdate(ArgumentMatchers.anyList()))
                .willAnswer { it.arguments[0] }
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
}