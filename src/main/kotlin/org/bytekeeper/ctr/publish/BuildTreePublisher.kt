package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.UnitType
import org.bytekeeper.ctr.repository.UnitEventType
import org.bytekeeper.ctr.repository.UnitEventsRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class BuildTreePublisher(private val unitEventsRepository: UnitEventsRepository,
                         private val publisher: Publisher) {
    @CommandHandler
    @Timed
    @Transactional(readOnly = true)
    fun handle(command: PreparePublish) {
        val root = Node()
        unitEventsRepository.findAllByFrameBetweenAndEventInOrderByGameAscFrameAsc(2, 8000, listOf(UnitEventType.UNIT_CREATE, UnitEventType.UNIT_MORPH))
                .use { stream ->
                    var lastGame = UUID(0, 0)
                    var botA: Long? = null
                    var botANode = root
                    var botBNode = root
                    stream.forEach { event ->
                        if (event.game.id != lastGame) {
                            botANode = root
                            botBNode = root
                            botA = event.bot.id
                            lastGame = event.game.id
                        }
                        when (event.unitType) {
                            UnitType.ZERG_LURKER_EGG, UnitType.ZERG_EGG, UnitType.ZERG_LARVA, UnitType.TERRAN_VULTURE_SPIDER_MINE, UnitType.SPELL_SCANNER_SWEEP, UnitType.SPELL_DARK_SWARM, UnitType.SPELL_DISRUPTION_WEB
                            -> Unit
                            else -> {
                                if (event.bot.id == botA) {
                                    botANode = botANode.add(event.unitType)
                                } else {
                                    botBNode = botBNode.add(event.unitType)
                                }
                            }
                        }
                    }
                }
        val result = mutableListOf<BORow>()
        root.flatten(result, root.count / 250)
        val writer = jacksonObjectMapper().writer().without(JsonGenerator.Feature.QUOTE_FIELD_NAMES)
        result.sortByDescending { it.amount }
        publisher.globalStatsWriter("top_bos.json")
                .use { out ->
                    writer.writeValue(out, result)
                }
    }

    class Node(private var child: MutableMap<UnitType, Node>? = null, var count: Int = 1) {
        fun add(unitType: UnitType): Node {
            count++
            if (child == null)
                child = mutableMapOf(unitType to Node())
            else
                child!!.computeIfAbsent(unitType) { Node() }
            return child!![unitType]!!
        }

        fun flatten(target: MutableList<BORow>, limit: Int, row: String = "", wasRelevantUnit: Boolean = true) {
            if (child == null || count < limit) {
                if (wasRelevantUnit && count > limit / 2) target += BORow(row.substring(2), count)
                return
            }
            child!!.entries.sortedByDescending { (_, node) -> node.count }
                    .forEach { (type, child) ->
                        child.flatten(target, limit, "$row, ${type.short()}", relevantUnit(type))
                    }
        }

        private fun relevantUnit(type: UnitType) = when (type) {
            UnitType.TERRAN_SCV, UnitType.PROTOSS_PROBE, UnitType.ZERG_DRONE, UnitType.PROTOSS_ASSIMILATOR, UnitType.PROTOSS_GATEWAY, UnitType.PROTOSS_FORGE, UnitType.PROTOSS_PHOTON_CANNON,
            UnitType.ZERG_CREEP_COLONY, UnitType.ZERG_SPORE_COLONY, UnitType.ZERG_SUNKEN_COLONY, UnitType.ZERG_EXTRACTOR,
            UnitType.ZERG_HYDRALISK_DEN, UnitType.TERRAN_REFINERY, UnitType.TERRAN_BARRACKS,
            UnitType.PROTOSS_PYLON, UnitType.TERRAN_SUPPLY_DEPOT, UnitType.ZERG_OVERLORD -> false
            else -> true
        }
    }

    class BORow(val buildOrder: String, val amount: Int)
}