package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test

class SscaitTest {
    @Test
    fun shouldParseBotResultFromSSCAIT() {
        val list = Klaxon().parseArray<BotInfo>(javaClass.getResourceAsStream("/bots.json"))

        assertThat(list).extracting("name", "wins")
            .contains(tuple("Jakub Trancik", "5118"))
    }

    @Test
    fun shouldDetectDisabledBots() {
        val list = Klaxon().parseArray<BotInfo>(javaClass.getResourceAsStream("/bots.json"))

        assertThat(list).extracting("disabled")
            .contains(true)
    }
}