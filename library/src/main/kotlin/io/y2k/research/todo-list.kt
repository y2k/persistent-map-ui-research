package io.y2k.research

import io.y2k.research.common.Stateful
import io.y2k.research.common.children
import io.y2k.research.common.type
import io.y2k.research.common.λ
import kotlinx.collections.immutable.*

data class State(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val name: String)

fun Stateful<State>.view() = run {
    fun h1(text: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(type to "TextView", "textSize" to 18f, "text" to text).putAll(extra)

    fun itemView(user: Item) =
        h1("Item (${user.name})", "textSize" to 16f)

    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        children to persistentListOf(
            persistentMapOf(
                type to "EditTextWrapper",
                "text" to state.text,
                "onEditListener" to λ<String> { dispatch { db -> WeatherDomain.updateText(db, it) to Unit } },
                children to persistentListOf(
                    persistentMapOf(
                        type to "EditText",
                        "hint" to "New todo item"
                    )
                )
            ),
            persistentMapOf(
                type to "LinearLayout",
                "gravity" to 5,
                children to persistentListOf(
                    persistentMapOf(
                        type to "Button",
                        "text" to "Add",
                        "onClickListener" to λ { dispatch { WeatherDomain.addTodo(it) to Unit } }
                    ),
                    persistentMapOf(
                        type to "Button",
                        "text" to "Remove all",
                        "onClickListener" to λ { dispatch { WeatherDomain.removeAllTodos(it) to Unit } }
                    )
                )
            ),
            h1("Todo Items:", "gravity" to 1),
            persistentMapOf(
                type to "LinearLayout",
                "orientation" to 1,
                children to state.todos.map { itemView(it) }.toPersistentList()
            )
        )
    )
}

object WeatherDomain {
    fun updateText(db: State, text: String) = db.copy(text = text)
    fun removeAllTodos(db: State) = db.copy(todos = persistentListOf())
    fun addTodo(db: State) = db.copy(text = "", todos = db.todos + Item(db.text))
}
