package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.rules.WinRatioTooLowRule
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Service
class BotUpdater(
    private val winRatioTooLowRule: WinRatioTooLowRule,
    private val botSources: BotSources,
    private val botService: BotService,
) {
    private val log = LogManager.getLogger()
    private val updateLock = ReentrantReadWriteLock()

    @Value("\${basil.botUpdateSchedule:'0 0 */6 * * *'}")
    private val updateSchedule = ""
    var nextBotUpdateTime: Long = 0
        protected set

    @Scheduled(cron = "\${basil.botUpdateSchedule:0 0 */6 * * *}")
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
            nextBotUpdateTime =
                CronExpression.parse(updateSchedule).next(OffsetDateTime.now())?.toInstant()?.toEpochMilli() ?: 0
        }
    }

    fun setupBot(setup: () -> Unit) {
        updateLock.read(setup)
    }
}