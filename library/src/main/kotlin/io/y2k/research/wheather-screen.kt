package io.y2k.research

import io.ktor.client.request.request
import io.ktor.client.request.url
import io.y2k.research.common.*
import io.y2k.research.common.Gravity.CENTER_H
import io.y2k.research.common.Gravity.CENTER_V
import io.y2k.research.common.Localization.Reload_Weather
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class WeatherState(val temperature: String = "", val error: String = "")
@Serializable
class WeatherResponse(val main: Main) {
    @Serializable
    class Main(val temp: Double)
}

fun Stateful<WeatherState>.view() =
    column(
        "gravity" to CENTER_V,
        children to persistentListOf(
            h1(
                state.temperature,
                "gravity" to CENTER_H
            ),
            padding(8) {
                h4(state.error, "textColor" to Colors.red)
            },
            button(
                Reload_Weather.i18n,
                λ { effect(TodoListDomain::mkRequest) })
        )
    )

object TodoListDomain {

    fun mkRequest(db: WeatherState) = run {
        val r = request {
            val city = "Saint+Petersburg"
            url("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=en")
        }
        val eff = LoadFromWeb(r).updateStore(::handleResponse)
        db.copy(temperature = "...", error = "") to setOf(eff)
    }

    fun handleResponse(db: WeatherState, result: Result<String>) =
        result.mapCatching { Json.nonstrict.parse(WeatherResponse.serializer(), it) }
            .fold(
                { db.copy(temperature = "${it.main.temp} C", error = "") },
                { db.copy(temperature = "--", error = "Error: ${it.message}") }
            )
}
