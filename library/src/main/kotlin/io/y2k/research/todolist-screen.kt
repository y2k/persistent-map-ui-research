package io.y2k.research

import io.y2k.research.common.*
import io.y2k.research.common.Gravity.CENTER_H
import io.y2k.research.common.Gravity.NO_GRAVITY
import io.y2k.research.common.Localization.Remove_all
import io.y2k.research.common.Localization.Today
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class TodoState(val todos: PersistentList<String> = persistentListOf())

fun Stateful<TodoState>.view() = run {
    onStarted(TodoListDomain.init().second)

    fun itemView(item: String) =
        padding("0,8,0,8") {
            h3("• $item")
        }

    column(
        "gravity" to NO_GRAVITY,
        children to persistentListOf(
            padding(8) {
                h1(Today.i18n, "gravity" to CENTER_H)
            },
            button(
                Remove_all.i18n,
                λ { update(TodoListDomain::deleteAllPressed) }
            ),
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
                    roundButton(
                        "+",
                        "onClickListener" to λ { update(TodoListDomain::createClicked) }
                    )
                )
            )
        )
    )
}

object TodoListDomain {

    fun init() =
        TodoState() to setOf(
            ReadAppStore.updateStoreSafe { db: TodoState, shared ->
                db.copy(todos = shared.todos.map { it.text }.toPersistentList())
            }
        )

    fun deleteAllPressed(db: TodoState) =
        db to setOf(UpdateAppStore { appDb -> appDb.copy(todos = persistentListOf()) })

    fun createClicked(db: TodoState): Pair<TodoState, Set<Eff<*>>> =
        db to setOf(Navigate(NavItem(CreateTodoState(), Stateful<CreateTodoState>::view)))
}
