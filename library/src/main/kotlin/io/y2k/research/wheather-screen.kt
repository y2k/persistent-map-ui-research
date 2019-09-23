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
import io.y2k.research.common.Localization.Reload_Weather
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
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
                λ {
                    effect(
                        TodoListDomain::mkRequest,
                        Effects::loadWeatherFromWebAsync
                    ).complete(TodoListDomain::handleResponse)
                })
        )
    )

object TodoListDomain {

    fun mkRequest(db: WeatherState) = run {
        val r = request {
            val city = "Saint+Petersburg"
            url("http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=en")
        }
        db.copy(temperature = "...", error = "") to r
    }

    fun handleResponse(db: WeatherState, r: Result<String>) = run {
        fun toTemp(json: String) = Json.parse(WeatherResponse.serializer(), json).main.temp
        r.fold(
            { db.copy(temperature = "${toTemp(it)} C", error = "") },
            { db.copy(temperature = "--", error = "Error: ${it.message}") }
        ) to Unit
    }
}

object Effects {
    lateinit var apiKey: String

    fun loadWeatherFromWebAsync(a: CoroutineScope, request: HttpRequestBuilder) = a.run {
        val client = HttpClient(AndroidClientEngine(AndroidEngineConfig())) {
            this.install(JsonFeature) { this.serializer = KotlinxSerializer(Json.nonstrict) }
        }
        request.url.parameters.append("appid", this@Effects.apiKey)
        async { client.get<String>(request) }
    }
}
