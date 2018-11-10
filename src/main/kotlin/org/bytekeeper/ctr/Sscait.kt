package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import java.net.URL

object Sscait {
    fun retrieveListOfBots(): List<BotInfo> =
        URL("https://sscaitournament.com/api/bots.php")
            .openStream()
            .use {
                Klaxon().parseArray(it)!!
            }
}

data class BotInfo(
    val name: String, val race: String, val wins: String, val losses: String, val status: String,
    val update: String, val botBinary: String, val bwapiDLL: String
) {
    val isDisabled = status == "Disabled"
}