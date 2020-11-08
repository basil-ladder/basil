package org.bytekeeper.ctr

import org.bytekeeper.ctr.math.Elo
import org.bytekeeper.ctr.repository.BotRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class SimMatchups {
    val bots = mutableMapOf<String, B>()
    val botRepository = mock<BotRepository>()
    val ucbMatchMaking = UCBMatchMaking(botRepository)
    var sumPlayed = 0

    @BeforeEach
    fun setup() {
        given(botRepository.findAllByEnabledTrue()).willAnswer { bots.values.toList() }

        SimMatchups::class.java.getResource("/bot_vs_bot.csv")
                .readText()
                .lines()
                .drop(1)
                .filter { it.isNotBlank() }
                .map {
                    val line = it.split(",")
                    val winner = line[0]
                    val loser = line[1]
                    val count = Integer.parseInt(line[2])
                    val played = Integer.parseInt(line[3])
                    bots.computeIfAbsent(loser) { B(loser) }.playedOnBasil = played
                    bots.computeIfAbsent(winner) { B(winner) }.gameCount.compute(loser) { _, c ->
                        (c ?: 0) + count
                    }
                }
        for (bot in bots.values) {
            bot.winrates = bot.gameCount.map { (e, c) ->
                val enemy = bots[e]!!
                val games = c + (enemy.gameCount[bot.name] ?: 0)
                enemy to c.toDouble() / games
            }.toMap().toMutableMap()
        }
    }

    @Test
    fun `Simulate with old match making and no "upsetters"`() {
        (0 until 1000000).forEach {
            playGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    @Test
    fun `Simulate with new match making and no "upsetters"`() {
        (0 until 1000000).forEach {
            ucbPlayGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    @Test
    fun `Simulate with new match making and one 10% "upsetter" in the last 20% games`() {
        (0 until 800000).forEach {
            ucbPlayGame()
        }
        bots["StyxZ"]!!.winrates.compute(bots["krasi0"]!!) { _, c -> c!! + 0.10 }
        bots["krasi0"]!!.winrates.compute(bots["StyxZ"]!!) { _, c -> c!! + 0.10 }
        (0 until 200000).forEach {
            ucbPlayGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    @Test
    fun `Simulate with old match making and one 10% "upsetter" in the last 20% games`() {
        (0 until 800000).forEach {
            playGame()
        }
        bots["StyxZ"]!!.winrates.compute(bots["krasi0"]!!) { _, c -> c!! + 0.10 }
        bots["krasi0"]!!.winrates.compute(bots["StyxZ"]!!) { _, c -> c!! + 0.10 }
        (0 until 200000).forEach {
            playGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    @Test
    fun `Simulate with old match making and one heavy-weight newcomer in the last 5% games`() {
        (0 until 950000).forEach {
            playGame()
        }
        val newcomer = B("AlphaOmega")
        bots[newcomer.name] = newcomer
        for (bot in bots.values) {
            bot.winrates[newcomer] = 0.1
            newcomer.winrates[bot] = 0.9

        }
        (0 until 50000).forEach {
            playGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    @Test
    fun `Simulate with new match making and one heavy-weight newcomer in the last 5% games`() {
        (0 until 950000).forEach {
            ucbPlayGame()
        }
        val newcomer = B("AlphaOmega")
        bots[newcomer.name] = newcomer
        for (bot in bots.values) {
            bot.winrates[newcomer] = 0.1
            newcomer.winrates[bot] = 0.9

        }
        (0 until 50000).forEach {
            ucbPlayGame()
        }
        bots.entries.sortedByDescending { (n, b) -> b.elo }
                .take(10)
                .forEach {
                    println(it.value)
                }
    }

    private fun playGame() {
        val first = bots.keys.random()
        val second = generateSequence { bots.keys.random() }.filter { it != first }.first()
        playAvsB(first, second)
    }

    private fun ucbPlayGame() {
        val first = bots.keys.random()
        val botA = bots[first]!!
        val second = ucb(botA).filter { it.name != first }.first()
        playAvsB(first, second.name)
    }

    private fun ucb(botA: B) =
            bots.values.map { botB ->
                val expectedGameOutcome: Double = 1.0 / (1.0 + 10.0.pow((botB.elo - botA.elo) / 400.0))
                val strengthCloseness: Double = 1.0 - abs(0.5 - expectedGameOutcome)
                botB to strengthCloseness + UCBMatchMaking.EXPLORATION * sqrt(ln(sumPlayed + 1.0) / (botB.played + 1)) + Math.random() * UCBMatchMaking.RATING_SKEW
            }.sortedByDescending { it.second }.map { it.first }.asSequence()

    private fun playAvsB(first: String, second: String) {
        val botA = bots[first]!!
        val botB = bots[second]!!
        val winrate = botA.winrates[botB] ?: return
        val (eloA, eloB) = if (Math.random() < winrate) {
            Elo.calculateElos(botA.elo, botA.played, botB.elo, botB.played)
        } else {
            val (a, b) = Elo.calculateElos(botB.elo, botB.played, botA.elo, botA.played)
            b to a
        }
        botA.elo = eloA
        botB.elo = eloB
        botA.played++
        botB.played++
        sumPlayed++
    }
}

class B(val name: String, var elo: Int = 2000, var played: Int = 0, var winrates: MutableMap<B, Double> = mutableMapOf(), val gameCount: MutableMap<String, Int> = mutableMapOf(), var playedOnBasil: Int = 0) {
    override fun toString(): String = "%20s : %5d".format(name, elo)
}