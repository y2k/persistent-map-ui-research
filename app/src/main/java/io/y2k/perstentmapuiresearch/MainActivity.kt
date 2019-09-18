package io.y2k.perstentmapuiresearch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.State
import io.y2k.research.Statefull
import io.y2k.research.view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = Statefull(State())
        launch {
            while (true) {
                state.view()
                    .let { Interpreter.convert(this@MainActivity, it) }
                    .let { setContentView(it) }
                state.whatForUpdate()
            }
        }
    }
}
