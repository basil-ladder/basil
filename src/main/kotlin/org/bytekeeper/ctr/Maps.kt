package org.bytekeeper.ctr

import org.bytekeeper.ctr.SCMapPool.Companion.poolSscait
import org.springframework.stereotype.Component

@Component
class Maps {

    val mapPools = listOf(
            poolSscait,
            SCMapPool.`2019Season1`
    )

    fun getMap(fileName: String) = SCMap.of(fileName)
    fun allMaps() = SCMap.allMaps.values.toList()
}

data class SCMapPool(val name: String, val maps: List<SCMap>) {
    fun random() = maps.random()

    companion object {
        val poolSscait = SCMapPool("SSCAIT", listOf(
                "sscai/(4)Empire of the Sun.scm",
                "sscai/(3)Tau Cross.scx",
                "sscai/(2)Destination.scx",
                "sscai/(4)Fighting Spirit.scx",
                "sscai/(4)Roadrunner.scx",
                "sscai/(4)Andromeda.scx",
                "sscai/(3)Neo Moon Glaive.scx",
                "sscai/(4)Jade.scx",
                "sscai/(4)La Mancha1.1.scx",
                "sscai/(4)Icarus.scm",
                "sscai/(2)Benzene.scx",
                "sscai/(4)Python.scx",
                "sscai/(4)Circuit Breaker.scx",
                "sscai/(2)Heartbreak Ridge.scx"
        ).map(SCMap.Companion::of))

        val `2019Season1` = SCMapPool("2019Season1", listOf(
                "2019Season1/(3)Medusa 2.2_iCCup.scx",
                "2019Season1/(4)Fighting Spirit.scx",
                "2019Season1/(4)Ground_Zero_2.0_iCCup.scx",
// Unsupported by BW:1.16.1                "2019Season1/(2)Cross Game.scx",
                "2019Season1/(2)Overwatch(n).scx",
                "2019Season1/(4)CircuitBreakers1.0.scx",
                "2019Season1/(4)Colosseum 2.0_iCCup.scx"
        ).map(SCMap.Companion::of))
    }
}

class SCMap private constructor(val fileName: String) {
    val mapName = mapNamePattern.matchEntire(fileName)?.groupValues?.get(1)

    companion object {
        private val mapNamePattern = ("[^/]*/?(?:\\(\\d+\\))?(.+?)\\.sc.").toRegex()
        internal val allMaps = mutableMapOf<String, SCMap>()

        internal fun of(fileName: String): SCMap = allMaps.computeIfAbsent(fileName) { SCMap(fileName) }
    }
}