package android.widget

import android.content.Context
import android.util.AttributeSet

class MemoViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    override fun toString(): String = "MemoViewGroup[${childCount}]"
}
