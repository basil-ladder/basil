package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.any
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotVsBotWonGames
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
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

    private val botA = Bot(null, true, null, "botA", Race.ZERG, "", null, false, null, 0, 1000)
    private val botB = Bot(null, true, null, "botB", Race.TERRAN, "", null, false, null, 0, 2000)
    private val botC = Bot(null, true, null, "botC", Race.PROTOSS, "", null, false, null, 0, 3000)

    @BeforeEach
    fun setup() {
        sut = BotVsBotPublisher(gameResultRepository, publisher)

        var g = given(publisher.globalStatsWriter(BDDMockito.anyString()))
        repeat(10) {
            g = g.willReturn(BufferedWriter(jsonWriter)).willReturn(BufferedWriter(csvWriter))
        }
    }

    @Test
    fun `should publish a CSV table`() {
        // GIVEN
        given(gameResultRepository.listBotVsBotWonGames(any())).willReturn(
                listOf(
                        BotVsBotWonGames(botA, botB, 12L),
                        BotVsBotWonGames(botB, botC, 11L),
                        BotVsBotWonGames(botC, botA, 10L)
                )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(csvWriter.toString()).isEqualTo("""Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
Bot, ELO, botC, botB, botA
botC, 3000, 0, 0, 10
botB, 2000, 11, 0, 0
botA, 1000, 0, 12, 0
""")
    }

    @Test
    fun `should publish crosstable info`() {
        // GIVEN
        given(gameResultRepository.listBotVsBotWonGames(any())).willReturn(
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
                """{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}{"botinfos":[{"name":"botC","race":"PROTOSS","rating":3000,"enabled":true,"vsBotIdxWon":[0,0,10]},{"name":"botB","race":"TERRAN","rating":2000,"enabled":true,"vsBotIdxWon":[11,0,0]},{"name":"botA","race":"ZERG","rating":1000,"enabled":true,"vsBotIdxWon":[0,12,0]}]}""")
    }
}