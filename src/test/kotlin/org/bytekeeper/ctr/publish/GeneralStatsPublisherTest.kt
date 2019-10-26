package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.BotUpdater
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.repository.BotRepository
import org.bytekeeper.ctr.repository.GameResultRepository
import org.bytekeeper.ctr.repository.UnitEventsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedWriter
import java.io.StringWriter
import java.time.LocalDateTime

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
    private lateinit var botUpdater: BotUpdater

    @Mock
    private lateinit var unitEventsRepository: UnitEventsRepository

    private val writer: StringWriter = StringWriter()

    @BeforeEach
    fun setup() {
        sut = GeneralStatsPublisher(botUpdater, gameResultRepository, botRepository, unitEventsRepository, publisher)

        given(publisher.globalStatsWriter(ArgumentMatchers.anyString()))
                .willReturn(BufferedWriter(writer))
    }

    @Test
    fun `should publish next update time`() {
        // GIVEN
        given(botUpdater.nextBotUpdateTime).willReturn(1L)

        // WHEN
        sut.handle(PreparePublish(LocalDateTime.of(2019, 10, 25, 0, 0)))

        // THEN
        assertThat(jacksonObjectMapper().readValue<GeneralStatsPublisher.PublishedStats>(writer.toString()))
                .extracting { it.nextUpdateTime }
                .isEqualTo(1L)

    }

    @Test
    fun `should skip publish below 6h`() {
        // GIVEN
        given(botUpdater.nextBotUpdateTime).willReturn(1L, 10L)
        sut.handle(PreparePublish(LocalDateTime.of(2019, 10, 25, 0, 0)))

        // WHEN
        sut.handle(PreparePublish(LocalDateTime.of(2019, 10, 25, 5, 59)))

        // THEN
        assertThat(jacksonObjectMapper().readValue<GeneralStatsPublisher.PublishedStats>(writer.toString()))
                .extracting { it.nextUpdateTime }
                .isEqualTo(1L)
    }

    @Test
    fun `should publish a week later`() {
        // GIVEN
        given(botUpdater.nextBotUpdateTime).willReturn(1L, 10L)
        sut.handle(PreparePublish(LocalDateTime.of(2019, 10, 18, 0, 0)))
        val secondWriter = StringWriter()
        given(publisher.globalStatsWriter(ArgumentMatchers.anyString()))
                .willReturn(BufferedWriter(secondWriter))


        // WHEN
        sut.handle(PreparePublish(LocalDateTime.of(2019, 10, 25, 0, 0)))

        // THEN
        assertThat(jacksonObjectMapper().readValue<GeneralStatsPublisher.PublishedStats>(secondWriter.toString()))
                .extracting { it.nextUpdateTime }
                .isEqualTo(10L)

    }
}