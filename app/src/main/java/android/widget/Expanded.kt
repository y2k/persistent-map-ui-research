package android.widget

import android.content.Context
import android.util.AttributeSet

class Expanded @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = LinearLayout.LayoutParams(-1, -1, 1f)
    }
}
