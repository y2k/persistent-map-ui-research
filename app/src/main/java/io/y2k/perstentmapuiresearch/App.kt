package io.y2k.perstentmapuiresearch

import android.app.Application
import io.y2k.research.common.ResConst

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        ResConst.density = resources.displayMetrics.density
        ResConst.button_background = R.drawable.button_background
        ResConst.button_background_white = R.drawable.button_background_white
    }
}
