package io.y2k.research

import io.y2k.research.common.*
import io.y2k.research.common.Localization.Todo
import io.y2k.research.common.Localization.Weather
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

data class ApplicationState(val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

val store = StatefulWrapper(ApplicationState(), MainScope())

class UpdateAppStore(val f: (ApplicationState) -> ApplicationState) : Eff<Unit> {
    override suspend fun invoke() = store.replace { f(it) }
}

object ReadAppStore : Eff<ApplicationState> {
    override suspend fun invoke(): ApplicationState = store.state
}

fun CoroutineScope.main(updateContentView: (View) -> Unit) {
    val state = initNavigation(
        TabsState(
            0,
            persistentListOf(
                TabItem(Weather.i18n, WeatherDomain.init().first, Stateful<WeatherState>::view),
                TabItem(Todo.i18n, TodoListDomain.init().first, Stateful<TodoState>::view)
            )
        ),
        Stateful<TabsState>::view
    )
    val runtime = StatefulWrapper(state, this)

    Navigation.shared = object : Navigation {
        override suspend fun <T> push(x: NavItem<T>): Unit =
            runtime.replace { db -> db.copy(navStack = db.navStack.add(x)) }

        override suspend fun pop(): Boolean =
            runtime.dispatch { db ->
                if (db.navStack.size == 1) db to false
                else db.copy(navStack = db.navStack.dropLast(1).toPersistentList()) to true
            }
    }

    launch {
        val listener = runtime.makeListener()
        while (true) {
            updateContentView(runtime.view())
            listener.receive()
        }
    }
}
