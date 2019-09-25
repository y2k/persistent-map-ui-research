package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

data class ApplicationState(val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

val store = StatefulWrapper(ApplicationState(), MainScope())

class UpdateAppStore(val f: (ApplicationState) -> ApplicationState) : Eff<Unit> {
    override suspend fun invoke() = store.update { f(it) }
}

object ReadAppStore : Eff<ApplicationState> {
    override suspend fun invoke(): ApplicationState = store.state
}

fun CoroutineScope.main(updateContentView: (View) -> Unit) {
    val initState = NavState(listOf(NavItem(TabsState(), Stateful<TabsState>::view)))
    val state = StatefulWrapper(initState, this)

    Navigation.shared = object : Navigation {
        override suspend fun <T> push(x: NavItem<T>): Unit =
            state.update { db -> db.copy(navStack = db.navStack + x) }

        override suspend fun pop(): Boolean =
            state.dispatch { db ->
                if (db.navStack.size == 1) db to false
                else db.copy(navStack = db.navStack.dropLast(1)) to true
            }
    }

    launch {
        val listener = state.makeListener()
        while (true) {
            updateContentView(state.view())
            listener.receive()
        }
    }
}
