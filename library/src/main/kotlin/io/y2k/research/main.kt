package io.y2k.research

import io.y2k.research.common.Eff
import io.y2k.research.common.StatefulWrapper
import io.y2k.research.common.update
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.MainScope

class UpdateAppStore(val f: (ApplicationState) -> ApplicationState) : Eff<Unit> {
    override suspend fun invoke() = store.update { f(it) }
}

object ReadAppStore : Eff<ApplicationState> {
    override suspend fun invoke(): ApplicationState = store.state
}

data class ApplicationState(val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

val store = StatefulWrapper(
    ApplicationState(),
    MainScope()
)
