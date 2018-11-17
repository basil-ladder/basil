package org.bytekeeper.ctr

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Ctr {
    val ctrPath = Paths.get(Config.workDir)
    val botPath = ctrPath.resolve("bots")

    fun botDir(name: String): Path {
        val result = botPath.resolve(name)
        if (!result.toFile().exists())
            Files.createDirectories(result)
        return result
    }

    fun botDirs() = botPath.toList()
}