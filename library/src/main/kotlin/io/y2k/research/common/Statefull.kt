package io.y2k.research.common

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

@Suppress("EXPERIMENTAL_API_USAGE")
class Statefull<State>(var state: State) {

    private val channel =
        BroadcastChannel<Unit>(Channel.CONFLATED)

    fun makeListener() = channel.openSubscription()

    fun <T> dispatch(f: (State) -> Pair<State, T>): T {
        val (s, r) = f(state)
        state = s
        channel.offer(Unit)
        return r
    }
}

const val type = "@"
const val children = "children"
