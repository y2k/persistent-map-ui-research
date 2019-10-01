package io.y2k.perstentmapuiresearch

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.common.Navigation
import io.y2k.research.main
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val reconciliation = Reconciliation(AndroidViewFactory, AndroidFVG, AndroidFV)

    private val root by lazy { FrameLayout(this) }
    private var prevState = persistentListOf<PersistentMap<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(root)
        main(::updateContentView)
    }

    override fun onBackPressed() {
        launch { if (!Navigation.shared.pop()) super.onBackPressed() }
    }

    private fun updateContentView(view: PersistentMap<String, Any>) {
        reconciliation.reconcile(prevState, persistentListOf(view), root)
        prevState = persistentListOf(view)
    }
}
