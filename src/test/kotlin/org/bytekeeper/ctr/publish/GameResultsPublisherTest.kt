package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.repository.Bot
import org.bytekeeper.ctr.repository.GameResult
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.Race
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
    private lateinit var publisher: Publisher

    private val writer: StringWriter = StringWriter()

    @BeforeEach
    fun setup() {
        sut = GameResultsPublisher(gameResultRepository, publisher, Maps(), Config())

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
                .isEqualTo("[{\"botA\":{\"name\":\"botA\",\"race\":\"PROTOSS\",\"winner\":true,\"loser\":false,\"crashed\":false},\"botB\":{\"name\":\"botB\",\"race\":\"TERRAN\",\"winner\":false,\"loser\":true,\"crashed\":false},\"validGame\":true,\"realTimeout\":false,\"frameTimeout\":false,\"endedAt\":-31557014167219200,\"map\":\"map\",\"gameHash\":\"\",\"frameCount\":0}]")
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
                .isEqualTo("[{\"botA\":{\"name\":\"botA\",\"race\":\"RANDOM\",\"winner\":true,\"loser\":false,\"crashed\":true},\"botB\":{\"name\":\"botB\",\"race\":\"ZERG\",\"winner\":false,\"loser\":true,\"crashed\":false},\"validGame\":true,\"realTimeout\":false,\"frameTimeout\":false,\"endedAt\":-31557014167219200,\"map\":\"map\",\"gameHash\":\"\",\"frameCount\":0}]")
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
                .isEqualTo("[{\"botA\":{\"name\":\"botA\",\"race\":\"RANDOM\",\"winner\":false,\"loser\":false,\"crashed\":false},\"botB\":{\"name\":\"botB\",\"race\":\"ZERG\",\"winner\":false,\"loser\":false,\"crashed\":false},\"validGame\":false,\"realTimeout\":true,\"frameTimeout\":false,\"endedAt\":-31557014167219200,\"map\":\"map\",\"gameHash\":\"\",\"frameCount\":0}]")
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
                .isEqualTo("[{\"botA\":{\"name\":\"botA\",\"race\":\"RANDOM\",\"winner\":true,\"loser\":false,\"crashed\":false},\"botB\":{\"name\":\"botB\",\"race\":\"ZERG\",\"winner\":false,\"loser\":true,\"crashed\":false},\"validGame\":true,\"realTimeout\":false,\"frameTimeout\":true,\"endedAt\":-31557014167219200,\"map\":\"map\",\"gameHash\":\"\",\"frameCount\":0}]")
    }
}