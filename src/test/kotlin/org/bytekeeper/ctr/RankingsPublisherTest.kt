package org.bytekeeper.ctr

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.publish.RankingsPublisher
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.Race
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class RankingsPublisherTest {
    @Mock
    private lateinit var botRepository: BotRepository
    private lateinit var sut: RankingsPublisher

    @Mock
    private lateinit var publisher: Publisher

    @Mock
    private lateinit var botSources: BotSources

    private val statsWriter: StringWriter = StringWriter()

    @BeforeEach
    fun setup() {
        sut = RankingsPublisher(publisher, botRepository, botSources)

        BDDMockito.given(publisher.globalStatsWriter(BDDMockito.anyString())).willReturn(BufferedWriter(statsWriter))
    }

    @Test
    fun shouldPublishAllElos() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.PROTOSS, null, Instant.MIN, false, null, 100, 1000)
        val botB = Bot(-1, true, null, "botB", Race.TERRAN, null, null, false, null, 200, 3000)
        val botC = Bot(-1, false, "I don't like it", "botC", Race.ZERG, null, null, false, null, 300, 4000)
        BDDMockito.given(botRepository.findAll()).willReturn(listOf(botA, botB, botC))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(statsWriter.toString()).isEqualTo(
                "[{\"botName\":\"botA\",\"rating\":1000,\"played\":100,\"won\":0,\"lost\":0,\"crashed\":0,\"crashesSinceUpdate\":0,\"race\":\"PROTOSS\",\"lastUpdated\":-31557014167219200,\"enabled\":true,\"disabledReason\":null}" +
                        ",{\"botName\":\"botB\",\"rating\":3000,\"played\":200,\"won\":0,\"lost\":0,\"crashed\":0,\"crashesSinceUpdate\":0,\"race\":\"TERRAN\",\"lastUpdated\":null,\"enabled\":true,\"disabledReason\":null}" +
                        ",{\"botName\":\"botC\",\"rating\":4000,\"played\":300,\"won\":0,\"lost\":0,\"crashed\":0,\"crashesSinceUpdate\":0,\"race\":\"ZERG\",\"lastUpdated\":null,\"enabled\":false,\"disabledReason\":\"I don't like it\"}]")
    }

}