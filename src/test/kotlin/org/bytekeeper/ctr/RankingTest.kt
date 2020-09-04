package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class RankingTest {
    private val botRepository: BotRepository = mock<BotRepository>()
    private val sut = Ranking(botRepository, Config())
    private val bots = (0 until 84).map {
        Bot(it.toLong(), name = "$it", race = Race.TERRAN, botType = "", played = 196 + it, rating = it)
    }

    @BeforeEach
    fun setup() {
        given(botRepository.findAllByEnabledTrue()).willReturn(bots)
        sut.updateRankings()
    }

    @Test
    fun `All should be ranked that are not in recovery`() {
        // GIVEN

        // WHEN

        // THEN
        assertThat(bots).filteredOn { it.rank == Ranking.Rank.UNRANKED }.hasSize(4)
    }

    @Test
    fun `should keep ranking intact for bots in recovery phase`() {
        // GIVEN
        bots[0].rating = 100
        bots[0].played = 480

        // WHEN
        sut.updateRankings()

        // THEN
        assertThat(bots).filteredOn { it.rank == Ranking.Rank.S }.hasSize(2)
    }
}