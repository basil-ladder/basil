package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.UnitType
import org.bytekeeper.ctr.repository.UnitEvent
import org.bytekeeper.ctr.repository.UnitEventType
import org.bytekeeper.ctr.repository.UnitEventsRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

@Component
class BuildTreePublisher(private val unitEventsRepository: UnitEventsRepository,
                         private val publisher: Publisher,
                         private val entityManager: EntityManager) {
    @CommandHandler
    @Timed
    @Transactional(readOnly = true)
    fun handle(command: PreparePublish) {
        val root = Node()
        unitEventsRepository.findAllByFrameBetweenAndEventInOrderByGameAscFrameAsc(2, 9000, listOf(UnitEventType.UNIT_CREATE, UnitEventType.UNIT_MORPH))
                .use { stream ->
                    var lastGame = UUID(0, 0)
                    var botA: Long? = null
                    var botANode = root
                    var botBNode = root
                    stream.forEach { event ->
                        entityManager.detach(event)
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
                                    botANode = botANode.add(event)
                                } else {
                                    botBNode = botBNode.add(event)
                                }
                            }
                        }
                    }
                }
        val result = retrieveBORows(root);
        val writer = jacksonObjectMapper().writer().without(JsonGenerator.Feature.QUOTE_FIELD_NAMES)
        publisher.globalStatsWriter("top_bos.json")
                .use { out ->
                    writer.writeValue(out, result)
                }
    }

    private fun retrieveBORows(root: Node) {
        val result = mutableListOf<BORow>()
        root.flatten(result, root.count / 500, root.count / 700)
        return result.sortByDescending { it.amount }
    }

    class Node(private var child: MutableMap<UnitType, Node>? = null, var count: Int = 1, var minFrame: Int = Int.MAX_VALUE, var maxFrame: Int = Int.MIN_VALUE) {
        fun add(event: UnitEvent): Node {
            val unitType = event.unitType
            count++
            val childNode = if (child == null) {
                child = HashMap(3)
                val node = Node()
                child!![unitType] = node
                node
            } else
                child!!.computeIfAbsent(unitType) { Node() }
            childNode.maxFrame = max(childNode.maxFrame, event.frame)
            childNode.minFrame = min(childNode.minFrame, event.frame)
            return childNode
        }

        fun flatten(target: MutableList<BORow>, alpha: Int, beta: Int, row: Deque<BOEntry> = ArrayDeque(), lastType: UnitType = UnitType.NONE, wasRelevantUnit: Boolean = true): Boolean {
            if (child == null || count <= alpha) {
                if (wasRelevantUnit) {
                    target += BORow(row.joinToString(), count)
                    return true
                }
                return false
            }
            val addedChild = child!!.entries
                    .map { (type, child) ->
                        (child.count >= beta) && run {
                            val isRelevantChild = relevantUnit(type) && row.none { it.unitType == type }
                            row += BOEntry(type, child.minFrame, child.maxFrame)
                            val added = child.flatten(target, alpha, beta, row, type, isRelevantChild)
                            row.removeLast()
                            added
                        }
                    }.any { it }
            if (!addedChild && wasRelevantUnit) {
                target += BORow(row.joinToString(), count)
                return true
            }
            return addedChild
        }

        private fun relevantUnit(type: UnitType) =
                when (type) {
                    UnitType.TERRAN_SCV, UnitType.PROTOSS_PROBE, UnitType.ZERG_DRONE, UnitType.PROTOSS_ASSIMILATOR, UnitType.PROTOSS_GATEWAY, UnitType.PROTOSS_FORGE, UnitType.PROTOSS_PHOTON_CANNON,
                    UnitType.ZERG_CREEP_COLONY, UnitType.ZERG_SPORE_COLONY, UnitType.ZERG_SUNKEN_COLONY, UnitType.ZERG_EXTRACTOR,
                    UnitType.ZERG_HYDRALISK_DEN, UnitType.TERRAN_REFINERY, UnitType.TERRAN_BARRACKS,
                    UnitType.PROTOSS_PYLON, UnitType.TERRAN_SUPPLY_DEPOT, UnitType.ZERG_OVERLORD -> false
                    else -> true
                }
    }

    class BOEntry(var unitType: UnitType, val minFrame: Int, val maxFrame: Int) {
        override fun toString(): String = "${unitType.short()} (${asTime(minFrame)} - ${asTime(maxFrame)})"

        private fun asTime(frame: Int) = "%d:%02d".format(frame / 24 / 60, (frame / 24) % 60)
    }

    class BORow(val buildOrder: String, val amount: Int) {
        override fun toString(): String = "$buildOrder: $amount"
    }
}