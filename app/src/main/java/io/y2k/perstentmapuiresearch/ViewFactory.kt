package io.y2k.perstentmapuiresearch

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.collection.LruCache
import io.y2k.research.common.children
import io.y2k.research.common.memo
import io.y2k.research.common.type
import io.y2k.research.common.λ
import kotlinx.collections.immutable.PersistentMap
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ViewFactory {

    private val cache = LruCache<Class<*>, Array<out Method>>(100)
    private val methodCache = LruCache<List<Any>, Method>(200)

    fun makeView(context: Context, map: PersistentMap<String, Any>): View {
        println("LOGX :: Make view ${map[type]}")
        val viewTypeName = map[type] as String
        val view = makeView(context, viewTypeName)

        view.setPadding(0, 0, 0, 0)
        (view as? TextView)?.includeFontPadding = false

        map.forEach { (key, value) ->
            if (key != type && key != children && key != memo && key != "@fabric")
                setProperty(view, key, value)
        }

        return view
    }

    private fun makeView(context: Context, viewTypeName: String): View {
        val cls = Class.forName("android.widget.$viewTypeName")
        return cls.constructors.first { it.parameterTypes.size == 1 }.newInstance(context) as View
    }

    @Suppress("UNCHECKED_CAST")
    fun setProperty(view: View, key: String, value: Any) {
        println("LOGX :: Set property view ${view::class.java.simpleName}.$key = $value")
        val setterName = makeSetterName(key)
        if (setterName.endsWith("Listener")) {
            val setter = findSetter(view, setterName)
                ?: error("view=${view::class.java.simpleName}, method=$setterName, type=${value::class.java.simpleName}")
            val listener =
                Proxy.newProxyInstance(view::class.java.classLoader, arrayOf(setter.parameterTypes[0])) { _, _, args ->
                    (value as λ<Any>).f(args[0])
                }
            setter(view, listener)
        } else {
            val setter = findSetter(view, setterName, value)
                ?: error("view=${view::class.java.simpleName}, method=$setterName, type=${value::class.java.simpleName}")
            setter(view, value)
        }
    }

    private fun findSetter(view: View, setterName: String, value: Any? = null): Method? {
        val key = listOf(view::class.java, setterName, if (value == null) Unit::class.java else value::class.java)
        return methodCache.get(key) ?: run {
            val m = fastGetMethods(view).find {
                it.name == setterName
                        && it.parameterTypes.size == 1
                        && (value == null || isAssignableFrom(it.parameterTypes[0], value))
            }
            m?.also { methodCache.put(key, m) }
        }
    }

    private fun fastGetMethods(view: View): Array<out Method> {
        val clazz = view::class.java
        return cache.get(clazz) ?: clazz.methods.also { cache.put(clazz, it) }
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
}
