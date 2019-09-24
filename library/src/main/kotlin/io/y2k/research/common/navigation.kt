package io.y2k.research.common

import kotlinx.collections.immutable.PersistentMap

data class NavItem<T>(
    val state: T,
    val view: Stateful<T>.() -> View
)

data class NavState(val navStack: List<NavItem<*>>)

interface Navigation {
    suspend fun <T> push(x: NavItem<T>)
    suspend fun pop(): Boolean

    companion object {
        lateinit var shared: Navigation
    }
}

fun Stateful<NavState>.view(): View = navItemView(state.navStack.last())

class Navigate<T>(val nav: NavItem<T>) : Eff<Unit> {
    override suspend fun invoke() = Navigation.shared.push(nav)
}

object NavigateBack : Eff<Unit> {
    override suspend fun invoke() {
        Navigation.shared.pop()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Stateful<NavState>.navItemView(navItem: NavItem<T>): PersistentMap<String, Any> {
    val childStateful: Stateful<T> = map(
        { it.navStack.last().state as T },
        { db, a: T ->
            val (x, xs) = db.navStack.last() to db.navStack.dropLast(1)
            val x2 = (x as NavItem<T>).copy(state = a)
            db.copy(navStack = xs + x2)
        }
    )
    return navItem.view(childStateful)
}
