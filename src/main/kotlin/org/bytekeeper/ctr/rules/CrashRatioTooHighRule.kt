package org.bytekeeper.ctr.rules

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.BotService
import org.bytekeeper.ctr.Config
import org.bytekeeper.ctr.repository.GameResultRepository
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class CrashRatioTooHighRule(private val gameResultRepository: GameResultRepository,
                            private val config: Config,
                            private val botService: BotService) {
    private val log = LogManager.getLogger()

    fun checkRule() {
        val ruleConfig = config.rules.crashRatio
        log.info("Disabling bots with >= ${ruleConfig.minGames} games since last update and crash ratio >= ${"%.2f".format(ruleConfig.maxRatio)}.")
        gameResultRepository.crashesSinceLastUpdate()
                .forEach {
                    if (it.games < max(ruleConfig.minGames, 1)) return@forEach
                    val ratio = it.crashed.toDouble() / it.games
                    if (ratio < ruleConfig.maxRatio) return@forEach
                    log.info("Disabling ${it.bot.name}, it crashed in ${it.crashed} of ${it.games} games since the last update.")
                    botService.disableBot(it.bot, "Crashed in ${it.crashed} of the last ${it.games} games")
                }
    }
}
