package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.BotRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Component
class GameRunner(private val gameService: GameService,
                 private val config: Config,
                 private val botSources: BotSources,
                 private val commands: Commands,
                 private val botService: BotService,
                 private val botRepository: BotRepository) : CommandLineRunner {
    private val log = LogManager.getLogger()

    private var nextPublishTime: Long = System.currentTimeMillis() + config.publishTimer * 60 * 1000
    private var nextBotUpdateTime: Long = 0
    private val phaser = object : Phaser() {
        override fun onAdvance(phase: Int, registeredParties: Int): Boolean {
            if (isTimeToPublish()) publish()
            if (isTimeToUpdateBotList()) updateBotList()
            return false
        }
    }


    override fun run(vararg args: String?) {
        updateBotList()
        log.info("Let's play!")

        repeat(3) {
            log.info("Starting worker thread")
            thread {
                try {
                    phaser.register()
                    while (true) {
                        gameService.schedule1on1()
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
        log.info("Updating database...")
        allBots.forEach { botInfo -> botService.registerOrUpdateBot(botInfo) }
        log.info("done")
        gameService.candidates = botRepository.findAllByEnabledTrue()
        if (gameService.candidates.isEmpty()) {
            log.error("No bots to send to the arena found!")
            return
        }
        log.info("${gameService.candidates.size} bots are enabled for BASIL")
        nextBotUpdateTime = System.currentTimeMillis() + config.botUpdateTimer * 60 * 1000
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

}

class BotDisabledException(message: String) : RuntimeException(message)
