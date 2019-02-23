package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.Maps
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.MapStat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.StringWriter

@ExtendWith(MockitoExtension::class)
internal class PerMapWinRatePublisherTest {
    private lateinit var sut: PerMapWinRatePublisher

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    private val botA = Bot(null, true, null, "botA", null, null, null, false, null, 0, 1000)
    private val botB = Bot(null, true, null, "botB", null, null, null, false, null, 0, 2000)

    private val writer = StringWriter()

    @BeforeEach
    fun setup() {
        sut = PerMapWinRatePublisher(botRepository, gameResultRepository, publisher, Maps())
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))
        given(publisher.botStatsWriter(anyString(), anyString())).willReturn(writer)
    }

    @Test
    fun `should write map stats for bot`() {
        // GIVEN
        given(gameResultRepository.botStatsPerMap(botA)).willReturn(listOf(MapStat("a.scx", 1, 2), MapStat("b.scm", 3, 4)))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(writer.toString()).isEqualTo("[{\"map\":\"a\",\"won\":1,\"lost\":2},{\"map\":\"b\",\"won\":3,\"lost\":4}][]")
    }
}