package org.bytekeeper.ctr.publish

import org.assertj.core.api.Assertions
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.GameResult
import org.bytekeeper.ctr.entity.GameResultRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.StringWriter
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
internal class GameResultsProjectionsTest {
    private lateinit var sut: GameResultsPublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    private val writer: StringWriter = StringWriter()

    @Before
    fun setup() {
        sut = GameResultsPublisher(gameResultRepository, publisher)

        given(publisher.globalStatsWriter(ArgumentMatchers.anyString()))
                .willReturn(writer)
    }

    @Test
    fun shouldRenderGamesWithWinner() {
        // GIVEN
        val botA = Bot(-1, true, "botA")
        val botB = Bot(-1, true, "botB")

        given(gameResultRepository.findAll()).willReturn(mutableListOf(
                GameResult(-1, Instant.MIN, 1.0, false, "map", botA, botB, botA, botB, false, false, "")))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString()).isEqualTo("[{\"botA\":\"botA\",\"botB\":\"botB\",\"winner\":\"botA\",\"loser\":\"botB\",\"realTimeout\":false,\"endedAt\":-31557014167219200,\"map\":\"map\",\"botACrashed\":false,\"botBCrashed\":false,\"gameHash\":\"\"}]")
    }

    @Test
    fun shouldRenderGamesWithCrash() {
        // GIVEN
        val botA = Bot(-1, true, "botA")
        val botB = Bot(-1, true, "botB")

        given(gameResultRepository.findAll()).willReturn(mutableListOf(
                GameResult(-1, Instant.MIN, 1.0, false, "map", botA, botB, botA, botB, true, false, "")))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        Assertions.assertThat(writer.toString()).isEqualTo("[{\"botA\":\"botA\",\"botB\":\"botB\",\"winner\":\"botA\",\"loser\":\"botB\",\"realTimeout\":false,\"endedAt\":-31557014167219200,\"map\":\"map\",\"botACrashed\":true,\"botBCrashed\":false,\"gameHash\":\"\"}]")
    }
}