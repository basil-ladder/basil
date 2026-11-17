package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
class GameRunner(private val gameService: GameService,
                 private val config: Config) : CommandLineRunner {
    private val log = LogManager.getLogger()

    override fun run(vararg args: String) {
        log.info("Let's play! Starting ${config.parallelGamesCount} concurrent games")

        repeat(config.parallelGamesCount) {
            log.info("Starting worker thread")
            thread {
                while (true) {
                    try {
                        gameService.schedule1on1()
                    } catch (e: NotEnoughBotsException) {
                        log.debug("${e.message} Pausing...")
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        log.error("Failed to schedule a game, retrying in 10s", e)
                        Thread.sleep(10_000)
                    }
                }
            }
        }
    }


}

class BotDisabledException(message: String) : RuntimeException(message)
