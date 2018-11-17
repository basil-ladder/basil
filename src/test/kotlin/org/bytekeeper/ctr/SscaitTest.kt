package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.iterable.Extractor
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

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

    @Test
    fun shouldParseUpdatedDate() {
        val list = Klaxon().parseArray<BotInfo>(javaClass.getResourceAsStream("/bots.json"))

        assertThat(list).extracting(Extractor<BotInfo, Instant> { it.lastUpdated() })
            .contains(
                LocalDateTime.of(2017, 11, 5, 17, 46, 41)
                    .toInstant(ZoneOffset.UTC)
            )
    }
}