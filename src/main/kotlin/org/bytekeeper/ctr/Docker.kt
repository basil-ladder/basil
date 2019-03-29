package org.bytekeeper.ctr

object Docker {
    fun retrieveContainersWithName(name: String) =
            ProcessBuilder(listOf("docker", "ps", "-a", "-f", "name=$name", "-q"))
                    .redirectErrorStream(true)
                    .start()
                    .inputStream
                    .bufferedReader()
                    .useLines { it.toList() }

    fun killContainer(nameOrId: String): Process =
            Runtime.getRuntime().exec(arrayOf("docker", "rm", "-vf", nameOrId))
}