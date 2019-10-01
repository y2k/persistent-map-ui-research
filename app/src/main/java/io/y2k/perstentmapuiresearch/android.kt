package io.y2k.perstentmapuiresearch

import android.content.Context
import android.view.View
import android.view.ViewGroup

object AndroidFV : FView<View> {

    override val View.isGroup: Boolean
        get() = this is ViewGroup
}

object AndroidFVG : FViewGroup<ViewGroup, View> {

    override val ViewGroup.context: Context
        get() = context

    override val ViewGroup.childCount: Int
        get() = childCount

    override fun ViewGroup.addView(view: View) {
        addView(view)
    }

    override fun ViewGroup.addView(view: View, i: Int) {
        addView(view, i)
    }

    override fun ViewGroup.removeView(view: View) {
        removeView(view)
    }

    override fun ViewGroup.removeViews(fromIndex: Int, count: Int) {
        removeViews(fromIndex, count)
    }

    override fun ViewGroup.getChildAt(i: Int): View {
        return getChildAt(i)
    }
}
