package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import java.net.URL

object Sscait {
    fun retrieveListOfBots(): List<BotInfo> {
        val connection = URL("https://sscaitournament.com/api/bots.php").openConnection()
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
        )
        return connection.getInputStream()
            .use {
                Klaxon().parseArray(it)!!
            }
    }
}

data class BotInfo(
    val name: String, val race: String, val wins: String, val losses: String, val status: String,
    val update: String, val botBinary: String, val bwapiDLL: String
) {
    val isDisabled = status == "Disabled"
}