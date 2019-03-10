package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
class GameRunner(private val gameService: GameService) : CommandLineRunner {
    private val log = LogManager.getLogger()

    override fun run(vararg args: String?) {
        log.info("Let's play!")

        repeat(3) {
            log.info("Starting worker thread")
            thread {
                try {
                    while (true) {
                        if (gameService.canSchedule()) {
                            gameService.schedule1on1()
                        } else {
                            log.debug("No bots to schedule, pausing...")
                            Thread.sleep(1000)
                        }
                    }
                } catch (e: Exception) {
                    log.error("Worker thread DIED!", e)
                    throw e
                }
            }
        }
    }


}

class BotDisabledException(message: String) : RuntimeException(message)
