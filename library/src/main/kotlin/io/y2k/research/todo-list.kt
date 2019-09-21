package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.*

data class TodoState(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

fun Stateful<TodoState>.view() = run {
    fun h1(text: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(type to "TextView", "textSize" to 18f, "text" to text) + extra

    fun itemView(item: Item) =
        persistentMapOf(
            type to "PaddingView",
            "padding" to "0,8,0,8",
            children to persistentListOf(
                h1(item.text, "textSize" to 22f)
            )
        )

    column(
        "backgroundColor" to Colors.background,
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
                row(
                    "gravity" to 5,
                    children to persistentListOf(
                        button("Add", λ { update(WeatherDomain::addTodo) }),
                        persistentMapOf(type to "FrameLayout", "minimumWidth" to (8 * ResConst.density).toInt()),
                        whiteButton("Remove all", λ { update(WeatherDomain::removeAllTodos) })
                    )
                )
            },
            persistentMapOf(
                type to "PaddingView",
                "padding" to "8,8,8,8",
                children to persistentListOf(
                    h1(
                        "Today",
                        "textSize" to 48f,
                        "gravity" to 1
                    )
                )
            ),
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
    fun updateText(db: TodoState, text: String) = db.copy(text = text)
    fun removeAllTodos(db: TodoState) = db.copy(todos = persistentListOf())
    fun addTodo(db: TodoState) = db.copy(text = "", todos = db.todos + Item(db.text))
}
