package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import javax.transaction.Transactional

@Service
class BotService(val botRepository: BotRepository) {
    private val log = LogManager.getLogger()

    @Transactional
    fun registerOrUpdateBot(botInfo: BotInfo) {
        botRepository.findByName(botInfo.name)
                ?.also { bot ->
                    bot.enabled = (!botInfo.disabled || bot.enabled && bot.lastUpdated != null) && !isDisabledLocally(botInfo.name)
                    bot.publishRead = botInfo.publishReadDirectory
                    bot.authorKeyId = botInfo.authorKey
                } ?: run {
            log.info("Bot ${botInfo.name} not yet registered, creating it.")
            val bot = Bot(null,
                    !botInfo.disabled && !isDisabledLocally(botInfo.name),
                    botInfo.name,
                    botInfo.race,
                    botInfo.botType,
                    null,
                    botInfo.publishReadDirectory,
                    botInfo.authorKey)
            botRepository.save(bot)
        }
    }

    fun isDisabledLocally(name: String): Boolean {
        val disabledBotsFile = Paths.get("", "disabledBots.txt")
        return disabledBotsFile.toFile().exists() && Files.lines(disabledBotsFile).anyMatch { it == name }
    }
}