package org.bytekeeper.ctr

import org.springframework.stereotype.Component

@Component
class Maps {
    final val poolSscait = SCMapPool("SSCAIT", listOf(
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

    final val mapPools = listOf(
            poolSscait
    )

    fun getMap(fileName: String) = SCMap.of(fileName)
    fun allMaps() = SCMap.allMaps.values.toList()
}

class SCMapPool(val name: String, val maps: List<SCMap>) {
    fun random() = maps.random()
}

class SCMap private constructor(val fileName: String) {
    val mapName = mapNamePattern.matchEntire(fileName)?.groupValues?.get(1)

    companion object {
        private val mapNamePattern = ("[^/]*/?(?:\\(\\d+\\))?(.+?)\\.sc.").toRegex()
        internal val allMaps = mutableMapOf<String, SCMap>()

        internal fun of(fileName: String): SCMap = allMaps.computeIfAbsent(fileName) { SCMap(fileName) }
    }
}