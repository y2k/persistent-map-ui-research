package io.y2k.perstentmapuiresearch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.Statefull
import io.y2k.research.WeatherConfig
import io.y2k.research.WeatherState
import io.y2k.research.weatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WeatherActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WeatherConfig.apiKey = BuildConfig.WEATHER_API

        val state = Statefull(WeatherState())
        launch {
            val listener = state.makeListener()
            while (true) {
                state.weatherView()
                    .let { Interpreter.convert(this@WeatherActivity, it) }
                    .let { setContentView(it) }
                listener.receive()
            }
        }
    }
}
