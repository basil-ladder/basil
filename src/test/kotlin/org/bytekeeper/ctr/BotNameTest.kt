package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BotNameTest {
    @ParameterizedTest
    @ValueSource(strings = ["PurpleWave", "Hao Pan", "Bot_2.0-beta", "Jérôme"])
    fun `should accept usual bot names`(name: String) {
        assertThat(isValidBotName(name)).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", ".", "..", "../other", "a/b", "bot:Z", ".hidden"])
    fun `should reject names that could escape folders or break arguments`(name: String) {
        assertThat(isValidBotName(name)).isFalse()
    }
}
