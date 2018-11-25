package org.bytekeeper.ctr

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.file.Path

@ConfigurationProperties("basil")
@Component
class Config {
    var botUpdateTimer: Long = 6 * 60
    var publishTimer: Long = 30
    lateinit var publishCommand: String
    lateinit var publishBasePath: Path
}