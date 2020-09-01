package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.scbw.FailedToLimitResources
import org.bytekeeper.ctr.scbw.Scbw
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class GameService(private val scbw: Scbw,
                  private val maps: Maps,
                  private val botSources: BotSources,
                  private val botUpdater: BotUpdater,
                  private val botRepository: BotRepository,
                  private var matchmaking: UCBMatchMaking) {
    private val log = LogManager.getLogger()
    private val locks = ConcurrentHashMap<Long, Long>()
    private var candidates: List<Bot> = emptyList()

    @EventHandler
    fun onBotListUpdate(event: BotListUpdated) {
        candidates = botRepository.findAllByEnabledTrue()
        log.info("${candidates.size} bots are enabled for BASIL")
    }

    fun canSchedule() = candidates.isNotEmpty()

    fun schedule1on1() {
        withLockedBot(generateSequence { candidates.random() }) { botA ->
            withLockedBot(matchmaking.opponentSequenceFor(botA)) { botB ->
                try {
                    val hash = Integer.toHexString(Objects.hash(botA.name, botB.name, Date())).toUpperCase()

                    // It is possible that a bot gets "deleted" between selecting it and setting it up.
                    // But it should not matter and just result in an exception in setupBot(...)

                    botUpdater.setupBot {
                        setupBot(botA)
                        setupBot(botB)
                    }

                    val pool = maps.mapPools.lastOrNull { botA.mapPools().contains(it.name) && botB.mapPools().contains(it.name) }
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