package io.y2k.perstentmapuiresearch

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.Effects
import io.y2k.research.common.Statefull
import io.y2k.research.WeatherState
import io.y2k.research.weatherView
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WeatherActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val root by lazy { FrameLayout(this) }
    private var prevState = persistentListOf<PersistentMap<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Effects.apiKey = BuildConfig.WEATHER_API
        setContentView(root)

        val state = Statefull(WeatherState())
        launch {
            val listener = state.makeListener()
            while (true) {
                updateContentView(state.weatherView())
                listener.receive()
            }
        }
    }

    private fun updateContentView(view: PersistentMap<String, Any>) {
        Reconciliation.reconcile(prevState, persistentListOf(view), root)
        prevState = persistentListOf(view)
    }
}
