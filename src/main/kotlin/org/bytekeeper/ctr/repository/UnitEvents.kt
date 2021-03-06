package org.bytekeeper.ctr.repository

import io.micrometer.core.annotation.Timed
import org.bytekeeper.basil.proto.GameResultOuterClass
import org.bytekeeper.ctr.UnitType
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*
import java.util.stream.Stream
import javax.persistence.*


enum class UnitEventType {
    INVALID,
    UNIT_CREATE,
    UNIT_COMPLETE,
    UNIT_SHOW,
    UNIT_HIDE,
    UNIT_RENEGADE,
    UNIT_DESTROY,
    UNIT_MORPH;

    companion object {
        fun fromLogEvent(event: String) = when (event) {
            "unitCreate" -> UNIT_CREATE
            "unitComplete" -> UNIT_COMPLETE
            "unitShow" -> UNIT_SHOW
            "unitHide" -> UNIT_HIDE
            "unitRenegade" -> UNIT_RENEGADE
            "unitDestroy" -> UNIT_DESTROY
            "unitMorph" -> UNIT_MORPH
            else -> INVALID
        }
    }
}

@Entity
class UnitEvent(val frame: Int,
                val event: UnitEventType,
                @ManyToOne(fetch = FetchType.LAZY) val game: GameResult,
                @ManyToOne(fetch = FetchType.LAZY) val bot: Bot,
                val unitId: Short,
                val unitType: UnitType,
                val posX: Short,
                val posY: Short) {
    @Id
    @GeneratedValue
    var id: Long? = null
}

fun GameResultOuterClass.BotResult.Builder.addFromUnitEvent(e: UnitEvent) =
        also {
            addUnitEventsBuilder()
                    .setFrame(e.frame)
                    .setPosX(e.posX.toInt())
                    .setPosY(e.posY.toInt())
                    .setUnitEventType(e.event.ordinal)
                    .setUnitType(e.unitType.ordinal)
                    .setUnitId(e.unitId.toInt())
        }

data class UnitStats(val type: UnitType, val event: UnitEventType, val amount: Long)
data class Nuke(val frame: Int, val posX: Short, val posY: Short)
data class GameEvent(val game: UUID, val unitType: UnitType, val event: UnitEventType, val amount: Long)

interface UnitEventsRepository : CrudRepository<UnitEvent, Long> {
    @Query("SELECT new org.bytekeeper.ctr.repository.UnitStats(unitType, event, count(*))" +
            " FROM UnitEvent GROUP BY unitType, event")
    @Timed
    fun globalUnitStats(): List<UnitStats>

    @Query("SELECT new org.bytekeeper.ctr.repository.GameEvent(game.id, unitType, event, count(*))" +
            " FROM UnitEvent WHERE game IN :gameResults AND event in (1, 2, 6, 7)" +
            " GROUP BY game.id, unitType, event HAVING count(*) >= 8")
    @Timed
    fun aggregateGameEventsWith8OrMoreEvents(@Param("gameResults") games: List<GameResult>): List<GameEvent>

    @Timed
    @QueryHints(QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "50"))
    fun findAllByFrameBetweenAndEventInOrderByGameAsc(minExcl: Int, maxExcl: Int, events: List<UnitEventType>): Stream<UnitEvent>
}
