package org.bytekeeper.ctr.rules

import org.bytekeeper.ctr.BotService
import org.bytekeeper.ctr.Config
import org.bytekeeper.ctr.eq
import org.bytekeeper.ctr.mock
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotStat
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.atLeastOnce
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.math.floor

internal class WinRatioTooLowRuleTest {

    private val gameResultRepository = mock<GameResultRepository>()
    private val botService = mock<BotService>()

    private val config = Config()
    private val sut = WinRatioTooLowRule(gameResultRepository, config, botService)

    private val bot = Bot(null, true, null, "botA", Race.TERRAN, "", null, false, null, 0, 1000)

    @Test
    fun `should be not be disabled with less than minGames`() {
        // GIVEN
        given(gameResultRepository.gamesSinceLastUpdate()).willReturn(listOf(BotStat(bot, 0, config.rules.winRatio.minGames - 1L)))

        // WHEN
        sut.checkRule()

        // THEN
        verify(botService, never()).disableBot(eq(bot), anyString())
    }

    @Test
    fun `should be disabled with equal or more than minGames`() {
        // GIVEN
        given(gameResultRepository.gamesSinceLastUpdate()).willReturn(listOf(BotStat(bot, 0, config.rules.winRatio.minGames.toLong())))

        // WHEN
        sut.checkRule()

        // THEN
        verify(botService, atLeastOnce()).disableBot(eq(bot), anyString())
    }

    @Test
    fun `should not be disabled with good winrate`() {
        // GIVEN
        val losses = floor((config.rules.winRatio.minGames + 1) * config.rules.winRatio.minRatio).toLong()
        given(gameResultRepository.gamesSinceLastUpdate()).willReturn(listOf(BotStat(bot, config.rules.winRatio.minGames.toLong() - losses,
                losses)))

        // WHEN
        sut.checkRule()

        // THEN
        verify(botService, never()).disableBot(eq(bot), anyString())
    }

    @Test
    fun `should be disabled with bad winrate`() {
        // GIVEN
        val wins = floor((config.rules.winRatio.minGames - 1) * config.rules.winRatio.minRatio).toLong()
        given(gameResultRepository.gamesSinceLastUpdate()).willReturn(listOf(BotStat(bot, wins,
                config.rules.winRatio.minGames - wins)))

        // WHEN
        sut.checkRule()

        // THEN
        verify(botService, atLeastOnce()).disableBot(eq(bot), anyString())
    }
}