package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CSVTest {
    @Test
    fun `should parse unit event line`() {
        // GIVEN
        val line = "1963,unitComplete,true,138,Terran_Supply_Depot, \"(3824,704)\""

        // WHEN
        val result = CSV.parseLine(line)

        // THEN
        assertThat(result).containsExactly("1963", "unitComplete", "true", "138", "Terran_Supply_Depot", "(3824,704)")
    }
}