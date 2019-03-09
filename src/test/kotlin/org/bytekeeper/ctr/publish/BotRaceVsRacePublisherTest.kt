package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRaceVsRace
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter

@ExtendWith(MockitoExtension::class)
internal class BotRaceVsRacePublisherTest {
    private lateinit var sut: BotRaceVsRacePublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    private val jsonWriter: StringWriter = StringWriter()

    private val botA = Bot(null, true, null, "botA", Race.ZERG, "", null, false, null, 0, 1000)
    private val botB = Bot(null, true, null, "botB", Race.TERRAN, "", null, false, null, 0, 2000)
    private val botC = Bot(null, true, null, "botC", Race.PROTOSS, "", null, false, null, 0, 3000)

    @BeforeEach
    fun setup() {
        sut = BotRaceVsRacePublisher(gameResultRepository, publisher)

        BDDMockito.given(publisher.botStatsWriter(anyString(), anyString()))
                .willReturn(BufferedWriter(jsonWriter))

    }

    @Test
    fun `should publish a CSV table`() {
        // GIVEN
        BDDMockito.given(gameResultRepository.listBotRaceVsRace()).willReturn(
                listOf(
                        BotRaceVsRace(botA, Race.ZERG, Race.TERRAN, 12L, 10L),
                        BotRaceVsRace(botA, Race.PROTOSS, Race.ZERG, 12L, 10L),
                        BotRaceVsRace(botA, Race.TERRAN, Race.TERRAN, 12L, 10L)
                )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(jsonWriter.toString()).isEqualTo("""[{"race":"ZERG","enemyRace":"TERRAN","won":12,"lost":10},{"race":"TERRAN","enemyRace":"TERRAN","won":12,"lost":10},{"race":"PROTOSS","enemyRace":"ZERG","won":12,"lost":10}]""")
    }
}