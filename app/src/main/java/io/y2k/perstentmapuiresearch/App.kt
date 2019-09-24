package io.y2k.perstentmapuiresearch

import android.app.Application
import io.y2k.research.common.LoadFromWeb
import io.y2k.research.common.Resources

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        LoadFromWeb.apiKey = BuildConfig.WEATHER_API

        Resources.density = resources.displayMetrics.density
        Resources.button_background = R.drawable.button_background
        Resources.button_background_white = R.drawable.button_background_white
        Resources.button_background_round = R.drawable.button_background_round
    }
}
