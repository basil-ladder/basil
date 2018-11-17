package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val PUBLISH_TIMER = 30// 10 minutes
const val DEFAULT_BOT_UPDATE_TIMER = 6 * 60 // 6 hours

class GameRunner() : Runnable {
    private val log = LogManager.getLogger()
    private val locks = ConcurrentHashMap<BotInfo, BotInfo>()
    private var nextPublishTime: Long = System.currentTimeMillis() + PUBLISH_TIMER * 60 * 1000
    private var nextBotUpdateTime: Long = 0

    private val phaser = object : Phaser() {
        override fun onAdvance(phase: Int, registeredParties: Int): Boolean {
            if (isTimeToPublish()) publish()
            if (isTimeToUpdateBotList()) updateBotList()
            return false
        }

    }

    var botInfoProvider: () -> BotInfo = { throw IllegalStateException() }

    override fun run() {
        updateBotList()
        Files.createDirectories(Paths.get(Config.workDir))
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
        val allBots = Sscait.retrieveListOfBots()
        val enabledBots = allBots.filter { !it.isDisabled }
        log.info("Received ${allBots.size} (${enabledBots.size} enabled) bots")
        if (allBots.isEmpty()) {
            log.error("No bots to send to the arena found!")
            return
        }
        botInfoProvider = enabledBots::random
        nextBotUpdateTime = System.currentTimeMillis() + (Config.botUpdateTimer ?: DEFAULT_BOT_UPDATE_TIMER) * 60 * 1000
    }

    private fun publish() {
        log.info("Publishing results")
        try {
            Elo.copyToTarget()
            val process = Runtime.getRuntime().exec(arrayOf("/bin/bash", Config.publishCommand))
            val exited = process
                .waitFor(5, TimeUnit.MINUTES)
            if (!exited) {
                log.error("Publishing of games still running after 5 minutes, killing it, continuing games...")
                process.destroyForcibly()
            }
        } catch (e: Exception) {
            log.error("Failed to publish results", e)
        } finally {
            nextPublishTime = System.currentTimeMillis() + PUBLISH_TIMER * 60 * 1000
        }
    }

    private fun runGame() {
        withLockedBot { botA ->
            withLockedBot { botB ->
                try {
                    val hash = Integer.toHexString(Objects.hash(botA.name, botB.name, Date())).toUpperCase()

                    botA.ensureBot()
                    botB.ensureBot()

                    val scbwRunner = ScbwRunner(
                        bots = listOf(botA.name, botB.name),
                        gameName = "CTR_$hash"
                    )
                    scbwRunner.run()
                } catch (e: FailedToLimitResources) {
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