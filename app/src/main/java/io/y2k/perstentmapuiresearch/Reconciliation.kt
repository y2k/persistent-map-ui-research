package io.y2k.perstentmapuiresearch

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import io.y2k.research.common.children
import io.y2k.research.common.type
import kotlinx.collections.immutable.*
import kotlin.math.min

object Reconciliation {

    fun reconcile(
        prevChildren: PersistentList<PersistentMap<String, Any>>,
        actualChildren: PersistentList<PersistentMap<String, Any>>,
        root: ViewGroup
    ) {
        if (prevChildren.size > actualChildren.size)
            removeUnusedChildrenFrom(root, actualChildren.size)
        if (prevChildren.size < actualChildren.size)
            addNewChildren(root, actualChildren.subList(prevChildren.size, actualChildren.size))
        for (i in 0 until min(prevChildren.size, actualChildren.size))
            reconcileItem(prevChildren[i], actualChildren[i], root[i], root, i)
    }

    private fun reconcileItem(
        prev: PersistentMap<String, Any>, actual: PersistentMap<String, Any>, view: View, root: ViewGroup, i: Int
    ) {
        if (isSameType(prev, actual)) {
            updateProperties(prev, actual, view)
            tryReconciliationChildren(view, prev, actual)
        } else {
            root.removeView(view)
            val newView = mkView(actual, root.context)
            root.addView(newView, i)
            tryReconciliationChildren(newView, persistentMapOf(), actual)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryReconciliationChildren(
        view: View, prev: PersistentMap<String, Any>, actual: PersistentMap<String, Any>
    ) {
        if (view !is ViewGroup) return
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
            .filterNot { it in listOf(type, children) || actual[it] == prev[it] }
            .forEach { ViewFactory.setProperty(view, it, actual[it]!!) }
    }

    private fun removeUnusedProps(unusedProps: Set<String>) {
        if (unusedProps.isNotEmpty()) error("Unsupported")
    }

    private fun mkView(actual: PersistentMap<String, Any>, context: Context): View =
        ViewFactory.makeView(context, actual)
}