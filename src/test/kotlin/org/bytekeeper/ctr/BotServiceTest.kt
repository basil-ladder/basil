package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
class BotServiceTest {
    private lateinit var sut: BotService

    @Mock
    private lateinit var botRepository: BotRepository

    private val botA = Bot(id = 1, name = "A", enabled = true)
    private val botAInfo = TestBotInfo()

    @Before
    fun setup() {
        sut = BotService(botRepository)
        botAInfo.name = botA.name
        given(botRepository.findByName(botA.name)).willReturn(botA)
    }

    @Test
    fun `should not disable bot if locally enabled and updated`() {
        // GIVEN
        botAInfo.disabled = true
        botA.lastUpdated = Instant.MIN

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)

    }

    @Test
    fun `should disable bot if locally enabled and not updated`() {
        // GIVEN
        botAInfo.disabled = true

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", false)
    }

    @Test
    fun `should enable bot if locally disabled`() {
        // GIVEN
        botA.enabled = false

        // WHEN
        sut.registerOrUpdateBot(botAInfo)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("enabled", true)
    }
}