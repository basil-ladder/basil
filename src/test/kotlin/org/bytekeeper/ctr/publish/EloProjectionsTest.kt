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
    private lateinit var sut: EloPublisher

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var publisher: Publisher

    private val botStatsWriter: StringWriter = StringWriter()
    val testBot = Bot(-1, true, null, "test", Race.PROTOSS, "MIRROR", null)
    private val gameResult = GameResult(
            id = UUID.randomUUID(),
            gameRealtime = 0.0,
            time = Instant.now(),
            map = "",
            botA = testBot,
            raceA = Race.PROTOSS,
            botB = testBot,
            raceB = Race.PROTOSS,
            gameHash = ""
    )

    @BeforeEach
    fun setup() {
        sut = EloPublisher(botEloRepository, botRepository, publisher)

        given(publisher.botStatsWriter(anyString(), anyString())).willReturn(botStatsWriter)
    }

    @Test
    fun shouldPublishBotEloHistory() {
        // GIVEN
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(testBot))
        given(botEloRepository.findAllByBot(testBot)).willReturn(listOf(
                BotElo(-1, testBot, Instant.MIN, 0, gameResult),
                BotElo(-1, testBot, Instant.MIN, 1, gameResult)
        ))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(botStatsWriter.toString()).isEqualTo("[{\"epochSecond\":-31557014167219200,\"rating\":0},{\"epochSecond\":-31557014167219200,\"rating\":1}]")
    }

}