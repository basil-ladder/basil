package org.bytekeeper.ctr

import com.beust.klaxon.Klaxon
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

fun deleteDirectory(path: Path) {
    if (!path.toFile().exists()) return
    Files.walk(path)
        .sorted(Comparator.reverseOrder())
        .use {
            it.forEach(Files::delete)
        }
}

val klaxon = Klaxon()
val userHome = System.getProperty("user.home")
