package org.bytekeeper.ctr.scbw

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class UnzipBotTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should extract files into the target folder`() {
        // GIVEN
        val aiDir = tempDir.resolve("bot/AI")

        // WHEN
        unzipBot(zipOf("bot.dll", "sub/data.txt"), aiDir)

        // THEN
        assertThat(aiDir.resolve("bot.dll")).hasContent("bot.dll")
        assertThat(aiDir.resolve("sub/data.txt")).hasContent("sub/data.txt")
    }

    @Test
    fun `should reject entries escaping the target folder`() {
        // GIVEN
        val aiDir = tempDir.resolve("bot/AI")

        // WHEN, THEN
        assertThatThrownBy { unzipBot(zipOf("../../escaped.txt"), aiDir) }
            .isInstanceOf(InvalidBotfileException::class.java)
        assertThat(tempDir.resolve("escaped.txt")).doesNotExist()
    }

    @Test
    fun `should reject absolute entries`() {
        // GIVEN
        val aiDir = tempDir.resolve("bot/AI")
        val outside = tempDir.resolve("absolute.txt")

        // WHEN, THEN
        assertThatThrownBy { unzipBot(zipOf(outside.toString()), aiDir) }
            .isInstanceOf(InvalidBotfileException::class.java)
        assertThat(outside).doesNotExist()
    }

    private fun zipOf(vararg names: String): ZipInputStream {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            names.forEach { name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(name.toByteArray())
                zip.closeEntry()
            }
        }
        return ZipInputStream(ByteArrayInputStream(bytes.toByteArray()))
    }
}
