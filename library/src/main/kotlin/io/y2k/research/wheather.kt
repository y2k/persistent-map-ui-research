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
import io.y2k.research.common.Statefull
import io.y2k.research.common.children
import io.y2k.research.common.type
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class WeatherState(val temperature: String = "", val error: String = "")
@Serializable
class WeatherResponse(val main: Main) {
    @Serializable
    class Main(val temp: Double)
}

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
        dispatch { db -> TodoListDomain.preload(db) }
            .let { runCatching { Effects.loadWeatherFromWeb<WeatherResponse>(it) } }
            .let { dispatch { db -> TodoListDomain.update(db, it) to Unit } }
    }
}

object TodoListDomain {
    fun preload(db: WeatherState): Pair<WeatherState, HttpRequestBuilder> {
        fun mkRequest() =
            request {
                val city = "Saint+Petersburg"
                url("http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=en")
            }

        return db.copy(temperature = "...", error = "") to mkRequest()
    }

    fun update(db: WeatherState, response: Result<WeatherResponse>) = run {
        fun mapToTemperature(response: WeatherResponse) = response.main.temp

        response.fold(
            { db.copy(temperature = "${mapToTemperature(it)} C", error = "") },
            { db.copy(temperature = "--", error = "Error") }
        )
    }
}

object Effects {
    lateinit var apiKey: String

    suspend inline fun <reified T> loadWeatherFromWeb(request: HttpRequestBuilder): T {
        val client = HttpClient(AndroidClientEngine(AndroidEngineConfig())) {
            install(JsonFeature) { serializer = KotlinxSerializer(Json.nonstrict) }
        }
        request.url.parameters.append("appid", apiKey)
        return client.get(request)
    }
}
