package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import kotlin.concurrent.thread

class GameRunner(val botNameProvider: () -> String, val mapNameProvider: () -> String) : Runnable {
    private val log = LogManager.getLogger()

    override fun run() {
        repeat(3) {
            log.info("Starting worker thread")
            thread {
                while (true) {
                    val scbwRunner = ScbwRunner(bots = listOf(botNameProvider(), botNameProvider()))
                    scbwRunner.run()
                }
            }
        }
    }
}