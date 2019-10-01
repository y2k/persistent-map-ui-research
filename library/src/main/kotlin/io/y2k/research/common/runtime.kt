@file:Suppress("NonAsciiCharacters", "ClassName", "FunctionName")

package io.y2k.research.common

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

interface Stateful<State> : CoroutineScope {
    val state: State
    fun <T> dispatch(f: (State) -> Pair<State, T>): T
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StatefulWrapper<State>(
    override var state: State, scope: CoroutineScope
) : Stateful<State>, CoroutineScope by scope {

    private val channel =
        BroadcastChannel<Unit>(Channel.CONFLATED)

    fun makeListener() = channel.openSubscription()

    override fun <T> dispatch(f: (State) -> Pair<State, T>): T {
        val (s, r) = f(state)
        if (state != s) {
            state = s
            channel.offer(Unit)
        }
        return r
    }
}

inline fun <State> Stateful<State>.replace(crossinline f: (State) -> State) =
    dispatch { f(it) to Unit }

fun <D, CD> Stateful<D>.map(g: (D) -> CD, f2: (D, CD) -> D): Stateful<CD> = run {
    val stateful = this
    object : Stateful<CD>, CoroutineScope by stateful {
        override val state: CD
            get() = g(stateful.state)

        override fun <T> dispatch(f: (CD) -> Pair<CD, T>): T =
            stateful.dispatch { db ->
                val locDb = g(db)
                val (newLocDb, t) = f(locDb)
                if (newLocDb == locDb) db to t
                else f2(db, newLocDb) to t
            }
    }
}

inline fun <D> Stateful<D>.update(crossinline f: (D) -> Pair<D, Set<Eff<*>>>) {
    val xs = dispatch { f(it) }
    launch {
        xs.forEach { e ->
            @Suppress("UNCHECKED_CAST")
            if (e is ComposeEffect<*, *>) (e as ComposeEffect<*, D>).store = this@update
            e()
        }
    }
}

@Deprecated("")
fun <D, T> Stateful<D>.subscribeEffect(eff: Eff<T>, f: (D, Result<T>) -> D) {
    launch {
        val x = runCatching { eff() }
        replace { db -> f(db, x) }
    }
}

// FIXME:
fun <D> Stateful<D>.onStarted(effs: Set<Eff<*>>) = Unit

const val type = "@"
const val children = "@children"
const val memo = "@memo"

class λ<T>(val f: (T) -> Unit) {

    private val id = run {
        val e = Exception().stackTrace[1]
        "${e.fileName}:${e.lineNumber}"
    }

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        val o = other as? λ<*> ?: return false
        return id == o.id
    }
}

inline fun λ(crossinline f: () -> Unit) = λ<Any> { f() }

inline fun <T : Any> memo(value: T, crossinline f: (T) -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(memo to value, "@fabric" to MemoViewFactory { f(value) }, type to "MemoViewGroup")

inline fun freeze(crossinline f: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    memo(Unit) { f() }

class MemoViewFactory(val f: () -> PersistentMap<String, Any>) {
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = true
}
