package io.y2k.research

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class WeatherState(val temperature: Float = 0f, val error: Boolean)

fun Statefull<WeatherState>.weatherView() =
    persistentMapOf(
        type to "LinearLayout",
        children to persistentListOf(
            persistentMapOf(
                type to "TextView",
                "text" to "${state.temperature} C"
            ),
            persistentMapOf(
                type to "Button",
                "text" to "Reload",
                "onClickListener" to { reloadWeather("moscow") }
            )
        )
    )

fun Statefull<WeatherState>.reloadWeather(city: String) {
    GlobalScope.launch {
        val r = runCatching { loadWeatherFromWeb(city) }
        dispatch { db -> update(db, r) to Unit }
    }
}

fun update(db: WeatherState, response: Result<WeatherResponse>): WeatherState = run {
    fun mapToTemperature(response: WeatherResponse): Float = TODO()

    response.fold(
        { db.copy(error = false, temperature = mapToTemperature(it)) },
        { db.copy(error = true) }
    )
}

suspend fun loadWeatherFromWeb(city: String): WeatherResponse = TODO()
class WeatherResponse
