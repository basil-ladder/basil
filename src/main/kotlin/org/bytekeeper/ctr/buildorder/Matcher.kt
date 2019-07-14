package org.bytekeeper.ctr.buildorder


typealias Transition = (Any) -> List<State>

class State(var final: Boolean = false) {
    val transitions: MutableList<Transition> = mutableListOf()
    val epsTransitions = mutableListOf<State>()

    fun compact() {
        val visited = mutableSetOf<State>()
        while (epsTransitions.isNotEmpty()) {
            val newEps = epsTransitions.flatMap { state ->
                visited += state
                if (state.final) final = true
                transitions += state.transitions
                state.epsTransitions
            }.distinct()
            epsTransitions.clear()
            epsTransitions += newEps - visited
        }
    }
}

interface StateDescriptor {
    fun apply(entry: State): State

    fun matches(items: Iterable<Any>): Boolean = matcher().matches(items)

    fun matcher(): Matcher = Matcher(this)
}

class Matcher(desc: StateDescriptor) {
    val start = State()
    val end = desc.apply(start)

    init {
        end.final = true
    }

    fun matches(items: Iterable<Any>): Boolean {
        var states = setOf(start)
        for (item in items) {
            states = states.flatMap { s ->
                s.compact()
                s.transitions.flatMap { t -> t(item) }
            }.toSet()
        }
        return states.any {
            it.compact()
            it.final
        }
    }
}

object AnyItem : StateDescriptor {
    override fun apply(entry: State): State {
        val done = State()
        entry.transitions += { listOf(done) }
        return done
    }
}

class One(vararg val item: Any) : StateDescriptor {
    override fun apply(entry: State): State {
        val done = State()
        entry.transitions += { i -> if (i in item) listOf(done) else emptyList() }
        return done
    }
}

class No(vararg val item: Any) : StateDescriptor {
    override fun apply(entry: State): State {
        val done = State()
        entry.transitions += { i -> if (i !in item) listOf(done) else emptyList() }
        return done
    }
}

class Opt(val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        val out = child.apply(entry)
        entry.epsTransitions += out
        return out
    }
}

class Seq(vararg val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        var last = entry
        for (c in child) {
            val childState = State()
            last.epsTransitions += listOf(childState)
            last = c.apply(childState)

        }
        return last
    }
}

class AtLeastOnce(val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        val done = child.apply(entry)
        done.epsTransitions += entry
        return done
    }
}

class ZeroOrMore(val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        val out = State()
        val done = child.apply(entry)
        done.epsTransitions += listOf(entry)
        done.epsTransitions += listOf(out)
        entry.epsTransitions += listOf(out)
        return out
    }
}

class OneOf(vararg val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        val out = State()
        entry.epsTransitions += child.map { c ->
            val childState = State()
            val childOut = c.apply(childState)
            childOut.epsTransitions += out
            childState
        }
        return out
    }
}

class Times(private val times: Int, private val child: StateDescriptor) : StateDescriptor {
    override fun apply(entry: State): State {
        var out = entry
        repeat(times) {
            out = child.apply(out);
        }
        return out
    }
}

fun between(min: Int, max: Int, child: StateDescriptor) = Seq(Times(min, child), Times(max - min, Opt(child)))