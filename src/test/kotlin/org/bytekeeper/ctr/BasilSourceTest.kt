package org.bytekeeper.ctr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.support.io.TempDirectory
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.streams.asSequence

@ExtendWith(TempDirectory::class)
class BasilSourceTest(@TempDirectory.TempDir val tempDir: Path) {
    private val config: Config = Config()
    private lateinit var sut: BasilSource


    val botZip = BasilSourceTest::class.java.getResource("/bot.zip")

    private val basilBotService: BasilBotService = mock<BasilBotService>()

    @BeforeEach
    fun setup() {
        val writer = jacksonObjectMapper().writer()
        config.basilBotSource = tempDir.resolve("basilBots.json")
        Files.write(config.basilBotSource, listOf(writer.writeValueAsString(listOf(
                BasilSource.BotInfo(name = "testBot", botBinary = botZip.toString(), botType = "MIRROR", raceValue = "Terran")))))
        sut = BasilSource(config, basilBotService)
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
}