package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.persistentListOf

data class CreateTodoState(val text: String = "")

fun Stateful<CreateTodoState>.view() =
    padding(8) {
        column(
            "gravity" to Gravity.NO_GRAVITY,
            children to persistentListOf(
                editor(
                    state.text,
                    λ<String> { update { db -> CreateTodoDomain.textChanged(db, it) } },
                    "hint" to Localization.New_todo_item.i18n
                ),
                freeze {
                    row(
                        "gravity" to Gravity.END,
                        children to persistentListOf(
                            padding(4) {
                                button(
                                    Localization.Add_Now.i18n,
                                    λ { effect { CreateTodoDomain.createPressed(it) } })
                            }
                        )
                    )
                }
            )
        )
    }

object CreateTodoDomain {
    fun textChanged(db: CreateTodoState, text: String) =
        db.copy(text = text)

    fun createPressed(db: CreateTodoState) =
        db.copy(text = "") to setOf(
            Navigate(Navigate.Back),
            UpdateAppStore { it.copy(todos = it.todos.add(Item(db.text))) })
}
