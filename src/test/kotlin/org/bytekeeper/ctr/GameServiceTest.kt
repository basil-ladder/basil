package org.bytekeeper.ctr

import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
class GameServiceTest {
    private lateinit var sut: GameService

    @Mock
    private lateinit var scbw: Scbw

    @Mock
    private lateinit var config: Config

    @Mock
    private lateinit var maps: Maps

    @Mock
    private lateinit var botSources: BotSources

    @Mock
    private lateinit var commands: Commands

    @Mock
    private lateinit var botService: BotService

    @Mock
    private lateinit var botRepository: BotRepository

    private val botA = Bot(id = 1, name = "A", enabled = true)
    private val botB = Bot(id = 2, name = "B", enabled = true)
    private val botAInfo = TestBotInfo()
    private val botBInfo = TestBotInfo()

    @Before
    fun setup() {
        sut = GameService(scbw, Maps(), botSources)
        given(botSources.botInfoOf(botA.name)).willReturn(botAInfo)
        given(botSources.botInfoOf(botB.name)).willReturn(botBInfo)
    }

    @Test
    fun `should not setup source-disabled, never received bot`() {
        // GIVEN
        sut.candidates = listOf(botA, botB)
        botAInfo.disabled = true
        botBInfo.disabled = true

        // WHEN
        sut.schedule1on1()

        // THEN
        verify(scbw, Mockito.never()).setupOrUpdateBot(any())
    }

    @Test
    fun `should not setup source-disabled, but received bot`() {
        // GIVEN
        sut.candidates = listOf(botA, botB)
        botA.lastUpdated = Instant.MIN
        botB.lastUpdated = Instant.MIN
        botAInfo.disabled = true
        botBInfo.disabled = true

        // WHEN
        sut.schedule1on1()

        // THEN
        verify(scbw, Mockito.never()).setupOrUpdateBot(any())
    }

    @Test
    fun `should schedule game for source-disabled, but locally enabled and available bot`() {
        // GIVEN
        sut.candidates = listOf(botA, botB)
        botA.lastUpdated = Instant.MIN
        botB.lastUpdated = Instant.MIN
        botAInfo.disabled = true
        botBInfo.disabled = true

        // WHEN
        sut.schedule1on1()

        // THEN
        verify(scbw).runGame(any())
    }

    @Test
    fun `should not schedule game for source-disabled, locally enabled but unavailable bot`() {
        // GIVEN
        sut.candidates = listOf(botA, botB)
        botAInfo.disabled = true
        botBInfo.disabled = true

        // WHEN
        sut.schedule1on1()

        // THEN
        verify(scbw, Mockito.never()).runGame(any())
    }
}