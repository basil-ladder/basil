package org.bytekeeper.ctr.publish

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
class EloPublisherTest {
    @Mock
    private lateinit var botEloRepository: BotEloRepository
    private lateinit var sut: EloPublisher

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var publisher: Publisher

    private val botStatsWriter: StringWriter = StringWriter()

    @Before
    fun setup() {
        sut = EloPublisher(botEloRepository, botRepository, publisher)

        given(publisher.botStatsWriter(anyString(), anyString())).willReturn(botStatsWriter)
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

}