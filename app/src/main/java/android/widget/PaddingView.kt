package android.widget

import android.content.Context
import android.util.AttributeSet

class PaddingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    fun setPadding(padding: String) {
        val ps = padding.split(",").map { toPx(it) }
        setPadding(ps[0], ps[1], ps[2], ps[3])
    }

    private fun toPx(it: String): Int =
        (it.toFloat() * resources.displayMetrics.density).toInt()
}
