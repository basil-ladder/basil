package org.bytekeeper.ctr

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.Race
import org.bytekeeper.ctr.publish.RankingsPublisher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.BufferedWriter
import java.io.StringWriter
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
class RankingsPublisherTest {
    @Mock
    private lateinit var botRepository: BotRepository
    private lateinit var sut: RankingsPublisher

    @Mock
    private lateinit var publisher: Publisher

    private val statsWriter: StringWriter = StringWriter()

    @Before
    fun setup() {
        sut = RankingsPublisher(publisher, botRepository)

        BDDMockito.given(publisher.globalStatsWriter(BDDMockito.anyString())).willReturn(BufferedWriter(statsWriter))
    }

    @Test
    fun shouldPublishAllElos() {
        // GIVEN
        val botA = Bot(-1, true, "botA", Race.PROTOSS, null, Instant.MIN, false, null, 100, 1000)
        val botB = Bot(-1, true, "botB", Race.PROTOSS, null, null, false, null, 200, 3000)
        BDDMockito.given(botRepository.findAllByEnabledTrue()).willReturn(listOf(botA, botB))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(statsWriter.toString()).isEqualTo(
                "[{\"botName\":\"botA\",\"rating\":1000,\"played\":100,\"won\":0,\"lost\":0,\"crashed\":0,\"race\":\"PROTOSS\",\"lastUpdated\":-31557014167219200,\"enabled\":true}," +
                        "{\"botName\":\"botB\",\"rating\":3000,\"played\":200,\"won\":0,\"lost\":0,\"crashed\":0,\"race\":\"PROTOSS\",\"lastUpdated\":null,\"enabled\":true}]")
    }

}