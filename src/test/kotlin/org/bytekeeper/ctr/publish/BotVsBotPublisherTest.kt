package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotVsBotWonGames
import org.bytekeeper.ctr.repository.GameResultRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter

@ExtendWith(MockitoExtension::class)
class BotVsBotPublisherTest {
    private lateinit var sut: BotVsBotPublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    private val jsonWriter: StringWriter = StringWriter()
    private val csvWriter: StringWriter = StringWriter()

    private val botA = Bot(null, true, null, "botA", null, null, null, false, null, 0, 1000)
    private val botB = Bot(null, true, null, "botB", null, null, null, false, null, 0, 2000)
    private val botC = Bot(null, true, null, "botC", null, null, null, false, null, 0, 3000)

    @BeforeEach
    fun setup() {
        sut = BotVsBotPublisher(gameResultRepository, publisher)

        given(publisher.globalStatsWriter(BDDMockito.anyString())).willReturn(BufferedWriter(jsonWriter)).willReturn(BufferedWriter(csvWriter))
    }

    @Test
    fun `should publish a CSV table`() {
        // GIVEN
        given(gameResultRepository.listBotVsBotWonGames()).willReturn(
                listOf(
                        BotVsBotWonGames(botA, botB, 12L),
                        BotVsBotWonGames(botB, botC, 11L),
                        BotVsBotWonGames(botC, botA, 10L)
                )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(csvWriter.toString()).isEqualTo("""
            Bot, ELO, botC, botB, botA
            botC, 3000, 0, 0, 10
            botB, 2000, 11, 0, 0
            botA, 1000, 0, 12, 0

            """.trimIndent())
    }

    @Test
    fun `should publish crosstable info`() {
        // GIVEN
        given(gameResultRepository.listBotVsBotWonGames()).willReturn(
                listOf(
                        BotVsBotWonGames(botA, botB, 12L),
                        BotVsBotWonGames(botB, botC, 11L),
                        BotVsBotWonGames(botC, botA, 10L)
                )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(jsonWriter.toString()).isEqualTo(
                """{"botinfos":[{"name":"botC","race":null,"rating":3000,"enabled":true},{"name":"botB","race":null,"rating":2000,"enabled":true},{"name":"botA","race":null,"rating":1000,"enabled":true}],"botVsBotStat":[{"winner":"botA","loser":"botB","won":12},{"winner":"botB","loser":"botC","won":11},{"winner":"botC","loser":"botA","won":10}]}""")
    }
}