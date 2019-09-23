package io.y2k.research

import io.y2k.research.common.*
import io.y2k.research.common.Gravity.CENTER_H
import io.y2k.research.common.Gravity.END
import io.y2k.research.common.Gravity.NO_GRAVITY
import io.y2k.research.common.Localization.Add_Now
import io.y2k.research.common.Localization.New_todo_item
import io.y2k.research.common.Localization.Remove_all
import io.y2k.research.common.Localization.Today
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList

data class TodoState(val text: String = "", val todos: PersistentList<Item> = persistentListOf())
data class Item(val text: String)

fun Stateful<TodoState>.view() = run {

    fun itemView(item: Item) =
        padding("0,8,0,8") {
            h3(item.text)
        }

    column(
        "gravity" to NO_GRAVITY,
        children to persistentListOf(
            editor(
                state.text,
                λ<String> { update { db -> WeatherDomain.updateText(db, it) } },
                "hint" to New_todo_item.i18n
            ),
            freeze {
                row(
                    "gravity" to END,
                    children to persistentListOf(
                        padding(4) {
                            button(Add_Now.i18n, λ { update(WeatherDomain::addTodo) })
                        },
                        padding(4) {
                            whiteButton(Remove_all.i18n, λ { update(WeatherDomain::removeAllTodos) })
                        }
                    )
                )
            },
            padding(8) {
                h1(Today.i18n, "gravity" to CENTER_H)
            },
            expanded {
                memo(state.todos) { todos ->
                    column(
                        children to todos.map { itemView(it) }.toPersistentList()
                    )
                }
            },
            row(
                "gravity" to CENTER_H,
                children to persistentListOf(
                    roundButton("+",
                        "onClickListener" to λ { effect__(WeatherDomain::navigateToCreate) }
                    )
                )
            )
        )
    )
}

object WeatherDomain {
    fun updateText(db: TodoState, text: String) = db.copy(text = text)
    fun removeAllTodos(db: TodoState) = db.copy(todos = persistentListOf())
    fun addTodo(db: TodoState) = db.copy(text = "", todos = db.todos + Item(db.text))
    fun navigateToCreate(db: TodoState): Pair<TodoState, suspend () -> Unit> = run {
        val nextPage = Navigation.mkNavItem(WeatherState(), Stateful<WeatherState>::view)
        db to suspend { Navigation.shared.push(nextPage) }
    }
}
