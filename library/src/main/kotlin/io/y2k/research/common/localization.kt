package io.y2k.research.common

import java.util.*

@Suppress("ObjectPropertyName")
object Localization {
    val Todo = "Todo" to "Todo"
    val Weather = "Weather" to "Погода"
    val Today = "Today" to "Сегодня"
    val Remove_all = "Remove all" to "Удилить все"
    val Add_Now = "+ Add Now" to "+ Добавить"
    val New_todo_item = "New todo item" to "Новая запись"
    val Reload_Weather = "Reload Weather" to "Перегрузить погоду"
}

val Pair<String, String>.i18n: String
    get() {
        val ru = Locale.forLanguageTag("ru-RU")
        return if (Locale.getDefault() == ru) this.second else this.first
    }
