package org.bytekeeper.ctr

import java.nio.file.Files
import java.nio.file.Paths

object DisabledBots {
    fun isEnabled(name: String): Boolean {
        val disabledBotsFile = Paths.get(Config.workDir, "disabledBots.txt")
        return !disabledBotsFile.toFile().exists() || Files.lines(disabledBotsFile).noneMatch { it == name }
    }
}