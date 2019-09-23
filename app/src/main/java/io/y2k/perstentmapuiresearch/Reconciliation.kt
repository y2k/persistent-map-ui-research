package io.y2k.perstentmapuiresearch

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import io.y2k.research.common.MemoViewFactory
import io.y2k.research.common.children
import io.y2k.research.common.memo
import io.y2k.research.common.type
import kotlinx.collections.immutable.*

object Reconciliation {

    private var propEqualsCount = 0
    private var recursiveLevel = 0

    fun reconcile(
        prevChildren: PersistentList<PersistentMap<String, Any>>,
        actualChildren: PersistentList<PersistentMap<String, Any>>,
        root: ViewGroup
    ) {
        recursiveLevel++
        if (recursiveLevel == 1) propEqualsCount = 0

        require(prevChildren.size == root.childCount) {
            "${root::class.java.simpleName}[${root.childCount}] != ${prevChildren.size}, actual = ${actualChildren.size}"
        }

        if (prevChildren.size > actualChildren.size)
            removeUnusedChildrenFrom(root, actualChildren.size)
        if (prevChildren.size < actualChildren.size)
            addNewChildren(root, actualChildren.subList(prevChildren.size, actualChildren.size))
        for (i in 0 until actualChildren.size)
            reconcileItem(prevChildren.getOrElse(i) { persistentMapOf() }, actualChildren[i], root[i], root, i)

        if (recursiveLevel == 1) println("LOGX :: propEqualsCount = $propEqualsCount")
        recursiveLevel--
    }

    private fun reconcileItem(
        prev: PersistentMap<String, Any>, actual: PersistentMap<String, Any>, view: View, root: ViewGroup, i: Int
    ) {
        val (prev2, view2) =
            if (isSameType(prev, actual)) {
                updateProperties(prev, actual, view)
                prev to view
            } else {
                root.removeView(view)
                val newView = mkView(actual, root.context)
                root.addView(newView, i)
                persistentMapOf<String, Any>() to newView
            }
        tryReconciliationChildren(prev2, actual, view2)
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryReconciliationChildren(
        prev: PersistentMap<String, Any>,
        actual: PersistentMap<String, Any>,
        view: View
    ) {
        if (view !is ViewGroup) return
        if (actual[memo] != null) {
            if (actual[memo] != prev[memo]) {
                val prevChildren =
                    if (prev.containsKey("@fabric")) persistentListOf((prev["@fabric"] as MemoViewFactory).f())
                    else prev[children] as? PersistentList<PersistentMap<String, Any>> ?: persistentListOf()
                val actualChildren = persistentListOf((actual["@fabric"] as MemoViewFactory).f())
                reconcile(prevChildren, actualChildren, view)
            }
        } else
            reconcile(
                prev[children] as? PersistentList<PersistentMap<String, Any>> ?: persistentListOf(),
                actual[children] as? PersistentList<PersistentMap<String, Any>> ?: persistentListOf(),
                view
            )
    }

    private fun isSameType(prev: PersistentMap<String, Any>, actual: PersistentMap<String, Any>): Boolean =
        prev[type] == actual[type]

    private fun removeUnusedChildrenFrom(root: ViewGroup, fromIndex: Int): Unit =
        root.removeViews(fromIndex, root.childCount - fromIndex)

    private fun addNewChildren(root: ViewGroup, list: ImmutableList<PersistentMap<String, Any>>): Unit =
        list.map { mkView(it, root.context) }.forEach { root.addView(it) }

    private fun updateProperties(prev: PersistentMap<String, Any>, actual: PersistentMap<String, Any>, view: View) {
        removeUnusedProps(prev.keys.subtract(actual.keys))
        actual.keys
            .filterNot { it in listOf(type, children, memo, "@fabric") || composeProps(prev, actual, it) }
            .forEach { ViewFactory.setProperty(view, it, actual[it]!!) }
    }

    private fun composeProps(
        prev: PersistentMap<String, Any>,
        actual: PersistentMap<String, Any>,
        key: String
    ): Boolean {
        propEqualsCount++
        return actual[key] == prev[key]
    }

    private fun removeUnusedProps(unusedProps: Set<String>) {
        if (unusedProps.isNotEmpty()) Exception("Unsupported: $unusedProps").printStackTrace()
    }

    @Suppress("UNCHECKED_CAST")
    private fun mkView(actual: PersistentMap<String, Any>, context: Context): View =
        ViewFactory.makeView(context, actual, false)
}
