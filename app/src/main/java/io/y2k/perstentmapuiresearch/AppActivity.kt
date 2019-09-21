package io.y2k.perstentmapuiresearch

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.AppState
import io.y2k.research.common.StatefulWrapper
import io.y2k.research.view
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AppActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val root by lazy { FrameLayout(this) }
    private var prevState = persistentListOf<PersistentMap<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(root)

        val state = StatefulWrapper(AppState())
        launch {
            val listener = state.makeListener()
            while (true) {
                updateContentView(state.view())
                listener.receive()
            }
        }
    }

    private fun updateContentView(view: PersistentMap<String, Any>) {
        Reconciliation.reconcile(prevState, persistentListOf(view), root)
        prevState = persistentListOf(view)
    }
}
