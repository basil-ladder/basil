package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class FileUtilsTest {
    @Test
    fun testDeleteDir() {
        // GIVEN
        val directory = Files.createTempDirectory("blub")
        Files.createTempFile(directory, "blub2", "suffix")

        // WHEN
        deleteDirectory(directory)

        // THEN
        assertThat(directory).doesNotExist()
    }
}