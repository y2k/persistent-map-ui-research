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
    const val red = 0xffff0000.toInt()
    const val white = 0xffffffff.toInt()
    const val background = 0xfffafafa.toInt()
}

object Resources {
    var density: Float = 0f
    var button_background: Int = 0
    var button_background_white: Int = 0
    var button_background_round: Int = 0
}

typealias View = PersistentMap<String, Any>
typealias Views = PersistentList<View>

fun button(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "PaddingView",
        "padding" to "12,12,12,12",
        "backgroundResource" to Resources.button_background,
        "onClickListener" to onClick,
        children to persistentListOf(
            persistentMapOf(
                type to "TextView",
                "textSize" to 18f,
                "textColor" to Colors.white,
                "text" to text
            )
        )
    )

fun whiteButton(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "PaddingView",
        "padding" to "12,12,12,12",
        "backgroundResource" to Resources.button_background_white,
        "onClickListener" to onClick,
        children to persistentListOf(
            persistentMapOf(
                type to "TextView",
                "textSize" to 18f,
                "textColor" to 0xff7e7878.toInt(),
                "text" to text
            )
        )
    )

fun row(vararg props: Pair<String, Any>) =
    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 0
    ).plus(props.asIterable())

fun column(vararg props: Pair<String, Any>) =
    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1
    ).plus(props.asIterable())

inline fun padding(padding: String, child: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "PaddingView",
        "padding" to padding,
        children to persistentListOf(child())
    )

inline fun padding(all: Int, child: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    padding("$all,$all,$all,$all", child)

fun h1(text: String, vararg props: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 48f, "text" to text) + props

fun h2(text: String, vararg props: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 36f, "text" to text) + props

fun h3(text: String, vararg props: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "textSize" to 22f, "text" to text) + props

fun h4(text: String, vararg props: Pair<String, Any>) =
    persistentMapOf(type to "TextView", "text" to text) + props

fun roundButton(text: String, vararg props: Pair<String, Any>): PersistentMap<String, Any> =
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
    ).putAll(props)

fun editor(text: String, f: λ<String>, vararg props: Pair<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "EditTextWrapper",
        "text" to text,
        "onTextListener" to f,
        children to persistentListOf(
            persistentMapOf(type to "EditText").putAll(props)
        )
    )

inline fun expanded(content: () -> PersistentMap<String, Any>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Expanded",
        children to persistentListOf(
            content()
        )
    )
