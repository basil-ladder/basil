package org.bytekeeper.ctr

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

fun deleteDirectory(path: Path) {
    if (!path.toFile().exists()) return
    Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .use {
                it.forEach(Files::delete)
            }
}

fun createCompressedFile(targetFile: Path, sourceFolder: Path) =
        SevenZOutputFile(targetFile.toFile())
                .use { outFile ->
                    Files.walk(sourceFolder)
                            .filter { Files.isRegularFile(it) }
                            .forEach { path ->
                                val entry = outFile.createArchiveEntry(path.toFile(), sourceFolder.relativize(path).toString())
                                outFile.putArchiveEntry(entry)
                                outFile.write(Files.readAllBytes(path))
                                outFile.closeArchiveEntry()
                            }
                }
