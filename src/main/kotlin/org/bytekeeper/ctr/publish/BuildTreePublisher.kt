package org.bytekeeper.ctr.publish

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.annotation.Timed
import org.bytekeeper.ctr.CommandHandler
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.UnitType
import org.bytekeeper.ctr.UnitType.*
import org.bytekeeper.ctr.buildorder.*
import org.bytekeeper.ctr.repository.GameResult
import org.bytekeeper.ctr.repository.UnitEvent
import org.bytekeeper.ctr.repository.UnitEventType
import org.bytekeeper.ctr.repository.UnitEventsRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.EntityNotFoundException
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

@Component
class BuildTreePublisher(private val unitEventsRepository: UnitEventsRepository,
                         private val publisher: Publisher,
                         private val entityManager: EntityManager,
                         private val boMatcher: BOMatcher) {
    @CommandHandler
    @Timed
    @Transactional(readOnly = true)
    fun handle(command: PreparePublish) {
        val root = Node()
        unitEventsRepository.findAllByFrameBetweenAndEventInOrderByGameAsc(2, 12000, listOf(UnitEventType.UNIT_CREATE, UnitEventType.UNIT_MORPH))
                .use { stream ->
                    var lastGame: GameResult? = null
                    val currentGame = mutableListOf<UnitEvent>()
                    stream.forEach { event ->
                        entityManager.detach(event)
                        try {
                            if (event.game != lastGame) {
                                if (currentGame.isNotEmpty()) {
                                    addEvents(root, currentGame)
                                    currentGame.clear()
                                }
                                lastGame?.let(entityManager::detach)
                                lastGame = event.game
                            }
                            currentGame += event
                        } catch (e: EntityNotFoundException) {
                        }
                    }
                    if (currentGame.isNotEmpty()) addEvents(root, currentGame)
                }
        val result = retrieveBORows(root)
        val writer = jacksonObjectMapper().writer()
        publisher.globalStatsWriter("top_bos.json")
                .use { out ->
                    writer.writeValue(out, result)
                }
    }

    private fun addEvents(root: Node, currentGame: MutableList<UnitEvent>) {
        var botANode = root
        var botBNode = root
        val event = currentGame.first()
        val botA = event.bot
        val botAWon = botA == event.game.winner
        val botBWon = botA == event.game.loser
        currentGame.sortBy { it.frame }
        currentGame.forEach { evt ->
            when (evt.unitType) {
                ZERG_LURKER_EGG, ZERG_EGG, ZERG_LARVA, TERRAN_VULTURE_SPIDER_MINE, SPELL_SCANNER_SWEEP, SPELL_DARK_SWARM, SPELL_DISRUPTION_WEB
                -> Unit
                else -> {
                    if (evt.bot == botA) {
                        botANode = botANode.add(evt, botAWon, botBWon)
                    } else {
                        botBNode = botBNode.add(evt, botBWon, botAWon)
                    }
                }
            }
        }
    }

    private fun retrieveBORows(root: Node): List<BORow> {
        val result = mutableListOf<BORow>()
        root.flatten(result, root.count / 500, root.count / 700)
        result.forEach {
            val bo = it.buildOrder.map { it.unitType }
            it.name = when {
                boMatcher.isTwoGate(bo) -> "2 Gate"
                boMatcher.isOneGateGoon(bo) -> "1 Gate Goon"
                boMatcher.is4Pool(bo) -> "4 Pool"
                boMatcher.is5Pool(bo) -> "5 Pool"
                boMatcher.is9Pool(bo) -> "9 Pool"
                boMatcher.is2Hatch(bo) -> "2 Hatch"
                boMatcher.is1Fac(bo) -> "1 Fac"
                boMatcher.is8Rax(bo) -> "8 Rax"
                else -> null
            }
        }
        return result.sortedByDescending { it.amount }
    }

    class Node(private var child: MutableMap<UnitType, Node>? = null, var count: Int = 1, var minFrame: Int = Int.MAX_VALUE, var maxFrame: Int = Int.MIN_VALUE, var won: Int = 0, var lost: Int = 0) {
        fun add(event: UnitEvent, won: Boolean, lost: Boolean): Node {
            val unitType = event.unitType
            count++
            if (won) this.won++
            if (lost) this.lost++

            fun newNode() = Node(won = if (won) 1 else 0, lost = if (lost) 1 else 0)

            val childNode = if (child == null) {
                child = HashMap(3)
                val node = newNode()
                child!![unitType] = node
                node
            } else
                child!!.computeIfAbsent(unitType) { newNode() }
            childNode.maxFrame = max(childNode.maxFrame, event.frame)
            childNode.minFrame = min(childNode.minFrame, event.frame)
            return childNode
        }

        fun flatten(target: MutableList<BORow>, alpha: Int, beta: Int, row: Deque<BOEntry> = ArrayDeque(), wasRelevantUnit: Boolean = true): Boolean {
            if (child == null || count <= alpha) {
                if (wasRelevantUnit) {
                    target += toBO(row, count, won, lost)
                    return true
                }
                return false
            }
            val addedChild = child!!.entries
                    .map { (type, child) ->
                        (child.count >= beta) && run {
                            val isRelevantChild = relevantUnit(type) && row.none { it.unitType == type }
                            row += BOEntry(type, child.minFrame, child.maxFrame)
                            val added = child.flatten(target, alpha, beta, row, isRelevantChild)
                            row.removeLast()
                            added
                        }
                    }.any { it }
            if (!addedChild && wasRelevantUnit) {
                target += toBO(row, count, won, lost)
                return true
            }
            return addedChild
        }

        private fun toBO(row: Collection<BOEntry>, count: Int, won: Int, lost: Int): BORow {
            return BORow(row.toList(), count, won, lost)
        }

        private fun relevantUnit(type: UnitType) =
                when (type) {
                    TERRAN_SCV, PROTOSS_PROBE, ZERG_DRONE, PROTOSS_ASSIMILATOR, PROTOSS_GATEWAY, PROTOSS_FORGE, PROTOSS_PHOTON_CANNON,
                    ZERG_CREEP_COLONY, ZERG_SPORE_COLONY, ZERG_SUNKEN_COLONY, ZERG_EXTRACTOR,
                    ZERG_HYDRALISK_DEN, TERRAN_REFINERY, TERRAN_BARRACKS,
                    PROTOSS_PYLON, TERRAN_SUPPLY_DEPOT, ZERG_OVERLORD -> false
                    else -> true
                }
    }

    class BOEntry(var unitType: UnitType, val minFrame: Int, val maxFrame: Int) {
        override fun toString(): String = "${unitType.short()} (${asTime(minFrame)} - ${asTime(maxFrame)})"

        private fun asTime(frame: Int) = "%d:%02d".format(frame / 24 / 60, (frame / 24) % 60)
    }

    class BORow(@JsonIgnore val buildOrder: List<BOEntry>, val amount: Int, val won: Int, val lost: Int, var name: String? = null) {
        val boString = buildOrder.joinToString()
        override fun toString(): String = "$buildOrder: $amount ($won - $lost)"
    }
}

@Component
class BOMatcher {
    fun isTwoGate(bo: List<UnitType>) = TWO_GATE.matches(bo)
    fun isOneGateGoon(bo: List<UnitType>) = ONE_GATE_GOON.matches(bo)
    fun is4Pool(bo: List<UnitType>) = _4_POOL.matches(bo)
    fun is5Pool(bo: List<UnitType>) = _5_POOL.matches(bo)
    fun is2Hatch(bo: List<UnitType>) = _2_HATCH.matches(bo)
    fun is8Rax(bo: List<UnitType>) = _8_RAX.matches(bo)
    fun is1Fac(bo: List<UnitType>) = _1_FAC.matches(bo)
    fun is9Pool(bo: List<UnitType>) = _9_POOL.matches(bo)

    companion object {
        private val WILDCARD = ZeroOrMore(AnyItem)
        private val WORKER_OR_SUPPLY = ZeroOrMore(One(TERRAN_SCV, ZERG_DRONE, PROTOSS_PROBE, PROTOSS_PYLON, ZERG_OVERLORD, TERRAN_SUPPLY_DEPOT))
        private val TWO_GATE = Seq(WILDCARD, One(PROTOSS_GATEWAY), ZeroOrMore(AnyItem), One(PROTOSS_GATEWAY), WILDCARD).matcher()
        private val ONE_GATE_GOON = Seq(ZeroOrMore(No(PROTOSS_ZEALOT)), One(PROTOSS_DRAGOON), WILDCARD).matcher()
        private val _4_POOL = Seq(One(ZERG_SPAWNING_POOL), Opt(One(ZERG_DRONE)), One(ZERG_ZERGLING), WILDCARD).matcher()
        private val _5_POOL = Seq(One(ZERG_DRONE), One(ZERG_SPAWNING_POOL), Opt(One(ZERG_DRONE)), One(ZERG_ZERGLING), WILDCARD).matcher()
        private val _2_HATCH = Seq(WILDCARD, One(ZERG_HATCHERY), WILDCARD).matcher()
        private val _8_RAX = Seq(WORKER_OR_SUPPLY, One(TERRAN_BARRACKS), WORKER_OR_SUPPLY, One(TERRAN_MARINE), WILDCARD).matcher()
        private val _1_FAC = Seq(WORKER_OR_SUPPLY, One(TERRAN_BARRACKS), WORKER_OR_SUPPLY, One(TERRAN_REFINERY), WORKER_OR_SUPPLY, One(TERRAN_FACTORY), WILDCARD).matcher()
        private var _9_POOL = Seq(Times(5, One(ZERG_DRONE)), One(ZERG_SPAWNING_POOL))
    }
}