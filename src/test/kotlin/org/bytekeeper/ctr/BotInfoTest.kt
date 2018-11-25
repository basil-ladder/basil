package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.BotInfo.Companion.BASIL_COMMAND_MATCHER
import org.bytekeeper.ctr.BotInfo.Companion.commands
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
        val botInfo = BotInfo("bot", "race", "0", "0", null, null, "binary", "bwapi", "TYPE", "BASIL: DISABLED")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("disabledForBasil", true)
    }

    @Test
    fun shouldResetBotForBASIL() {
        // GIVEN

        // WHEN
        val botInfo = BotInfo("bot", "race", "0", "0", null, null, "binary", "bwapi", "TYPE", "BASIL: RESET")

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("clearReadDirectory", true)
    }
}