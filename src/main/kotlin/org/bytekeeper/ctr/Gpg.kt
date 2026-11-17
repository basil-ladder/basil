package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.util.concurrent.TimeUnit

object Gpg {
    private val log = LogManager.getLogger()

    fun ensureKeyIsPresent(keyId: String) {
        val checkForExistingKeyProcess = ProcessBuilder(listOf(
                "gpg",
                "--batch",
                "-k", keyId
        )).redirectErrorStream(true)
                .start().also {
                    val stopped = it.waitFor(5, TimeUnit.SECONDS)
                    if (!stopped) {
                        it.destroyForcibly()
                        throw FailedToEnsureKey("Could not establish presence of $keyId")
                    }
                }
        if (checkForExistingKeyProcess.exitValue() != 0) {
            log.info("Key $keyId not yet downloaded, grabbing it.")
            ProcessBuilder(listOf(
                    "gpg",
                    "--batch",
                    "--keyserver", "pgp.mit.edu",
                    "--recv-keys", keyId
            )).redirectErrorStream(true)
                    .start().also {
                        val stopped = it.waitFor(30, TimeUnit.SECONDS)
                        if (!stopped) {
                            it.destroyForcibly()
                            throw FailedToEnsureKey("Retrieving key $keyId timed out after 30s.")
                        }
                        if (it.exitValue() != 0) {
                            throw FailedToEnsureKey("Key $keyId was not found on the key server.")
                        }
                    }
            log.info("Key $keyId retrieved.")
        }

    }

    fun encryptFile(source: Path, target: Path, keyId: String) {
        ProcessBuilder(listOf(
                "gpg",
                "--batch",
                "--yes",
                "--trust-model", "ALWAYS",
                "--output", target.toString(),
                "--encrypt",
                "--recipient", keyId, source.toString()))
                .start()
                .also {
                    val stopped = it.waitFor(1, TimeUnit.MINUTES)
                    if (!stopped) {
                        it.destroyForcibly()
                        throw FailedToEncrypt("Encryption still ran after 1min for key $keyId, aborting.")
                    }
                    if (it.exitValue() != 0) {
                        throw FailedToEncrypt("Encryption with key $keyId failed!")
                    }
                }
    }
}

class FailedToEnsureKey(message: String) : RuntimeException(message)
class FailedToEncrypt(message: String) : RuntimeException(message)