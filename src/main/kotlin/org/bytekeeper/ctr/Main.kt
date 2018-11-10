package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager

object Main {
    private val log = LogManager.getLogger()

    fun run() {
        log.info("Retrieving list of bots")
        val bots = Sscait.retrieveListOfBots()
        val botNames = bots.map { it.name }
        val amountOfEnabledBots = bots.count { !it.isDisabled }
        log.info("Received ${bots.size} ($amountOfEnabledBots enabled) bots")
        if (bots.isEmpty()) {
            log.error("No bots to send to the arena found!")
            return
        }
        log.info("Let's play!")
        GameRunner(botNames::random, botNames::random).run()
    }
}

fun main(args: Array<String>) {
    Main.run()
}
