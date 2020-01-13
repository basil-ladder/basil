package org.bytekeeper.ctr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.zip.ZipInputStream
import kotlin.streams.asSequence

class BasilSourceTest() {
    @TempDir
    lateinit var tempDir: Path
    private val config: Config = Config()
    private lateinit var sut: BasilSource

    val botZip = BasilSourceTest::class.java.getResource("/bot.zip")

    private val basilBotService: BasilBotService = mock<BasilBotService>()

    private val webClient: RedirectingWebClient = mock<RedirectingWebClient>()

    private var bufferProvider: (uri: URI) -> Mono<DataBuffer> = { uri ->
        DataBufferUtils.readInputStream({ uri.toURL().openStream() },
                DefaultDataBufferFactory(),
                4096)
                .toMono()
    }

    @BeforeEach
    fun setup() {
        val writer = jacksonObjectMapper().writer()
        config.basilBotSource = tempDir.resolve("basilBots.json")
        Files.write(config.basilBotSource, listOf(writer.writeValueAsString(listOf(
                BasilSource.BotInfo(name = "testBot", botBinary = botZip.toString(), botType = "MIRROR", raceValue = "Terran")))))
        given(basilBotService.update(eq("testBot"), anyString(), any())).willAnswer { it.arguments[2] }
        given(webClient.get(any())).willAnswer {
            val uri = it.arguments[0] as URI
            val clientResponse = mock<ClientResponse>()
            given(clientResponse.bodyToMono(any<Class<DataBuffer>>())).willReturn(bufferProvider(uri))
            clientResponse.toMono()
        }

        sut = BasilSource(config, basilBotService, webClient)
        sut.refresh()
    }

    @Test
    fun `should update on refresh`() {
        // GIVEN
        // refreshed

        // WHEN

        // THEN
        verify(basilBotService).update(eq("testBot"), eq("18A46E5BA54DEA5B06A4EF6462E14633"), any())
    }

    @Test
    fun `should have same hash on second refresh`() {
        // GIVEN
        sut.refresh()

        // WHEN

        // THEN
        verify(basilBotService, times(2)).update(eq("testBot"), eq("18A46E5BA54DEA5B06A4EF6462E14633"), any())
    }

    @Test
    fun `bots should be enabled by default`() {
        // GIVEN

        // WHEN
        val botInfo = sut.botInfoOf("testBot")!!

        // THEN
        assertThat(botInfo).hasFieldOrPropertyWithValue("disabled", false)
    }

    @Test
    fun `bots should be downloaded`() {
        // GIVEN
        val botInfo = sut.botInfoOf("testBot")!!

        // WHEN
        val downloadBinary = sut.downloadBinary(botInfo)!!

        // THEN
        val lines = ZipInputStream(downloadBinary).use { zip ->
            generateSequence { zip.nextEntry }
                    .flatMap {
                        BufferedReader(InputStreamReader(zip)).lines().asSequence()
                    }.toList()
        }
        assertThat(lines).contains("BOT BINARY")
    }

    @Test
    fun `bot binary should be downloaded`() {
        // GIVEN
        val botInfo = sut.botInfoOf("testBot")!!

        // WHEN
        val downloadBinary = sut.downloadBwapiDLL(botInfo)!!

        // THEN
        downloadBinary.use {
            assertThat(it).hasContent("BLUB")
        }
    }

    @Test
    fun `bot binary should be ignored if too large`() {
        // GIVEN
        val botInfo = sut.botInfoOf("testBot")!!
        bufferProvider = {
            DataBufferUtils.readInputStream({ ByteArrayInputStream(ByteArray(120 * 1024 * 1024)) },
                    DefaultDataBufferFactory(),
                    120 * 1024 * 1024)
                    .toMono()
        }
        sut.refresh()

        // WHEN
        val downloadBinary = sut.downloadBwapiDLL(botInfo)

        // THEN
        assertThat(downloadBinary).isNull()
    }

    @Test
    fun `should not change lastUpdated on error`() {
        // GIVEN
        bufferProvider = { Mono.error(FailedToDownloadBot("No wai")) }

        // WHEN, THEN
        sut.refresh()
        assertThat(sut.botInfoOf("testBot"))
                .extracting { it?.lastUpdated }
                .isEqualTo(Instant.MIN)
    }

    @Test
    fun `should not change lastUpdated on invalid file`() {
        // GIVEN
        bufferProvider = {
            DataBufferUtils.readInputStream({ ByteArrayInputStream(ByteArray(120)) },
                    DefaultDataBufferFactory(),
                    120)
                    .toMono()
        }

        // WHEN, THEN
        sut.refresh()
        assertThat(sut.botInfoOf("testBot"))
                .extracting { it?.lastUpdated }
                .isEqualTo(Instant.MIN)
        assertThat(Files.list(Paths.get("/tmp")))
                .anyMatch {
                    it.fileName.toString().startsWith("invalid-bot-binary")
                }
    }
}