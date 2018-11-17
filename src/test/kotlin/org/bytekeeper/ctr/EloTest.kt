package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.math.abs

internal class EloTest {
    @Test
    fun botBWins() {
        // GIVEN
        val botA = BotElo("a", 2806, 30)
        val botB = BotElo("b", 2577, 30)

        // WHEN
        Elo.updateElo(botA, botB, false)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("rating", 2798)
        assertThat(botB).hasFieldOrPropertyWithValue("rating", 2585)
    }

    @Test
    fun botAWins() {
        // GIVEN
        val botA = BotElo("a", 2806, 30)
        val botB = BotElo("b", 2577, 30)

        // WHEN
        Elo.updateElo(botA, botB, true)

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("rating", 2808)
        assertThat(botB).hasFieldOrPropertyWithValue("rating", 2575)
    }

    @Test
    fun shouldUpdateAggregatedEloFile() {
        // GIVEN
        val tempDirectory = Files.createTempDirectory("ctr")
        Config.workDir = tempDirectory.toFile().absolutePath

        // WHEN
        for (i in 1..30) {
            if (i % 2 == 0)
                Elo.updateElo("a", "b")
            else
                Elo.updateElo("b", "a")
        }

        // THEN
        val elos = klaxon.parseArray<BotElo>(tempDirectory.resolve("elo.json").toFile())
        assertThat(elos).extracting("rating", "played")
            .allMatch { t ->
                abs(t.toList()[0] as Int - 1600) < 20 &&
                        t.toList()[1] == 30
            }
    }

    @Test
    fun shouldUpdateIndividualEloFile() {
        // GIVEN
        val tempDirectory = Files.createTempDirectory("ctr")
        Config.workDir = tempDirectory.toFile().absolutePath

        // WHEN
        for (i in 1..30) {
            if (i % 2 == 0)
                Elo.updateElo("a", "b")
            else
                Elo.updateElo("b", "a")
        }

        // THEN
        val eloHistory = klaxon.parse<BotEloHistory>(Ctr.botDir("a").resolve(ELO_HISTORY_FILENAME).toFile())
        assertThat(eloHistory).hasFieldOrPropertyWithValue("botName", "a")
        assertThat(eloHistory!!.eloList).extracting("rating")
            .contains(1602, 1604, 1608)
            .hasSize(15)
    }
}