package org.bytekeeper.ctr

import java.util.concurrent.TimeUnit

object Docker {
    fun retrieveContainersWithName(name: String) =
            ProcessBuilder(listOf("docker", "ps", "-a", "-f", "name=$name", "-q"))
                    .redirectErrorStream(true)
                    .start()
                    .inputStream
                    .bufferedReader()
                    .useLines { it.toList() }

    fun killContainer(nameOrId: String): Process =
            Runtime.getRuntime().exec(arrayOf("docker", "rm", "vf", nameOrId))

    fun updateResourceConstraints(nameOrId: String) {
        val exited = Runtime.getRuntime().exec(arrayOf("docker", "update", "--cpus", "1", "--memory", "1G", nameOrId))
                .waitFor(15, TimeUnit.SECONDS)
        if (!exited) throw FailedToLimitResources("Failed to limit resources for $nameOrId")
    }
}