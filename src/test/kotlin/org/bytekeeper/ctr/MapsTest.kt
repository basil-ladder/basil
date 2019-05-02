package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.SCMapPool.Companion.poolSscait
import org.junit.jupiter.api.Test

class MapsTest {
    private val sut = Maps()

    @Test
    fun shouldParseMapNameWithoutPlayerCount() {
        // GIVEN
        // WHEN
        val mapName = SCMap.of("a/b.scx").mapName

        // THEN
        assertThat(mapName).isEqualTo("b")
    }

    @Test
    fun shouldParseSupportedNames() {
        // GIVEN
        // WHEN
        val mapNames = poolSscait.maps.map { it.mapName }

        // THEN
        assertThat(mapNames).contains(
                "Empire of the Sun",
                "Destination",
                "Tau Cross",
                "La Mancha1.1"
        )
    }
}