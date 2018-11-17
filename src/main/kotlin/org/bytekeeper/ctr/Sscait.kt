package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import org.bytekeeper.ctr.Sscait.dateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Sscait {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun retrieveListOfBots(): List<BotInfo> {
        return download("https://sscaitournament.com/api/bots.php").use {
            klaxon.parseArray(it)!!
        }
    }
}

class BotInfo(
    val name: String,
    val race: String,
    val wins: String? = "0",
    val losses: String? = "0",
    val status: String? = "Disabled",
    val update: String? = null,
    val botBinary: String,
    val bwapiDLL: String,
    val botType: String
) {
    val isDisabled = status == "Disabled"

    fun downloadBinary() = download(botBinary)

    fun downloadBwapiDll() = download(bwapiDLL)

    fun lastUpdated(): Instant =
        if (update == null) Instant.MIN else LocalDateTime.parse(update, dateFormat).toInstant(ZoneOffset.UTC)
}