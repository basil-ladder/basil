package org.bytekeeper.ctr.proj

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.StringWriter
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
internal class EloProjectionsTest {
    @Mock
    private lateinit var botEloRepository: BotEloRepository
    private lateinit var sut: EloProjections

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var publisher: Publisher

    private val botStatsWriter: StringWriter = StringWriter()
    private val statsWriter: StringWriter = StringWriter()

    @Before
    fun setup() {
        sut = EloProjections(botEloRepository, botRepository, publisher)

        given(publisher.botStatsWriter(anyString(), anyString())).willReturn(botStatsWriter)
        given(publisher.globalStatsWriter(anyString())).willReturn(statsWriter)
    }

    @Test
    fun shouldPublishBotEloHistory() {
        // GIVEN
        val testBot = Bot(-1, true, "test", Race.PROTOSS, "MIRROR", null)
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(testBot))
        given(botEloRepository.findAllByBot(testBot)).willReturn(listOf(
                BotElo(-1, testBot, Instant.MIN, 0, ""),
                BotElo(-1, testBot, Instant.MIN, 1, "")
        ))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(botStatsWriter.toString()).isEqualTo("[{\"epochSecond\":-31557014167219200,\"rating\":0,\"gameHash\":\"\"},{\"epochSecond\":-31557014167219200,\"rating\":1,\"gameHash\":\"\"}]")
    }

    @Test
    fun shouldPublishAllElos() {
        // GIVEN
        val botA = Bot(-1, true, "botA", Race.PROTOSS, null, null, 100, 1000)
        val botB = Bot(-1, true, "botB", Race.PROTOSS, null, null, 200, 3000)
        given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(statsWriter.toString()).isEqualTo("[{\"botName\":\"botA\",\"rating\":1000,\"played\":100},{\"botName\":\"botB\",\"rating\":3000,\"played\":200}]")
    }
}