package org.bytekeeper.ctr

import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given

internal class UCBMatchMakingTest {
    private val botRepository: BotRepository = mock()
    private val sut = UCBMatchMaking(botRepository)
    private val botA = Bot(id = 1, name = "A", race = Race.RANDOM, botType = "", rating = 1500, played = 500)


    @ParameterizedTest
    @MethodSource("eloBots")
    fun should(bots: List<Bot>) {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(bots)
        val counts = Array(NUM_BOTS) { 0 }

        // WHEN
        for (i in 0 until 200000) {
            counts[sut.opponentSequenceFor(botA).first().id!!.toInt()]++
        }

        // THEN
        printDistribution(counts, bots)
    }

    private fun printDistribution(counts: Array<Int>, bots: List<Bot>) {
        for (i in counts.indices) {
            System.out.printf("%5d : %d\n", bots[i].rating, counts[i])
        }
    }


    companion object {
        private val GAMES_PLAYED = 500
        private val NUM_BOTS = 100

        @JvmStatic
        private fun eloBots() = listOf(
                (0 until NUM_BOTS).map { id ->
                    Bot(id = id.toLong(), name = "$id", race = Race.RANDOM, botType = "", rating = 1490 + 20 * id / NUM_BOTS, played = GAMES_PLAYED / NUM_BOTS)
                },
                (0 until NUM_BOTS).map { id ->
                    Bot(id = id.toLong(), name = "$id", race = Race.RANDOM, botType = "", rating = 1000 + 1000 * id / NUM_BOTS, played = GAMES_PLAYED / NUM_BOTS)
                },
                (0 until NUM_BOTS - 10).map { id ->
                    Bot(id = id.toLong(), name = "$id", race = Race.RANDOM, botType = "", rating = 1000 + 1000 * id / NUM_BOTS, played = GAMES_PLAYED / NUM_BOTS)
                } + (NUM_BOTS - 10 until NUM_BOTS).map { id ->
                    Bot(id = id.toLong(), name = "$id", race = Race.RANDOM, botType = "", rating = 1500, played = GAMES_PLAYED - id)
                }

        )

    }
}