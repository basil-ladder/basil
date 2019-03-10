package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.rules.WinRatioTooLowRule
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Service
class BotUpdater(private val winRatioTooLowRule: WinRatioTooLowRule,
                 private val botSources: BotSources,
                 private val botService: BotService,
                 private val events: Events,
                 private val config: Config) {
    private val log = LogManager.getLogger()
    private val updateLock = ReentrantReadWriteLock()
    var nextBotUpdateTime: Long = 0
        protected set

    @Scheduled(fixedDelayString = "#{\${basil.botUpdateTimer:6*60} * 60 * 1000}")
    fun updateBotList() {
        updateLock.write {
            winRatioTooLowRule.checkRule()

            log.info("Retrieving list of bots")
            botSources.refresh()

            val allBots = botSources.allBotInfos()
            val enabledBots = allBots.filter { !it.disabled }
            log.info("Received ${allBots.size} (${enabledBots.size} enabled) bots")
            log.info("Updating database...")
            allBots.forEach { botInfo -> botService.registerOrUpdateBot(botInfo) }
            log.info("done")
            events.post(BotListUpdated())
            nextBotUpdateTime = System.currentTimeMillis() + config.botUpdateTimer * 60 * 1000
        }
    }

    fun setupBot(setup: () -> Unit) {
        updateLock.read(setup)
    }
}