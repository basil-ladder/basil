package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.Race
import org.bytekeeper.ctr.scbw.Scbw
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Instant

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension::class)
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

    @Mock
    private lateinit var botUpdater: BotUpdater

    @Mock
    private lateinit var matchmaking: UCBMatchMaking

    private val botA = Bot(id = 1, name = "A", race = Race.RANDOM, botType = "", mapPools = "pool2,pool3")
    private val botB = Bot(id = 2, name = "B", race = Race.RANDOM, botType = "", mapPools = "pool2")
    private val botC = Bot(id = 3, name = "C", race = Race.RANDOM, botType = "", mapPools = "")
    private val botD = Bot(id = 4, name = "D", race = Race.RANDOM, botType = "", mapPools = "")
    private val botAInfo = TestBotInfo()
    private val botBInfo = TestBotInfo()
    private val botCInfo = TestBotInfo()
    private val botDInfo = TestBotInfo()

    private val pool2 = SCMapPool("pool2", emptyList())
    private val pool3 = SCMapPool("pool3", emptyList())

    @BeforeEach
    fun setup() {
        given(maps.mapPools).willReturn(listOf(pool2, pool3))
        given(matchmaking.opponentSequenceFor(any())).willReturn(sequenceOf(botB, botC, botD))
        sut = GameService(scbw, maps, botSources, botUpdater, botRepository, matchmaking)
        willReturn(botAInfo).given(botSources).botInfoOf(botA.name)
        willReturn(botBInfo).given(botSources).botInfoOf(botB.name)
        willReturn(botCInfo).given(botSources).botInfoOf(botC.name)
        willReturn(botDInfo).given(botSources).botInfoOf(botD.name)

        given(botUpdater.setupBot(any())).willAnswer { (it.arguments[0] as () -> Unit)() }
    }

    @Test
    fun `should not setup source-disabled, never received bot`() {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

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
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

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
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

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
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

        botAInfo.disabled = true
        botBInfo.disabled = true

        // WHEN
        sut.schedule1on1()

        // THEN
        verify(scbw, Mockito.never()).runGame(any())
    }

    @Test
    fun `should use most recent map pool`() {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

        // WHEN
        sut.schedule1on1()

        // THEN
        val gameConfigCaptor = ArgumentCaptor.forClass(Scbw.GameConfig::class.java)
        verify(scbw).runGame(gameConfigCaptor.cap())
        assertThat(gameConfigCaptor.value)
                .extracting { it.mapPool }
                .isEqualTo(pool2)
    }

    @Test
    fun `should fall back to SSCAIT map pool`() {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botC, botB))

        // WHEN
        sut.schedule1on1()

        // THEN
        val gameConfigCaptor = ArgumentCaptor.forClass(Scbw.GameConfig::class.java)
        verify(scbw).runGame(gameConfigCaptor.cap())
        assertThat(gameConfigCaptor.value)
                .extracting { it.mapPool }
                .isEqualTo(SCMapPool.poolSscait)
    }

    @Test
    fun `Should only schedule bot later again`() {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB, botC, botD))
        sut.schedule1on1()

        // WHEN
        sut.schedule1on1()

        // THEN
        val gameConfigCaptor = ArgumentCaptor.forClass(Scbw.GameConfig::class.java)
        verify(scbw, times(2)).runGame(gameConfigCaptor.cap())
        assertThat(gameConfigCaptor.value)
                .extracting { it.bots }
                .isEqualTo(listOf("C", "B"))
    }
}