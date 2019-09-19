package android.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

@Suppress("unused")
class EditTextWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: (String) -> Unit = {}
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            listener(s.toString())
        }
    }
    private var initText: String = ""

    private val editText
        get() = getChildAt(0) as EditText

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        editText.setText(initText)
        editText.addTextChangedListener(textWatcher)
    }

    fun setText(text: String) {
        val editText = getChildAt(0) as? EditText ?: run {
            initText = text
            return
        }
        editText.removeTextChangedListener(textWatcher)
        editText.setText(text)
        editText.addTextChangedListener(textWatcher)
    }

    fun setOnEditListener(listener: (String) -> Unit) {
        this.listener = listener
    }
}
