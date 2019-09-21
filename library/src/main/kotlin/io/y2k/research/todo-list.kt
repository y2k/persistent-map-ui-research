package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.*

data class TodoState(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

fun Stateful<TodoState>.view() = run {

    fun itemView(item: Item) =
        padding("0,8,0,8") {
            h2(item.text)
        }

    column(
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
                        padding("4,4,4,4") {
                            button("+ Add Now", λ { update(WeatherDomain::addTodo) })
                        },
                        padding("4,4,4,4") {
                            whiteButton("Remove all", λ { update(WeatherDomain::removeAllTodos) })
                        }
                    )
                )
            },
            padding("8,8,8,8") {
                h1("Today", "gravity" to 1)
            },
            expanded {
                memo(state.todos) { todos ->
                    persistentMapOf(
                        type to "LinearLayout",
                        "orientation" to 1,
                        children to todos.map { itemView(it) }.toPersistentList()
                    )
                }
            },
            row(
                "gravity" to 1,
                children to persistentListOf(
                    roundButton("+")
                )
            )

        )
    )
}

object WeatherDomain {
    fun updateText(db: TodoState, text: String) = db.copy(text = text)
    fun removeAllTodos(db: TodoState) = db.copy(todos = persistentListOf())
    fun addTodo(db: TodoState) = db.copy(text = "", todos = db.todos + Item(db.text))
}
