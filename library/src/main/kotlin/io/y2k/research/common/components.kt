package io.y2k.research.common

import kotlinx.collections.immutable.*

object Gravity {
    const val NO_GRAVITY = 0
    const val CENTER_H = 1
    const val CENTER_V = 16
    const val CENTER = CENTER_H or CENTER_V
    const val TOP = 48
    const val BOTTOM = 80
    const val LEFT = 3
    const val RIGHT = 5
    const val START = 8388611
    const val END = 8388613
}

object Colors {
    const val white = 0xffffffff.toInt()
    const val background = 0xfffafafa.toInt()
}

fun button(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Button",
        "backgroundResource" to Resources.button_background,
        "textColor" to 0xFFFFFFFF.toInt(),
        "text" to text,
        "onClickListener" to onClick
    )

fun whiteButton(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Button",
        "backgroundResource" to Resources.button_background_white,
        "textColor" to 0xff7e7878.toInt(),
        "text" to text,
        "onClickListener" to onClick
    )

fun row(vararg items: Pair<String, Any>) =
    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 0
    ).plus(items.asIterable())

fun column(vararg items: Pair<String, Any>) =
    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1
    ).plus(items.asIterable())

inline fun padding(padding: String, child: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "PaddingView",
        "padding" to padding,
        children to persistentListOf(child())
    )

inline fun padding(all: Int, child: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    padding("$all,$all,$all,$all", child)

fun h1(text: String, vararg extra: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 48f, "text" to text) + extra

fun h2(text: String, vararg extra: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 36f, "text" to text) + extra

fun h3(text: String, vararg extra: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 22f, "text" to text) + extra

fun h4(text: String, vararg extra: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "text" to text) + extra

fun roundButton(text: String): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Button",
        "backgroundResource" to Resources.button_background_round,
        "text" to text,
        "textSize" to 50f,
        "gravity" to 17,
        "minimumWidth" to 0,
        "minWidth" to 0,
        "minimumHeight" to 0,
        "minHeight" to 0,
        "textColor" to Colors.white
    )

fun editor(text: String, λ: λ<String>, vararg extra: Pair<String, Any>): PersistentMap<String, Any> {
    return persistentMapOf(
        type to "EditTextWrapper",
        "text" to text,
        "onTextListener" to λ,
        children to persistentListOf(
            persistentMapOf(
                type to "EditText",
                "hint" to "New todo item"
            ).putAll(extra)
        )
    )
}

inline fun expanded(content: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Expanded",
        children to persistentListOf(
            content()
        )
    )
