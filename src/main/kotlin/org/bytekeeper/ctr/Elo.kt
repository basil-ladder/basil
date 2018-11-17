package org.bytekeeper.ctr

import org.bytekeeper.ctr.Ctr.botDir
import org.bytekeeper.ctr.Ctr.botDirs
import org.bytekeeper.ctr.Ctr.ctrPath
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

class BotElo(val botName: String, var rating: Int = 1600, var played: Int = 0)
class EloHistoryEntry(val rating: Int, val timestamp: Long)
class BotEloHistory(val botName: String, val eloList: MutableList<EloHistoryEntry> = mutableListOf())

const val ELO_HISTORY_FILENAME = "eloHistory.json"

object Elo {
    private val lock = ReentrantLock()

    fun updateElo(winner: String, loser: String) {
        lock.withLock {
            val eloFile = ctrPath.resolve("elo.json")
            val currentElos = if (eloFile.toFile().exists()) {
                klaxon.parseArray<BotElo>(eloFile.toFile())!!.toMutableList()
            } else mutableListOf()

            fun eloOf(name: String) = currentElos.firstOrNull { it.botName == name }
                ?: kotlin.run {
                    val botElo = BotElo(name)
                    currentElos.add(botElo)
                    botElo
                }

            val eloA = eloOf(winner)
            val eloB = eloOf(loser)
            updateElo(eloA, eloB, true)

            Files.write(
                eloFile,
                listOf(klaxon.toJsonString(currentElos)),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )

            val timestamp = Instant.now().toEpochMilli()
            updateEloHistory(winner, eloA.rating, timestamp)
            updateEloHistory(loser, eloB.rating, timestamp)
            Unit
        }
    }

    private fun updateEloHistory(bot: String, rating: Int, timestamp: Long) {
        val botEloFile = botDir(bot).resolve(ELO_HISTORY_FILENAME)

        val botEloHistory =
            if (botEloFile.toFile().exists()) klaxon.parse<BotEloHistory>(botEloFile.toFile())!!
            else BotEloHistory(bot)

        botEloHistory.eloList += EloHistoryEntry(rating, timestamp)

        Files.write(
            botEloFile,
            listOf(klaxon.toJsonString(botEloHistory)),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    internal fun updateElo(eloA: BotElo, eloB: BotElo, aWon: Boolean) {
        val eA = 1.0 / (1 + Math.pow(10.0, (eloB.rating - eloA.rating) / 400.0))
        val eB = 1.0 - eA
        val rA = eloA.rating
        val rB = eloB.rating
        val kA = if (eloA.played < 30) 40 else if (eloA.rating > 2400) 10 else 20
        val kB = if (eloB.played < 30) 40 else if (eloB.rating > 2400) 10 else 20
        eloA.rating = (rA + kA * ((if (aWon) 1 else 0) - eA)).roundToInt()
        eloB.rating = (rB + kB * ((if (aWon) 0 else 1) - eB)).roundToInt()

        eloA.played++
        eloB.played++
    }

    fun copyToTarget() {
        val aggDir = Paths.get(Config.targetDir).resolve("agg")
        Files.createDirectories(aggDir)
        Files.copy(
            ctrPath.resolve("elo.json"),
            aggDir.resolve("elo.json"),
            StandardCopyOption.REPLACE_EXISTING
        )
        botDirs().forEach { botDir ->
            val botEloFile = botDir.resolve(ELO_HISTORY_FILENAME)
            if (!botEloFile.toFile().exists()) return@forEach

            Files.copy(botEloFile, aggDir.resolve("ELO - ${botDir.fileName}"), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}