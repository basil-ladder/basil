package org.bytekeeper.ctr

import java.util.concurrent.TimeUnit

class ScbwRunner(
    val bots: List<String>,
    val dockerOpts: String? = "--cpu-quota=1 --memory=1G",
    val timeout: Int? = null,
    val botDir: String? = null,
    val gameDir: String? = null,
    val readOverwrite: Boolean? = null,
    val gameSpeed: Int? = null
) {
    fun run() {
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

