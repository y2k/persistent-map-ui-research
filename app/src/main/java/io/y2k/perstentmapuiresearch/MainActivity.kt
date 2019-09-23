package io.y2k.perstentmapuiresearch

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.AppState
import io.y2k.research.common.*
import io.y2k.research.view
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val root by lazy { FrameLayout(this) }
    private var prevState = persistentListOf<PersistentMap<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(root)

        val initState = NavState(listOf(Navigation.mkNavItem(AppState(), Stateful<AppState>::view)))
        val state = StatefulWrapper(initState, this)

        Navigation.shared = object : Navigation {
            override suspend fun push(x: Pair<Any, Stateful<Any>.() -> View>): Unit =
                state.update { db -> db.copy(childs = db.childs + x) }

            override suspend fun pop(): Boolean =
                state.dispatch { db ->
                    if (db.childs.size == 1) db to false
                    else db.copy(childs = db.childs.dropLast(1)) to true
                }
        }

        launch {
            val listener = state.makeListener()
            while (true) {
                updateContentView(state.view())
                listener.receive()
            }
        }
    }

    override fun onBackPressed() {
        launch { if (!Navigation.shared.pop()) super.onBackPressed() }
    }

    private fun updateContentView(view: PersistentMap<String, Any>) {
        Reconciliation.reconcile(prevState, persistentListOf(view), root)
        prevState = persistentListOf(view)
    }
}
