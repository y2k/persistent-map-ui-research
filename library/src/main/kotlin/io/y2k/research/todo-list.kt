package io.y2k.research

import kotlinx.collections.immutable.*
import kotlinx.coroutines.channels.Channel

class Item(val name: String)

const val type = "@"
const val children = "children"

fun Statefull<State>.view() =
    view(state.todos)

fun Statefull<State>.view(items: Iterable<Item>) = run {
    fun h1(title: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(
            type to "TextView",
            "textSize" to 18f,
            "text" to title
        ).putAll(extra)

    fun toChildView(user: Item) =
        h1("Item (${user.name})", "textSize" to 16f)

    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        children to persistentListOf(
            persistentMapOf(
                type to "EditText",
                "hint" to "New todo item"
            ),
            persistentMapOf(
                type to "LinearLayout",
                children to persistentListOf(
                    persistentMapOf(
                        type to "Button",
                        "text" to "Add"
                    ),
                    persistentMapOf(
                        type to "Button",
                        "text" to "Remove all",
                        "onClickListener" to {
                            dispatch { it.copy(todos = persistentListOf()) to Unit }
                        }
                    )
                )
            ),
            h1("Todo Items:"),
            persistentMapOf(
                type to "LinearLayout",
                "orientation" to 1,
                children to items.map { toChildView(it) }.toPersistentList()
            )
        )
    )
}

data class State(val todos: PersistentList<Item> = persistentListOf())

class Statefull<State>(var state: State) {

    private val channel = Channel<Unit>(0)

    suspend fun whatForUpdate(): Unit = channel.receive()

    fun <T> dispatch(f: (State) -> Pair<State, T>): T {
        val (s, r) = f(state)
        state = s
        while (channel.offer(Unit)) {
        }
        return r
    }
}
