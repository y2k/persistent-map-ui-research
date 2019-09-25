package io.y2k.research.common

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class NavItem<T>(
    val state: T,
    val view: Stateful<T>.() -> View
)

data class NavState(val navStack: PersistentList<NavItem<*>>)

fun <T> initNavigation(state: T, view: Stateful<T>.() -> View): NavState =
    NavState(persistentListOf(NavItem(state, view)))

interface Navigation {
    suspend fun <T> push(x: NavItem<T>)
    suspend fun pop(): Boolean

    companion object {
        lateinit var shared: Navigation
    }
}

fun Stateful<NavState>.view(): View = navItemView(state.navStack.last())

class Navigate<T>(val nav: NavItem<T>) : Eff<Unit> {
    override suspend fun invoke() {
        if (nav == Back) Navigation.shared.pop()
        else Navigation.shared.push(nav)
    }

    companion object {
        val Back = NavItem(Unit, { error("") })
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Stateful<NavState>.navItemView(navItem: NavItem<T>): PersistentMap<String, Any> {
    val childStateful: Stateful<T> = map(
        { it.navStack.last().state as T },
        { db, a: T ->
            val (x, xs) = db.navStack.last() to db.navStack.dropLast(1).toPersistentList()
            val x2 = (x as NavItem<T>).copy(state = a)
            db.copy(navStack = xs.add(x2))
        }
    )
    return navItem.view(childStateful)
}
