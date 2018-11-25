package org.bytekeeper.ctr

import org.springframework.stereotype.Component

@Component
class Maps {
    private val MAP_NAME_PATTERN = "[^/]*/(?:\\(\\d+\\))?([^.]+)\\.sc.".toRegex()

    val maps = listOf(
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
    )

    fun mapName(map: String) = MAP_NAME_PATTERN.matchEntire(map)?.groupValues?.get(1)
}