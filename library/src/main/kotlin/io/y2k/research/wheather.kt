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
import io.y2k.research.common.*
import io.y2k.research.common.Gravity.CENTER_H
import io.y2k.research.common.Gravity.CENTER_V
import kotlinx.collections.immutable.persistentListOf
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

fun Stateful<WeatherState>.view() =
    column(
        "gravity" to CENTER_V,
        children to persistentListOf(
            h2(
                state.temperature,
                "gravity" to CENTER_H
            ),
            h4(state.error),
            button("Reload Weather", λ(::reloadWeather))
        )
    )

fun Stateful<WeatherState>.reloadWeather() {
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
            { db.copy(temperature = "--", error = "Error: ${it.message}") }
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
