package org.bytekeeper.ctr.proj

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

    @Before
    fun setup() {
        sut = BotProjections(botService, events)
        given(botService.getBotsForUpdate(ArgumentMatchers.anyList()))
                .willAnswer { it.arguments[0] }
    }

    @Test
    fun shouldWriteEloHistory() {
        val botA = Bot(name = "A", enabled = true)
        val botB = Bot(name = "B", enabled = true)

        (1..100).map {
            for (i in 1..50) {
                if (Math.random() < 0.5)
                    sut.onGameEnded(GameEnded(botA, botB, "someMap", Instant.now(), 0.0, ""))
                else
                    sut.onGameEnded(GameEnded(botB, botA, "someMap", Instant.now(), 0.0, ""))
            }
        }

        verify(events, times(100 * 50 * 2)).post(any<EloUpdated>())
    }
}