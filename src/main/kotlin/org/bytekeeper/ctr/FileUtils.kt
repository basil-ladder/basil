package org.bytekeeper.ctr

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

val userHome = System.getProperty("user.home")
