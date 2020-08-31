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
    private var sumPlayed: Int = 0
    private lateinit var candidates: List<Bot>
    private val rng = SplittableRandom()

    init {
        onEloUpdated(null)
    }

    @EventHandler
    fun onEloUpdated(event: EloUpdated?) {
        updateCandidates()
    }

    @EventHandler
    fun onBotListUpdated(event: BotListUpdated) {
        updateCandidates()
    }

    private fun updateCandidates() {
        candidates = botRepository.findAllByEnabledTrue()
        sumPlayed = candidates.sumBy { it.played }
    }

    fun opponentSequenceFor(botA: Bot): Sequence<Bot> {
        val botARating = botA.rating
        return candidates.map { botB ->
            val expectedGameOutcome: Double = 1.0 / (1.0 + 10.0.pow((botB.rating - botARating) / 400.0))
            val strengthCloseness: Double = 1.0 - abs(0.5 - expectedGameOutcome)
            botB to strengthCloseness + EXPLORATION * sqrt(ln(sumPlayed + 1.0) / (botB.played + 1)) + rng.nextDouble() * RANDOMIZATION
        }.sortedByDescending { it.second }.map { it.first }.asSequence()
    }

    companion object {
        /**
         * Higher = bot with fewer games will be preferred
         */
        private const val EXPLORATION = 0.2

        /**
         * Higher = Higher probability that a "bad" opponent might be chosen (this is not a 0-1 scale!)
         */
        private const val RANDOMIZATION = 3
    }
}