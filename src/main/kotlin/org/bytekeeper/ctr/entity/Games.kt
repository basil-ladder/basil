package org.bytekeeper.ctr.entity

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class GameResult(@Id @GeneratedValue var id: Long? = null,
                 var time: Instant,
                 var gameRealtime: Double,
                 var realtimeTimeout: Boolean = false,
                 var map: String,
                 @ManyToOne var botA: Bot,
                 @ManyToOne var botB: Bot,
                 @ManyToOne var winner: Bot? = null,
                 @ManyToOne var loser: Bot? = null,
                 var botACrashed: Boolean = false,
                 var botBCrashed: Boolean = false,
                 var gameHash: String)

interface GameResultRepository : CrudRepository<GameResult, Long> {
    @EntityGraph(attributePaths = ["botA", "botB", "winner", "loser"])
    override fun findAll(): MutableIterable<GameResult>
}