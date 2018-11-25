package org.bytekeeper.ctr

import kotlin.math.roundToInt

object Elo {
    fun calculateElos(rA: Int, playedA: Int, rB: Int, playedB: Int): Pair<Int, Int> {
        val eA = 1.0 / (1 + Math.pow(10.0, (rB - rA) / 400.0))
        val eB = 1.0 - eA
        val kA = kValue(playedA, rA)
        val kB = kValue(playedB, rB)

        return (rA + kA * (1 - eA)).roundToInt() to (rB + kB * (-eB)).roundToInt()
    }

    private fun kValue(playedA: Int, rA: Int) =
            if (playedA < 30 && rA < 2100) 40 else if (rA < 2200) 20 else 10

}