package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test

class KlaxonTest {
    @Test
    fun shouldLoadResultJson() {
        // GIVEN

        // WHEN
        val resultJson = Klaxon().parse<ResultJson>(KlaxonTest::class.java.getResourceAsStream("/testresult.json"))

        // THEN
        assertThat(resultJson).extracting("is_realtime_outed", "is_gametime_outed", "is_crashed")
            .containsExactly(false, false, true)
    }
}