package org.bytekeeper.ctr

import java.io.InputStream
import java.net.URL

fun download(url: String): InputStream {
    val connection = URL(url).openConnection()
    connection.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
    )
    connection.connectTimeout = 10000
    connection.readTimeout = 10000

    return connection.getInputStream()
}