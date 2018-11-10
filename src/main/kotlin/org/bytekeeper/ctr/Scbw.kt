package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

class ScbwRunner(
    val bots: List<String>,
    val dockerOpts: String? = "--cpu-quota=1 --memory=1G",
    val timeout: Int? = 600,
    val botDir: String? = null,
    val gameDir: String? = null,
    val readOverwrite: Boolean? = true,
    val gameSpeed: Int? = null
) {
    private val log = LogManager.getLogger()

    fun run() {
        log.info("Upcoming: ${bots[0]} vs ${bots[1]}")
        val cmd = mutableListOf("scbw.play", "--headless")

        fun addParameter(par: String, value: Any?) {
            if (value == null) return
            cmd += par;
            cmd += value.toString()
        }

        cmd += "--bots"
        cmd += bots
        addParameter("--timeout", timeout)
        addParameter("--bot_dir", botDir)
        addParameter("--game_dir", gameDir)
        addParameter("--opt", dockerOpts)
        addParameter("--game_speed", gameSpeed)
        if (readOverwrite == true) cmd += "--read_overwrite"
        val process = Runtime.getRuntime().exec(cmd.toTypedArray())
        val exited = process.waitFor(15, TimeUnit.MINUTES)
        if (!exited) {
            process.destroyForcibly()
        }
    }
}

