package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotVsBotWonGames
import org.bytekeeper.ctr.entity.GameResultRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.BufferedWriter
import java.io.StringWriter

@RunWith(MockitoJUnitRunner::class)
internal class BotVsBotPublisherTest {
    private lateinit var sut: BotVsBotPublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    private val jsonWriter: StringWriter = StringWriter()
    private val csvWriter: StringWriter = StringWriter()

    val botA = Bot(null, true, "botA", null, null, null, 0, 1000)
    val botB = Bot(null, true, "botB", null, null, null, 0, 2000)
    val botC = Bot(null, true, "botC", null, null, null, 0, 3000)

    @Before
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
            Bot, botC, botB, botA
            botC, 0, 0, 10
            botB, 11, 0, 0
            botA, 0, 12, 0

            """.trimIndent())
    }
}