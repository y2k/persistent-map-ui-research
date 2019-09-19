package io.y2k.research

import kotlinx.collections.immutable.*

data class State(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val name: String)

fun Statefull<State>.view() = run {
    fun h1(title: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(type to "TextView", "textSize" to 18f, "text" to title).putAll(extra)

    fun toChildView(user: Item) =
        h1("Item (${user.name})", "textSize" to 16f)

    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        children to persistentListOf(
            persistentMapOf(
                type to "EditTextWrapper",
                "text" to state.text,
                "onEditListener" to { x: String -> dispatch { db -> updateText(db, x) to Unit } },
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
                        "onClickListener" to { dispatch { addTodo(it) to Unit } }
                    ),
                    persistentMapOf(
                        type to "Button",
                        "text" to "Remove all",
                        "onClickListener" to { dispatch { removeAllTodos(it) to Unit } }
                    )
                )
            ),
            h1("Todo Items:", "gravity" to 1),
            persistentMapOf(
                type to "LinearLayout",
                "orientation" to 1,
                children to state.todos.map { toChildView(it) }.toPersistentList()
            )
        )
    )
}

fun updateText(db: State, x: String) = db.copy(text = x)
fun removeAllTodos(db: State) = db.copy(todos = persistentListOf())
fun addTodo(db: State) = db.copy(text = "", todos = db.todos + Item(db.text))
