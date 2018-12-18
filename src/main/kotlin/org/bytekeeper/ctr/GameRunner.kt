package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Component
class GameRunner(private val scbw: Scbw,
                 private val config: Config,
                 private val maps: Maps,
                 private val botSources: BotSources,
                 private val commands: Commands,
                 private val botRepository: BotRepository,
                 private val events: Events) : CommandLineRunner {
    private val log = LogManager.getLogger()

    private val locks = ConcurrentHashMap<BotInfo, BotInfo>()
    private var nextPublishTime: Long = System.currentTimeMillis() + config.publishTimer * 60 * 1000
    private var nextBotUpdateTime: Long = 0
    private val phaser = object : Phaser() {
        override fun onAdvance(phase: Int, registeredParties: Int): Boolean {
            if (isTimeToPublish()) publish()
            if (isTimeToUpdateBotList()) updateBotList()
            return false
        }

    }

    var botInfoProvider: () -> BotInfo = { throw IllegalStateException() }

    override fun run(vararg args: String?) {
        updateBotList()
        log.info("Let's play!")

        repeat(3) {
            log.info("Starting worker thread")
            thread {
                try {
                    phaser.register()
                    while (true) {
                        runGame()
                        if (isTimeToPublish() || isTimeToUpdateBotList()) {
                            phaser.arriveAndAwaitAdvance()
                        }
                    }
                } catch (e: Exception) {
                    log.error("Worker thread DIED!", e)
                    throw e
                }
            }
        }
    }

    private fun isTimeToUpdateBotList() = nextBotUpdateTime <= System.currentTimeMillis()

    private fun isTimeToPublish() = nextPublishTime <= System.currentTimeMillis()

    private fun updateBotList() {
        log.info("Retrieving list of bots")
        botSources.refresh()
        val allBots = botSources.allBotInfos()
        val enabledBots = allBots.filter { !it.disabled }
        log.info("Received ${allBots.size} (${enabledBots.size} enabled) bots")
        if (allBots.isEmpty()) {
            log.error("No bots to send to the arena found!")
            return
        }
        botInfoProvider = enabledBots::random
        nextBotUpdateTime = System.currentTimeMillis() + config.botUpdateTimer * 60 * 1000

        events.waitForEmptyQueue()
    }

    private fun publish() {
        log.info("Publishing results")

        try {
            commands.handle(PreparePublish())
            val process = Runtime.getRuntime().exec(arrayOf("/bin/bash", config.publishCommand))
            val exited = process
                    .waitFor(5, TimeUnit.MINUTES)
            if (!exited) {
                log.error("Publishing of games still running after 5 minutes, killing it, continuing games...")
                process.destroyForcibly()
            }
        } catch (e: Exception) {
            log.error("Failed to publish results", e)
        } finally {
            nextPublishTime = System.currentTimeMillis() + config.publishTimer * 60 * 1000
        }
    }

    private fun runGame() {
        withLockedBot { botA ->
            withLockedBot { botB ->
                try {
                    val hash = Integer.toHexString(Objects.hash(botA.name, botB.name, Date())).toUpperCase()

                    scbw.setupOrUpdateBot(botA)
                    scbw.setupOrUpdateBot(botB)

                    scbw.runGame(Scbw.GameConfig(listOf(botA.name, botB.name), maps.maps.random(), "CTR_$hash"))
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

    private fun withLockedBot(block: (BotInfo) -> Unit) {
        val bot = generateSequence(botInfoProvider)
                .filter { DisabledBots.isEnabled(it.name) }
                .mapNotNull { botInfo ->
                    locks.putIfAbsent(botInfo, botInfo) ?: return@mapNotNull botInfo
                    null
                }.first()
        try {
            block(bot)
        } finally {
            locks.remove(bot)
        }
    }
}