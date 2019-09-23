package io.y2k.research.common

import io.y2k.research.AppState
import io.y2k.research.WeatherState
import io.y2k.research.view
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.coroutines.MainScope

fun test() {
    val initState = NavState(
        listOf(
            mkNavItem(AppState(), Stateful<AppState>::view),
            mkNavItem(WeatherState(), Stateful<WeatherState>::view)
        )
    )
    StatefulWrapper(initState, MainScope())
}

interface Navigation {
    suspend fun push(x: Pair<Any, Stateful<Any>.() -> View>)
    suspend fun pop(): Boolean

    companion object {
        lateinit var shared: Navigation

        @Suppress("UNCHECKED_CAST")
        inline fun <T : Any> mkNavItem(initState: T, crossinline f: Stateful<T>.() -> View) =
            Pair<Any, Stateful<Any>.() -> View>(initState, { (this as Stateful<T>).f() })
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T : Any> mkNavItem(initState: T, crossinline f: Stateful<T>.() -> View) =
    Pair<Any, Stateful<Any>.() -> View>(initState, { (this as Stateful<T>).f() })

sealed class Pages {
    object Home : Pages()
    object CreateTodo : Pages()
}

data class NavState(
    val childs: List<Pair<Any, Stateful<Any>.() -> View>>
)

typealias View = PersistentMap<String, Any>
typealias Views = PersistentList<View>

fun Stateful<NavState>.view(): View = run {
    val second = state.childs.last().second

    val childStateful = map(
        { it.childs.last().first },
        { db, a ->
            val (x, xs) = db.childs.last() to db.childs.dropLast(1)
            val x2 = x.copy(first = a)
            db.copy(childs = xs + x2)
        }
    )

    second.invoke(childStateful)
}
