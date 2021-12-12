package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.scbw.FailedToLimitResources
import org.bytekeeper.ctr.scbw.Scbw
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayDeque

@Component
class GameService(
    private val scbw: Scbw,
    private val maps: Maps,
    private val botSources: BotSources,
    private val botUpdater: BotUpdater,
    private val botRepository: BotRepository,
    private var matchmaking: UCBMatchMaking
) {
    private val log = LogManager.getLogger()
    private val locks = ConcurrentHashMap<Long, Long>()
    private val botList = ArrayDeque<Long>()

    fun schedule1on1() {
        val botPos = synchronized(botList) { botList.mapIndexed { index, l -> l to index }.toMap() }
        val candidates = botRepository.findAllByEnabledTrue().sortedWith { a, b ->
            (botPos[a.id] ?: -1).compareTo(botPos[b.id] ?: -1)
        }.toMutableList()
        synchronized(botList) {
            botList.retainAll(candidates.map { it.id }.toSet())
        }
        if (candidates.size < 2) throw NotEnoughBotsException("Found ${candidates.size} bots, but need at least 2!")

        withLockedBot(generateSequence { candidates.removeFirst() }) { botA ->
            withLockedBot(matchmaking.opponentSequenceFor(botA)) { botB ->
                synchronized(botList) {
                    botList.remove(botA.id)
                    botList.remove(botB.id)
                    botList.addLast(botA.id!!)
                    botList.addLast(botB.id!!)
                }
                try {
                    val hash = Integer.toHexString(Objects.hash(botA.name, botB.name, Date())).uppercase()

                    // It is possible that a bot gets "deleted" between selecting it and setting it up.
                    // But it should not matter and just result in an exception in setupBot(...)

                    botUpdater.setupBot {
                        setupBot(botA)
                        setupBot(botB)
                    }

                    val pool = maps.mapPools.lastOrNull {
                        botA.mapPools().contains(it.name) && botB.mapPools().contains(it.name)
                    }
                        ?: SCMapPool.poolSscait
                    scbw.runGame(Scbw.GameConfig(listOf(botA.name, botB.name), pool, "CTR_$hash"))
//                    println("${botA.name} : ${botA.rating} vs ${botB.name} : ${botB.rating}")
                    Thread.sleep(1000)
                } catch (e: FailedToLimitResources) {
                    log.warn(e.message)
                } catch (e: BotDisabledException) {
                    log.warn(e.message)
                } catch (e: Exception) {
                    log.warn("Error while trying to schedule ${botA.name} vs ${botB.name}", e)
                }
            }
        }
    }

    private fun setupBot(bot: Bot) {
        val botInfo = botSources.botInfoOf(bot.name)
        if (botInfo?.disabled == false) {
            scbw.setupOrUpdateBot(botInfo)
        } else if (bot.lastUpdated == null) {
            throw BotDisabledException("${bot.name} is enabled for BASIL but disabled in source and the binary was not yet retrieved.")
        }
        scbw.checkBotDirectory(bot)
    }

    private fun withLockedBot(selector: Sequence<Bot>, block: (Bot) -> Unit) {
        val bot = selector
            .mapNotNull { bot ->
                locks.putIfAbsent(bot.id!!, bot.id!!) ?: return@mapNotNull bot
                null
            }.first()
        try {
            block(bot)
        } finally {
            locks.remove(bot.id)
        }
    }
}

class NotEnoughBotsException(msg: String) : IllegalStateException(msg)