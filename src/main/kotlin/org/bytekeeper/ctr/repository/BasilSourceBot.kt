package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class BasilSourceBot(@Id @GeneratedValue var id: Long? = null,
                     var name: String,
                     var lastUpdated: Instant?,
                     var hash: String)

interface BasilSourceBotRepository : CrudRepository<BasilSourceBot, Long> {
    @Timed
    fun findByName(name: String): BasilSourceBot?
}
