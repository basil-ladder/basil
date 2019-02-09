package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
class BotServiceTest {
    private lateinit var sut: BotService

    @Mock
    private lateinit var botRepository: BotRepository

    private val botA = Bot(id = 1, name = "A", enabled = true)
    private val botAInfo = TestBotInfo()

    private val config = Config()

    @BeforeEach
    fun setup() {
        sut = BotService(botRepository, config)
        botAInfo.name = botA.name
        given(botRepository.findByName(botA.name)).willReturn(botA)
    }

    @Test
    fun `should not disable bot if locally enabled and updated but disabled in source`() {
        // GIVEN
        botAInfo.disabled = true
        botAInfo.lastUpdated = Instant.now()
        botA.lastUpdated = Instant.MIN

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)

    }

    @Test
    fun `should disable bot if disabled in source but locally enabled and not updated`() {
        // GIVEN
        botAInfo.disabled = true

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", false)
    }

    @Test
    fun `should enable bot if locally disabled but enabled and updated in source`() {
        // GIVEN
        botA.enabled = false
        botA.lastUpdated = Instant.MAX.minusSeconds(10)
        botAInfo.lastUpdated = Instant.MAX

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)
        assertThat(botA).hasFieldOrPropertyWithValue("disabledReason", null)
    }

    @Test
    fun `should enable bot if locally disabled and never updated but enabled and updated in source`() {
        // GIVEN
        botA.enabled = false
        botAInfo.lastUpdated = Instant.MAX

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)
        assertThat(botA).hasFieldOrPropertyWithValue("disabledReason", null)
    }

    @Test
    fun `should not enable bot if locally disabled and source is enabled but not updated`() {
        // GIVEN
        botA.enabled = false
        botAInfo.lastUpdated = Instant.now()
        botA.lastUpdated = botAInfo.lastUpdated

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", false)
    }

    @Test
    fun `should disable bot if locally enabled but disabled on source and not updated longer than threshold`() {
        // GIVEN
        botA.enabled = true
        botA.lastUpdated = Instant.MIN
        botAInfo.disabled = true
        botAInfo.lastUpdated = Instant.now().minus(config.disableBotSourceDisabledAfter.toDays() + 1, ChronoUnit.DAYS)

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", false)
    }

    @Test
    fun `should not disable bot if locally enabled but disabled on source and not updated within threshold`() {
        // GIVEN
        botA.enabled = true
        botA.lastUpdated = Instant.MIN
        botAInfo.disabled = true
        botAInfo.lastUpdated = Instant.now().minus(config.disableBotSourceDisabledAfter.toDays() - 1, ChronoUnit.DAYS)

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)
    }
}