package io.y2k.research

import io.y2k.research.common.*
import io.y2k.research.common.Localization.Todo
import io.y2k.research.common.Localization.Weather
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class TabsState(
    val page: Int = 0,
    val pages: List<String> = listOf(Weather.i18n, Todo.i18n),
    val weather: WeatherState = WeatherState(),
    val todo: TodoState = TodoState()
)

fun Stateful<TabsState>.view() =
    column(
        "backgroundColor" to Colors.background,
        children to persistentListOf(
            expanded {
                padding(16) {
                    content()
                }
            },
            row(children to tabsViews())
        )
    )

private fun Stateful<TabsState>.content() = when (state.page) {
    0 -> map({ it.weather }, { db, x -> db.copy(weather = x) }).view()
    1 -> map({ it.todo }, { db, x -> db.copy(todo = x) }).view()
    else -> error(state)
}

private fun Stateful<TabsState>.tabsViews() =
    state.pages
        .mapIndexed { i, x ->
            val button =
                if (i == state.page) button(x, λ { update { db -> db.copy(page = i) } })
                else whiteButton(x, λ { update { db -> db.copy(page = i) } })
            padding(4) {
                button
            }
        }
        .toPersistentList()
