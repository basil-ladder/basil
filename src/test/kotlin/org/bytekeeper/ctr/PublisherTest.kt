package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class PublisherTest {
    @TempDir
    lateinit var tempDir: Path
    private val config = Config()
    private lateinit var sut: Publisher

    private val commands: Commands = mock()

    @BeforeEach
    fun setup() {
        config.publishBasePath = tempDir
        config.dataBasePath = tempDir
        sut = Publisher(config, commands)
    }

    @Test
    fun `should create file in bot stats dir`() {
        // GIVEN

        // WHEN
        sut.botStatsWriter("blub", "blob").close()

        // THEN
        assertThat(tempDir.resolve("stats").resolve("blub").resolve("blob")).exists()
    }
}