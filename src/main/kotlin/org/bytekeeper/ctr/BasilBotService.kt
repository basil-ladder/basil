package org.bytekeeper.ctr

import org.bytekeeper.ctr.entity.BasilSourceBot
import org.bytekeeper.ctr.entity.BasilSourceBotRepository
import org.springframework.stereotype.Service
import java.time.Instant
import javax.transaction.Transactional


@Service
class BasilBotService(private val basilSourceBotRepository: BasilSourceBotRepository) {

    @Transactional
    fun update(name: String, hash: String, updateTime: Instant): Instant {
        val basilSourceBot = basilSourceBotRepository.findByName(name)
                ?: BasilSourceBot(name = name, lastUpdated = updateTime, hash = hash)
        if (basilSourceBot.hash != hash) {
            basilSourceBot.hash = hash
            basilSourceBot.lastUpdated = updateTime
        }
        basilSourceBotRepository.save(basilSourceBot)
        return basilSourceBot.lastUpdated!!
    }

    fun lastUpdateOf(name: String): Instant? = basilSourceBotRepository.findByName(name)?.lastUpdated
}