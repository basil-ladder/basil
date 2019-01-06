package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
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
        botRepository.findByName(botInfo.name)
                ?.also { bot ->
                    bot.enabled = (!botInfo.disabled && bot.lastUpdated?.isBefore(botInfo.lastUpdated) == true
                            || bot.enabled && bot.lastUpdated != null
                            && (!botInfo.disabled || Duration.between(botInfo.lastUpdated, Instant.now()).compareTo(config.disableBotSourceDisabledAfter) < 0))
                    if (bot.enabled) {
                        bot.disabledReason = null
                    }
                    bot.publishRead = botInfo.publishReadDirectory
                    bot.authorKeyId = botInfo.authorKey
                } ?: run {
            log.info("Bot ${botInfo.name} not yet registered, creating it.")
            val bot = Bot(null,
                    !botInfo.disabled,
                    null,
                    botInfo.name,
                    botInfo.race,
                    botInfo.botType,
                    null,
                    botInfo.publishReadDirectory,
                    botInfo.authorKey)
            botRepository.save(bot)
        }
    }

    @Transactional
    fun disableBot(bot: Bot, reason: String) {
        with(botRepository.getById(bot.id!!)) {
            enabled = false
            disabledReason = reason.take(200)
        }
    }
}