package org.bytekeeper.ctr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.bytekeeper.ctr.SscaitSource.BotInfo
import org.bytekeeper.ctr.SscaitSource.BotInfo.Companion.BASIL_COMMAND_MATCHER
import org.bytekeeper.ctr.SscaitSource.BotInfo.Companion.commands
import org.junit.Test

internal class BotInfoTest {
    @Test
    fun shouldMatchBasilCommand() {
        assertThat("BASIL: DESTROY, ALL, HUMAN").containsPattern(BASIL_COMMAND_MATCHER)
    }

    @Test
    fun shouldParseBasilCommand() {
        // GIVEN

        // WHEN
        val matcher = BASIL_COMMAND_MATCHER.matcher("BASIL: DESTROY, ALL, HUMAN")

        // THEN
        matcher.find()
        assertThat(commands(matcher.group(1)))
                .contains("DESTROY", "ALL", "HUMAN")
    }

    @Test
    fun shouldDisableBotForBASIL() {
        // GIVEN

        // WHEN
        val botInfo = BotInfo("bot", "Random", "0", "0", null, null, "binary", "bwapi", "TYPE", "BASIL: DISABLED")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("disabled", true)
    }

    @Test
    fun shouldResetBotForBASIL() {
        // GIVEN

        // WHEN
        val botInfo = BotInfo("bot", "Zerg", "0", "0", null, null, "binary", "bwapi", "TYPE", "Some text here BASIL: RESET whatever!")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("clearReadDirectory", true)
    }

    @Test
    fun `should parse publish read`() {
        // GIVEN

        // WHEN
        val botInfo = BotInfo("bot", "Protoss", "0", "0", null, null, "binary", "bwapi", "TYPE", "Some text here BASIL: PUBLISH-READ whatever!")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("publishReadDirectory", true)
    }

    @Test
    fun `should parse multiple settings`() {
        // GIVEN

        // WHEN
        val botInfo = BotInfo("bot", "Terran", "0", "0", null, null, "binary", "bwapi", "TYPE", "Some text here BASIL: PUBLISH-READ,RESET whatever!")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("publishReadDirectory", true)
        assertThat(botInfo).hasFieldOrPropertyWithValue("clearReadDirectory", true)
    }

    @Test
    fun `should parse public key id`() {
        // WHEN
        val botInfo = BotInfo("bot", "Terran", "0", "0", null, null, "binary", "bwapi", "TYPE", "Some text here BASIL: PB-KEY-12345678,RESET whatever!")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("publishReadDirectory", true)
        assertThat(botInfo).hasFieldOrPropertyWithValue("authorKey", "12345678")
    }


    @Test
    fun `should find bot with BASIL command`() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // WHEN
        val botInfoList = mapper.readValue<List<BotInfo>>(BotInfoTest::class.java.getResource("/bots.json"))

        // THEN
        assertThat(botInfoList).extracting("authorKey", "publishReadDirectory")
                .contains(Tuple.tuple("4E5297FFDB40A6FA", true))

    }
}