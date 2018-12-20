package org.bytekeeper.ctr

import org.bytekeeper.ctr.entity.Race
import java.time.Instant

class TestBotInfo : BotInfo {
    override var name: String = ""
    override var publishReadDirectory: Boolean = false
    override var authorKey: String? = null
    override var clearReadDirectory: Boolean = false
    override var disabled: Boolean = false
    override var race: Race = Race.PROTOSS
    override var botType: String = ""
    override var lastUpdated: Instant = Instant.MIN

}