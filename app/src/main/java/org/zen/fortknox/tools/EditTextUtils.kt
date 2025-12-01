package org.zen.fortknox.tools

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import org.zen.fortknox.R
import kotlin.math.max
import kotlin.text.isEmpty
import kotlin.text.replace
import kotlin.toString

fun EditText.setLimiter(textView: TextView, limit: Int, removeSpaces: Boolean = false
) {
    val editText = this

    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var text = editText.text.toString()
            if (removeSpaces) {
                text = text.replace(" ", "");
            }

            if (text.isEmpty()) {
                textView.visibility = View.INVISIBLE
            } else {
                textView.visibility = View.VISIBLE
                textView.text = "${text.length} / $limit"

            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
}

fun EditText.validateData(condition: Boolean, errorMessage: String
): Int {
    val editText = this

    if (condition) {
        editText.error = errorMessage
        return -1
    }

    editText.error = null
    return 0
}

fun TextInputLayout.validateData(condition: Boolean, errorMessage: String
): Int {
    val textInputLayout = this

    if (condition) {
        textInputLayout.error = errorMessage
        textInputLayout.isErrorEnabled = true

        return -1
    }

    textInputLayout.isErrorEnabled = false
    return 0
}

fun EditText.validateRealTime(
    textInputLayout: TextInputLayout,
    errorMessage: String
) {
    val editText = this

    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                textInputLayout.error = errorMessage
            } else {
                textInputLayout.isErrorEnabled = false
                textInputLayout.error = null
            }
        }
    })
}

fun EditText.showLimiter(textView: TextView, maxChars: Int, isChangingColors: Boolean = true) {
    val editText = this

    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val currentTextLength = s.toString().length
            textView.text = "$currentTextLength / $maxChars"

            /* Nothing entered, make textview invisible */
            textView.isVisible = currentTextLength!= 0

            if (isChangingColors) {
                if (currentTextLength == maxChars) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.green))
                } else {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }
        }
    })
}