package io.y2k.perstentmapuiresearch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.Statefull
import io.y2k.research.WeatherState
import io.y2k.research.weatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WeatherActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = Statefull(WeatherState())
        launch {
            while (true) {
                state.whatForUpdate()
                state.weatherView()
                    .let { Interpreter.convert(this@WeatherActivity, it) }
                    .let { setContentView(it) }
            }
        }
    }
}
