package org.bytekeeper.ctr.rules

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.BotService
import org.bytekeeper.ctr.Config
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class WinRatioTooLowRule(private val gameResultRepository: GameResultRepository,
                         private val config: Config,
                         private val botService: BotService) {
    private val log = LogManager.getLogger()

    fun checkRule() {
        val ruleConfig = config.rules.winRatio
        log.info("Disabling bots with >= ${ruleConfig.minGames} games since last update and win ratio < ${"%.2f".format(ruleConfig.minRatio)}.")
        gameResultRepository.gamesSinceLastUpdate()
                .forEach {
                    val games = it.lost + it.won
                    if (it.lost + it.won < max(ruleConfig.minGames, 1)) return@forEach
                    val ratio = it.won.toDouble() / (it.won + it.lost)
                    if (ratio >= ruleConfig.minRatio) return@forEach
                    val winRatioString = "%.2f".format(ratio)
                    log.info("Disabling ${it.bot.name}, it played $games since the last update and has a WR of $winRatioString.")
                    botService.disableBot(it.bot, "WR $winRatioString in the last $games games")
                }
    }

}