package io.y2k.research

import io.y2k.research.common.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

data class AppState(
    val page: Int = 0,
    val pages: List<String> = listOf("Weather", "Todo"),
    val weather: WeatherState = WeatherState(),
    val todo: TodoState = TodoState(todos = persistentListOf(Item("One"), Item("Two"), Item("Three")))
)

fun Stateful<AppState>.view() =
    column(
        "backgroundColor" to Colors.background,
        children to persistentListOf(
            expanded {
                padding("16,16,16,16") {
                    content()
                }
            },
            row(children to tabsViews())
        )
    )

private fun Stateful<AppState>.content() = when (state.page) {
    0 -> map({ it.weather }, { db, x -> db.copy(weather = x) }).view()
    1 -> map({ it.todo }, { db, x -> db.copy(todo = x) }).view()
    else -> error(state)
}

private fun Stateful<AppState>.tabsViews() =
    state.pages
        .mapIndexed { i, x ->
            val button =
                if (i == state.page) button(x, λ { update { db -> db.copy(page = i) } })
                else whiteButton(x, λ { update { db -> db.copy(page = i) } })
            persistentMapOf(
                type to "PaddingView",
                "padding" to "4,4,4,4",
                children to persistentListOf(button)
            )
        }
        .toPersistentList()
