package io.y2k.perstentmapuiresearch

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.children
import io.y2k.research.type
import io.y2k.research.view
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view()
            .let { Interpreter.convert(this, it) }
            .let { setContentView(it) }
    }
}

@Suppress("UNCHECKED_CAST")
object Interpreter {

    fun convert(context: Context, map: PersistentMap<String, Any>): View {
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

    private fun setProperty(view: View, key: String, value: Any) {
        val setterName = makeSetterName(key)
        val setter = view::class.java.methods
            .find {
                it.name == setterName
                        && it.parameterTypes.size == 1
                        && isAssignableFrom(it.parameterTypes[0], value)
            }
            ?: error("view=${view::class.java.simpleName}, method=$setterName, type=${value::class.java.simpleName}")
        setter(view, value)
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
            .map { convert(viewGroup.context, it) }
            .forEach { viewGroup.addView(it) }
    }
}
