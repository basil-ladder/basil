package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.support.io.TempDirectory
import java.nio.file.Path

@ExtendWith(TempDirectory::class)
internal class PublisherTest(@TempDirectory.TempDir private val tempDir: Path) {
    private val config = Config()
    private lateinit var sut: Publisher

    @BeforeEach
    fun setup() {
        config.publishBasePath = tempDir
        config.dataBasePath = tempDir
        sut = Publisher(config)
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