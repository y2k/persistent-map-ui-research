package io.y2k.research

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.AndroidClientEngine
import io.ktor.client.engine.android.AndroidEngineConfig
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.request.url
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class WeatherState(val temperature: String = "", val error: String = "")

fun Statefull<WeatherState>.weatherView() =
    persistentMapOf(
        type to "LinearLayout",
        "orientation" to 1,
        "gravity" to 16,
        children to persistentListOf(
            persistentMapOf(
                type to "TextView",
                "textSize" to 36f,
                "gravity" to 1,
                "text" to state.temperature
            ),
            persistentMapOf(
                type to "TextView",
                "text" to state.error
            ),
            persistentMapOf(
                type to "Button",
                "text" to "Reload",
                "onClickListener" to { reloadWeather() }
            )
        )
    )

fun Statefull<WeatherState>.reloadWeather() {
    GlobalScope.launch {
        dispatch { db -> preload(db) to Unit }
        val r = runCatching { loadWeatherFromWeb<WeatherResponse>(makeRequest()) }
        dispatch { db -> update(db, r) to Unit }
    }
}

fun preload(db: WeatherState): WeatherState =
    db.copy(temperature = "...", error = "")

private fun update(db: WeatherState, response: Result<WeatherResponse>): WeatherState = run {
    fun mapToTemperature(response: WeatherResponse) =
        response.main.temp

    response.fold(
        { db.copy(error = "", temperature = "${mapToTemperature(it)} C") },
        { db.copy(error = "Error", temperature = "--") }
    )
}

private fun makeRequest() =
    request {
        val city = "Saint+Petersburg"
        url("http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=en")
    }

suspend inline fun <reified T> loadWeatherFromWeb(r: HttpRequestBuilder): T {
    val client = HttpClient(AndroidClientEngine(AndroidEngineConfig())) {
        install(JsonFeature) { serializer = KotlinxSerializer(Json.nonstrict) }
    }
    r.url.parameters.append("appid", WeatherConfig.apiKey)
    return client.get(r)
}

@Serializable
class WeatherResponse(val main: Main) {
    @Serializable
    class Main(val temp: Double)
}

object WeatherConfig {
    lateinit var apiKey: String
}
