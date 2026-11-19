package org.bytekeeper.ctr.scbw

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class CopyWriteDirTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should copy files and folders but not links`() {
        // GIVEN
        val secret = Files.writeString(tempDir.resolve("secret.txt"), "secret")
        val secretDir = Files.createDirectories(tempDir.resolve("secretDir"))
        Files.writeString(secretDir.resolve("key"), "key")
        val writeDir = Files.createDirectories(tempDir.resolve("write"))
        Files.writeString(Files.createDirectories(writeDir.resolve("sub")).resolve("learned.txt"), "learned")
        Files.createSymbolicLink(writeDir.resolve("stolen.txt"), secret)
        Files.createSymbolicLink(writeDir.resolve("stolenDir"), secretDir)
        val readDir = Files.createDirectories(tempDir.resolve("read"))

        // WHEN
        copyWriteDir(writeDir, readDir)

        // THEN
        assertThat(readDir.resolve("sub/learned.txt")).hasContent("learned")
        assertThat(readDir.resolve("stolen.txt")).doesNotExist()
        assertThat(readDir.resolve("stolenDir")).doesNotExist()
    }
}
