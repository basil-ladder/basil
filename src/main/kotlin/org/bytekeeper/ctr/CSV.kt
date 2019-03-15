package org.bytekeeper.ctr

object CSV {
    fun parseLine(line: String): List<String> =
            line.split(",\\s*(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    .map { if (it.startsWith('"')) it.substring(1, it.length - 1) else it }
}