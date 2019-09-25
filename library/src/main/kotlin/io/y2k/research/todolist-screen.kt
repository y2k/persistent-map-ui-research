package io.y2k.research

import io.y2k.research.common.*
import io.y2k.research.common.Gravity.CENTER_H
import io.y2k.research.common.Gravity.NO_GRAVITY
import io.y2k.research.common.Localization.Today
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class TodoState(val todos: PersistentList<String> = persistentListOf())

fun Stateful<TodoState>.view() = run {
    subscribeEffect(ReadAppStore, WeatherDomain::applyAppStore)

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
                        "onClickListener" to λ { effect(WeatherDomain::createClicked) }
                    )
                )
            )
        )
    )
}

object WeatherDomain {

    fun applyAppStore(db: TodoState, addDb: Result<ApplicationState>) =
        addDb.fold(
            { x -> db.copy(todos = x.todos.map { it.text }.toPersistentList()) },
            { db }
        )

    fun createClicked(db: TodoState): Pair<TodoState, Set<Eff<*>>> =
        db to setOf(Navigate(NavItem(CreateTodoState(), Stateful<CreateTodoState>::view)))
}
