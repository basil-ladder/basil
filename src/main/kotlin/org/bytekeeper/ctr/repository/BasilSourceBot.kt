package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class BasilSourceBot(@Id @GeneratedValue var id: Long? = null,
                     var name: String,
                     var lastUpdated: Instant?,
                     var hash: String)

interface BasilSourceBotRepository : CrudRepository<BasilSourceBot, Long> {
    @Timed
    fun findByName(name: String): BasilSourceBot?
}
