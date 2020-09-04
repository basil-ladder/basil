package org.bytekeeper.ctr

import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional
import kotlin.math.min
import kotlin.math.round

@Component
class Ranking(
        private val botRepository: BotRepository,
        private val config: Config
) {
    fun buildEloLadder(botCount: Int): List<RankBotCount> {
        val result = mutableListOf<RankBotCount>()
        var remainingBots = botCount
        for (delta in arrayOf(Rank.S to 0.01, Rank.A to 0.07, Rank.B to 0.21, Rank.C to 0.21, Rank.D to 0.21, Rank.E to 0.21)) {
            val botsInRank = min(round(delta.second * botCount).toInt(), remainingBots)
            remainingBots -= botsInRank
            result += RankBotCount(delta.first, botsInRank)
        }
        result += RankBotCount(Rank.F, remainingBots)
        return result
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    fun updateRankings() {
        val activeBots = botRepository.findAllByEnabledTrue()
        val botsToCheck = activeBots
                .sortedBy { it.rating }.toMutableList()
        val ranks = buildEloLadder(botsToCheck.size)
        for (rank in ranks) {
            for (i in 0 until rank.botCount) {
                val bot = botsToCheck.removeLast()
                if (bot.played >= bot.rankSince + config.ranking.games_rank_locked) {
                    bot.rankSince = bot.played
                    bot.rank = rank.rank
                    botRepository.save(bot)
                }
            }
        }
    }

    data class RankBotCount(val rank: Rank, val botCount: Int)

    enum class Rank {
        S, A, B, C, D, E, F, UNRANKED
    }
}