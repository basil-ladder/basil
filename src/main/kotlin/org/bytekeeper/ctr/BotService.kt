package org.bytekeeper.ctr

import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.stereotype.Service

@Service
class BotService(private val botRepository: BotRepository) {

    fun findByName(name: String) = botRepository.findByName(name)

    fun getById(id: Long) = botRepository.getById(id)

    fun save(bot: Bot) = botRepository.save(bot)
}