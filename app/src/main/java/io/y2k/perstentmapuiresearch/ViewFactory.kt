package io.y2k.perstentmapuiresearch

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.y2k.research.common.children
import io.y2k.research.common.type
import io.y2k.research.common.λ
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import java.lang.reflect.Proxy

@Suppress("UNCHECKED_CAST")
object ViewFactory {

    fun makeView(context: Context, map: PersistentMap<String, Any>): View {
        println("LOGX :: Make view ${map[type]}")
        val viewTypeName = map[type] as String
        val view = makeView(context, viewTypeName)

        map.forEach { (key, value) ->
            if (key != type && key != children)
                setProperty(view, key, value)
        }

        val children = map[children]
        if (children != null)
            addChildren(view as ViewGroup, children as PersistentList<PersistentMap<String, Any>>)

        return view
    }

    private fun makeView(context: Context, viewTypeName: String): View {
        val cls = Class.forName("android.widget.$viewTypeName")
        return cls.constructors.first { it.parameterTypes.size == 1 }.newInstance(context) as View
    }

    fun setProperty(view: View, key: String, value: Any) {
        println("LOGX :: Set property view ${view::class.java.simpleName}.$key = $value")
        val setterName = makeSetterName(key)
        if (setterName.endsWith("Listener")) {
            val setter = view::class.java.methods
                .find { it.name == setterName && it.parameterTypes.size == 1 }
                ?: error("view=${view::class.java.simpleName}, method=$setterName, type=${value::class.java.simpleName}")
            val listener =
                Proxy.newProxyInstance(view::class.java.classLoader, arrayOf(setter.parameterTypes[0])) { _, _, args ->
                    (value as λ<Any>).f(args[0])
                }
            setter(view, listener)
        } else {
            val setter = view::class.java.methods
                .find {
                    it.name == setterName
                            && it.parameterTypes.size == 1
                            && isAssignableFrom(it.parameterTypes[0], value)
                }
                ?: error("view=${view::class.java.simpleName}, method=$setterName, type=${value::class.java.simpleName}")
            setter(view, value)
        }
    }

    private fun isAssignableFrom(clazz: Class<*>, value: Any): Boolean {
        if (clazz.simpleName in listOf("int", "Integer")
            && value::class.java.simpleName in listOf("int", "Integer")
        ) return true
        if (clazz.simpleName in listOf("float", "Float")
            && value::class.java.simpleName in listOf("float", "Float")
        ) return true
        return clazz.isAssignableFrom(value::class.java)
    }

    private fun makeSetterName(key: String): String =
        "set" + key.substring(0..0).toUpperCase() + key.substring(1)

    private fun addChildren(viewGroup: ViewGroup, persistentList: PersistentList<PersistentMap<String, Any>>) {
        persistentList
            .map { makeView(viewGroup.context, it) }
            .forEach { viewGroup.addView(it) }
    }
}
