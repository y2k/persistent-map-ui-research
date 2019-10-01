package io.y2k.research.common

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class TabItem<T>(
    val title: String,
    val state: T,
    val view: Stateful<T>.() -> View
)

data class TabsState(
    val page: Int = 0,
    val pages: PersistentList<TabItem<*>>
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

private fun Stateful<TabsState>.content(): View {
    @Suppress("UNCHECKED_CAST")
    fun <T> helper(item: TabItem<T>): View {
        val childStateful = map(
            { db: TabsState -> db.pages[db.page].state as T },
            { db: TabsState, x: T ->
                db.copy(pages = db.pages.set(db.page, (db.pages[db.page] as TabItem<T>).copy(state = x)))
            }
        )
        return item.view(childStateful)
    }
    return helper(state.pages[state.page])
}

private fun Stateful<TabsState>.tabsViews(): Views =
    state.pages
        .mapIndexed { i, x ->
            val button =
                if (i == state.page) button(x.title, λ { replace { db -> db.copy(page = i) } })
                else whiteButton(x.title, λ { replace { db -> db.copy(page = i) } })
            padding(4) {
                button
            }
        }
        .toPersistentList()
