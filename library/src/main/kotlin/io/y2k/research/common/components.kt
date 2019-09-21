package io.y2k.research.common

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus

object Colors {
    const val background = 0xfffafafa.toInt()
}

fun button(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Button",
        "backgroundResource" to ResConst.button_background,
        "textColor" to 0xFFFFFFFF.toInt(),
        "text" to text,
        "onClickListener" to onClick
    )

fun whiteButton(text: String, onClick: λ<*>): PersistentMap<String, Any> =
    persistentMapOf(
        type to "Button",
        "backgroundResource" to ResConst.button_background_white,
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
