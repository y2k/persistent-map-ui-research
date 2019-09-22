@file:Suppress("NonAsciiCharacters", "ClassName", "FunctionName")

package io.y2k.research.common

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

interface Stateful<State> {
    val state: State
    fun <T> dispatch(f: (State) -> Pair<State, T>): T
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StatefulWrapper<State>(override var state: State) : Stateful<State> {

    private val channel =
        BroadcastChannel<Unit>(Channel.CONFLATED)

    fun makeListener() = channel.openSubscription()

    override fun <T> dispatch(f: (State) -> Pair<State, T>): T {
        val (s, r) = f(state)
        state = s
        channel.offer(Unit)
        return r
    }
}

inline fun <State> Stateful<State>.update(crossinline f: (State) -> State) =
    dispatch { f(it) to Unit }

fun <T, R> Stateful<T>.map(g: (T) -> R, f2: (T, R) -> T): Stateful<R> = run {
    val this_ = this
    object : Stateful<R> {
        override val state: R
            get() = g(this_.state)

        override fun <X> dispatch(f: (R) -> Pair<R, X>): X =
            this_.dispatch { db ->
                val (a, b) = f(g(db))
                f2(db, a) to b
            }
    }
}

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

object Resources {
    var density: Float = 0f
    var button_background: Int = 0
    var button_background_white: Int = 0
    var button_background_round: Int = 0
}