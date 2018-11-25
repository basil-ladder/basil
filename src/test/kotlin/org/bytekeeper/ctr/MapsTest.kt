package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class MapsTest {
    private val sut = Maps()

    @Test
    fun shouldParseMapNameWithoutPlayerCount() {
        // GIVEN
        // WHEN
        val mapName = sut.mapName("a/b.scx")

        // THEN
        assertThat(mapName).isEqualTo("b")
    }

    @Test
    fun shouldParseSupportedNames() {
        // GIVEN
        // WHEN
        val mapNames = sut.maps.map { sut.mapName(it) }

        // THEN
        assertThat(mapNames).contains(
                "Empire of the Sun",
                "Destination",
                "Tau Cross"
        )
    }
}