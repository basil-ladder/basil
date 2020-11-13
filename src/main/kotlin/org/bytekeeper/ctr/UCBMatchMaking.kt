package org.bytekeeper.ctr

import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

@Component
class UCBMatchMaking(
        private val botRepository: BotRepository
) {
    private val rng = SplittableRandom()

    fun opponentSequenceFor(botA: Bot): Sequence<Bot> {
        val candidates = botRepository.findAllByEnabledTrue()
        val sumPlayed = candidates.sumBy { it.played }

        val botARating = botA.rating
        return candidates.map { botB ->
            val ratingSkew = rng.nextInt(-RATING_SKEW * 2, RATING_SKEW * 2 + 1)
            val expectedGameOutcome: Double = 1.0 / (1.0 + 10.0.pow((botB.rating - botARating + ratingSkew) / 400.0))
            val strengthCloseness: Double = 1.0 - abs(0.5 - expectedGameOutcome)
            botB to strengthCloseness + EXPLORATION * sqrt(ln(sumPlayed + 1.0) / (botB.played + 1)) + rng.nextDouble() * RANDOMIZATION
        }.sortedByDescending { it.second }.map { it.first }.asSequence()
    }

    companion object {
        /**
         * Higher = bot with fewer games will be preferred
         */
        const val EXPLORATION = 0.2

        /**
         * Higher = Higher probability that a "bad" opponent might be chosen - the actual rating difference will be changed up to this value
         */
        const val RATING_SKEW = 50

        /**
         * Add additional noise to the selection process, this allows games vs vast skill differences to still happen
         */
        const val RANDOMIZATION = 2
    }
}