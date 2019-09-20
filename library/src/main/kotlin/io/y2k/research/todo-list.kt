package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.*

data class State(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

fun Stateful<State>.view() = run {
    fun h1(text: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(type to "TextView", "textSize" to 18f, "text" to text) + extra

    fun itemView(item: Item) =
        h1("'${item.text}'", "textSize" to 16f)

    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        children to persistentListOf(
            persistentMapOf(
                type to "EditTextWrapper",
                "text" to state.text,
                "onTextListener" to λ<String> { update { db -> WeatherDomain.updateText(db, it) } },
                children to persistentListOf(
                    persistentMapOf(
                        type to "EditText",
                        "hint" to "New todo item"
                    )
                )
            ),
            const {
                persistentMapOf(
                    type to "LinearLayout",
                    "gravity" to 5,
                    children to persistentListOf(
                        persistentMapOf(
                            type to "Button",
                            "text" to "Add",
                            "onClickListener" to λ { update(WeatherDomain::addTodo) }
                        ),
                        persistentMapOf(
                            type to "Button",
                            "text" to "Remove all",
                            "onClickListener" to λ { update(WeatherDomain::removeAllTodos) }
                        )
                    )
                )
            },
            h1("Todo Items:", "gravity" to 1),
            memo(state.todos) { todos ->
                persistentMapOf(
                    type to "LinearLayout",
                    "orientation" to 1,
                    children to todos.map { itemView(it) }.toPersistentList()
                )
            }
        )
    )
}

object WeatherDomain {
    fun updateText(db: State, text: String) = db.copy(text = text)
    fun removeAllTodos(db: State) = db.copy(todos = persistentListOf())
    fun addTodo(db: State) = db.copy(text = "", todos = db.todos + Item(db.text))
}
