package io.y2k.research

import kotlinx.collections.immutable.*

class User(val name: String)

const val type = "@"
const val children = "children"

fun view() =
    List(10) { User("Item #${it + 1}") }
        .toPersistentList()
        .let { view(it) }

fun view(items: PersistentList<User>) = run {
    fun h1(title: String, vararg extra: Pair<String, Any>) =
        persistentMapOf(
            type to "TextView",
            "textSize" to 18f,
            "text" to title
        ).putAll(extra)

    fun toChildView(user: User) =
        h1("Item (${user.name})", "textSize" to 16f)

    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        children to persistentListOf(
            h1("Todo Items:"),
            persistentMapOf(
                type to "LinearLayout",
                "orientation" to 1,
//                "layoutParams.weight" to 1f,
                children to items.map { toChildView(it) }.toPersistentList()
            )
        )
    )
}
