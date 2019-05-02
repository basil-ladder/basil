package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import javax.transaction.Transactional

@Service
class BotService(private val botRepository: BotRepository,
                 private val config: Config) {
    private val log = LogManager.getLogger()


    @Transactional
    fun registerOrUpdateBot(botInfo: BotInfo) {
        botRepository.findByName(botInfo.name)?.also { updateBot(it, botInfo) } ?: registerBot(botInfo)
    }

    private fun registerBot(botInfo: BotInfo) {
        log.info("Bot ${botInfo.name} not yet registered, creating it.")
        val bot = Bot(id = null,
                enabled = !botInfo.disabled,
                disabledReason = null,
                name = botInfo.name,
                race = botInfo.race,
                botType = botInfo.botType,
                lastUpdated = null,
                publishRead = botInfo.publishReadDirectory,
                authorKeyId = botInfo.authorKey,
                mapPools = botInfo.supportedMapPools.joinToString(","))
        botRepository.save(bot)
    }

    private fun updateBot(bot: Bot, botInfo: BotInfo) {
        val enabledInSourceAndUpdated = !botInfo.disabled && botInfo.lastUpdated.isAfter(bot.lastUpdated ?: Instant.MIN)
        val locallyEnabledAndBinaryAvailable = bot.enabled && bot.lastUpdated != null
        val enabledInSourceOrRecentlyDisabled = !botInfo.disabled || Duration.between(botInfo.lastUpdated, Instant.now()) < config.disableBotSourceDisabledAfter
        bot.enabled = enabledInSourceAndUpdated
                || locallyEnabledAndBinaryAvailable && enabledInSourceOrRecentlyDisabled
        if (bot.enabled) {
            bot.disabledReason = null
        }
        bot.publishRead = botInfo.publishReadDirectory
        bot.authorKeyId = botInfo.authorKey
        bot.mapPools = botInfo.supportedMapPools.joinToString(",")
    }

    @Transactional
    fun disableBot(bot: Bot, reason: String) {
        with(botRepository.getById(bot.id!!)) {
            enabled = false
            disabledReason = reason.take(200)
        }
    }


}