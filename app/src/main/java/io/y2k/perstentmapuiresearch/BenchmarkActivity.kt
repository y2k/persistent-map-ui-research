package io.y2k.perstentmapuiresearch

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.TodoState
import io.y2k.research.WeatherState
import io.y2k.research.common.StatefulWrapper
import io.y2k.research.common.View
import io.y2k.research.view
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BenchmarkActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this)

        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(Button(this).apply {
            text = "Start"
            setOnClickListener {
                launch {
                    text = "..."
                    isEnabled = false
                    delay(100)

                    val count = runBenchmark(container)

                    text = "Start (FPS = ${count / 5f})"
                    isEnabled = true
                    container.removeAllViews()
                }
            }
        })
        ll.addView(container, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(ll)
    }

    private fun runBenchmark(container: FrameLayout): Long {
        container.removeAllViews()
        var prev = persistentListOf<View>()

        val startTime = System.currentTimeMillis()
        var iter = 0L

        while (System.currentTimeMillis() - startTime < 5_000) {
            var actual = persistentListOf(StatefulWrapper(TodoState(), MainScope()).view())
            Reconciliation.reconcile(prev, actual, container)
            prev = actual

            actual = persistentListOf(StatefulWrapper(WeatherState(), MainScope()).view())
            Reconciliation.reconcile(prev, actual, container)
            prev = actual
            iter++
        }
        return iter
    }
}
