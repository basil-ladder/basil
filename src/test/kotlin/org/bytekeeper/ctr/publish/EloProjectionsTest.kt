package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.StringWriter
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class EloPublisherTest {
    @Mock
    private lateinit var botEloRepository: BotEloRepository

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var botHistoryRepository: BotHistoryRepository
    private lateinit var sut: EloPublisher

    @Mock
    private lateinit var publisher: Publisher

    private val botStatsWriter: StringWriter = StringWriter()
    val testBot = Bot(-1, true, null, "test", Race.PROTOSS, "MIRROR", null)
    private val gameResult = GameResult(
        id = UUID.randomUUID(),
        gameRealtime = 0.0,
        time = Instant.now(),
        mapPool = "",
        map = "",
        botA = testBot,
        raceA = Race.PROTOSS,
        botB = testBot,
        raceB = Race.PROTOSS,
        gameHash = ""
    )

    @BeforeEach
    fun setup() {
        sut = EloPublisher(botEloRepository, publisher, botRepository, botHistoryRepository)

        given(publisher.botStatsWriter(anyString(), anyString())).willReturn(botStatsWriter)
    }

    @Test
    fun shouldPublishBotEloHistory() {
        // GIVEN
        given(botEloRepository.findByBotOrderByTime(null)).willReturn(
            listOf(
                BotElo(-1, testBot, Instant.MIN, 0, gameResult),
                BotElo(-1, testBot, Instant.MIN, 1, gameResult)
            )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(botStatsWriter.toString()).isEqualTo("[{\"epochSecond\":-31557014167219200,\"rating\":0},{\"epochSecond\":-31557014167219200,\"rating\":1}]")
    }

    @Test
    fun shouldShowUpdates() {
        // GIVEN
        given(botEloRepository.findByBotOrderByTime(null)).willReturn(
            listOf(
                BotElo(-1, testBot, Instant.ofEpochSecond(1000), 0, gameResult),
                BotElo(-1, testBot, Instant.ofEpochSecond(2000), 1, gameResult),
                BotElo(-1, testBot, Instant.ofEpochSecond(3000), 2, gameResult),
                BotElo(-1, testBot, Instant.ofEpochSecond(4000), 2, gameResult),
                BotElo(-1, testBot, Instant.ofEpochSecond(5000), 2, gameResult)
            )
        )
        given(botHistoryRepository.findAllByOrderByTimeAsc()).willReturn(
            listOf(
                BotHistory(testBot, Instant.ofEpochSecond(1100), ""),
                BotHistory(testBot, Instant.ofEpochSecond(1200), ""),
                BotHistory(testBot, Instant.ofEpochSecond(4000), "")
            )
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(botStatsWriter.toString()).isEqualTo("[{\"epochSecond\":1000,\"rating\":0},{\"epochSecond\":2000,\"rating\":1,\"updated\":true},{\"epochSecond\":3000,\"rating\":2},{\"epochSecond\":4000,\"rating\":2,\"updated\":true},{\"epochSecond\":5000,\"rating\":2}]")
    }
}