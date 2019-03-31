package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class GameResultsPublisherTest {
    private lateinit var sut: GameResultsPublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var unitEventsRepository: UnitEventsRepository

    @Mock
    private lateinit var publisher: Publisher

    private val writer: StringWriter = StringWriter()

    @BeforeEach
    fun setup() {
        sut = GameResultsPublisher(gameResultRepository, unitEventsRepository, publisher, Maps(), Config())

        given(publisher.globalStatsWriter(ArgumentMatchers.anyString()))
                .willReturn(BufferedWriter(writer))
    }

    @Test
    fun shouldRenderGamesWithWinner() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.PROTOSS, "")
        val botB = Bot(-1, true, null, "botB", Race.TERRAN, "")

        given(gameResultRepository.findByTimeGreaterThan(any())).willReturn(mutableListOf(
                GameResult(UUID.randomUUID(), Instant.MIN, 1.0, false, false, "map", botA, Race.ZERG, botB, Race.TERRAN, botA, botB, false, false, "", 0)))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString())
                .isEqualTo("""{bots:[{name:"botA",race:"P"},{name:"botB",race:"T"}],maps:["map"],results:[{a:{b:0,r:"Z",w:1},b:{b:1,l:1},e:"-1de5954fe5500",m:0,h:"",fc:0}]}""")
    }

    @Test
    fun shouldRenderGamesWithCrash() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.RANDOM, "")
        val botB = Bot(-1, true, null, "botB", Race.ZERG, "")

        given(gameResultRepository.findByTimeGreaterThan(any())).willReturn(mutableListOf(
                GameResult(UUID.randomUUID(), Instant.MIN, 1.0, false, false, "map", botA, Race.TERRAN, botB, Race.PROTOSS, botA, botB, true, false, "", 0)))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString())
                .isEqualTo("""{bots:[{name:"botA",race:"R"},{name:"botB",race:"Z"}],maps:["map"],results:[{a:{b:0,r:"T",w:1,c:1},b:{b:1,r:"P",l:1},e:"-1de5954fe5500",m:0,h:"",fc:0}]}""".trimMargin())
    }

    @Test
    fun shouldRenderGamesWithRealtimeout() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.RANDOM, "")
        val botB = Bot(-1, true, null, "botB", Race.ZERG, "")

        given(gameResultRepository.findByTimeGreaterThan(any())).willReturn(mutableListOf(
                GameResult(UUID.randomUUID(), Instant.MIN, 1.0, true, false, "map", botA, Race.PROTOSS, botB, Race.ZERG, null, null, false, false, "", 0)))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString())
                .isEqualTo("""{bots:[{name:"botA",race:"R"},{name:"botB",race:"Z"}],maps:["map"],results:[{a:{b:0,r:"P"},b:{b:1},iv:1,to:1,e:"-1de5954fe5500",m:0,h:"",fc:0}]}""")
    }

    @Test
    fun shouldRenderGamesWithFrametimeout() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.RANDOM, "")
        val botB = Bot(-1, true, null, "botB", Race.ZERG, "")

        given(gameResultRepository.findByTimeGreaterThan(any())).willReturn(mutableListOf(
                GameResult(UUID.randomUUID(), Instant.MIN, 1.0, false, true, "map", botA, Race.TERRAN, botB, Race.PROTOSS, botA, botB, false, false, "", 0)))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString())
                .isEqualTo("""{bots:[{name:"botA",race:"R"},{name:"botB",race:"Z"}],maps:["map"],results:[{a:{b:0,r:"T",w:1},b:{b:1,r:"P",l:1},fo:1,e:"-1de5954fe5500",m:0,h:"",fc:0}]}""")
    }

    @Test
    fun `should render game events`() {
        // GIVEN
        val botA = Bot(-1, true, null, "botA", Race.RANDOM, "")
        val botB = Bot(-1, true, null, "botB", Race.ZERG, "")

        val gameId = UUID.randomUUID()
        given(gameResultRepository.findByTimeGreaterThan(any())).willReturn(mutableListOf(
                GameResult(gameId, Instant.MIN, 1.0, false, true, "map", botA, Race.TERRAN, botB, Race.PROTOSS, botA, botB, false, false, "", 0)))
        given(unitEventsRepository.aggregateGameEventsWith8OrMoreEvents(any())).willReturn(
                listOf(GameEvent(gameId, UnitType.PROTOSS_CARRIER, UnitEventType.UNIT_CREATE, 10L))
        )

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString())
                .isEqualTo("""{bots:[{name:"botA",race:"R"},{name:"botB",race:"Z"}],maps:["map"],results:[{a:{b:0,r:"T",w:1},b:{b:1,r:"P",l:1},fo:1,e:"-1de5954fe5500",m:0,h:"",fc:0,ev:[{u:72,e:1,c:10}]}]}""")
    }
}