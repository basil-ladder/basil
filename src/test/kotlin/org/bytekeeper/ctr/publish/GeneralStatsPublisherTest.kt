package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.GameRunner
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.entity.BotRepository
import org.bytekeeper.ctr.entity.GameResultRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter

@ExtendWith(MockitoExtension::class)
class GeneralStatsPublisherTest {
    private lateinit var sut: GeneralStatsPublisher

    @Mock
    private lateinit var gameResultRepository: GameResultRepository

    @Mock
    private lateinit var publisher: Publisher

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var gameRunner: GameRunner

    private val writer: StringWriter = StringWriter()

    @BeforeEach
    fun setup() {
        sut = GeneralStatsPublisher(gameRunner, gameResultRepository, botRepository, publisher)

        given(publisher.globalStatsWriter(ArgumentMatchers.anyString()))
                .willReturn(BufferedWriter(writer))
    }

    @Test
    fun `should publish next update time`() {
        // GIVEN
        given(gameRunner.nextBotUpdateTime).willReturn(1L)

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(jacksonObjectMapper().readValue<GeneralStatsPublisher.PublishedStats>(writer.toString()))
                .extracting { it.nextUpdateTime }
                .isEqualTo(1L)

    }
}