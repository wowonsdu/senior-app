package zdrowy.senior.io.ui.patient

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

internal object TimeInputMasker {
    fun attach(editText: TextInputEditText) {
        editText.addTextChangedListener(MaskWatcher(editText))
    }

    fun normalize(value: String): String? {
        val digits = value.filter { it.isDigit() }
        if (digits.length != 4) return null
        val hours = digits.substring(0, 2).toIntOrNull() ?: return null
        val minutes = digits.substring(2, 4).toIntOrNull() ?: return null
        if (hours !in 0..23 || minutes !in 0..59) return null
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private class MaskWatcher(
        private val editText: TextInputEditText
    ) : TextWatcher {
        private var updating = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (updating) return
            val raw = s?.toString().orEmpty()
            val digits = raw.filter { it.isDigit() }.take(4)
            val formatted = formatDigits(digits)
            if (formatted == raw) return
            updating = true
            editText.setText(formatted)
            val cursor = cursorPosition(digits.length, formatted.length)
            editText.setSelection(cursor)
            updating = false
        }

        private fun formatDigits(digits: String): String {
            if (digits.isEmpty()) return ""
            val builder = StringBuilder()
            val padded = digits.padEnd(4, '-')
            builder.append(padded.substring(0, 2))
            builder.append(':')
            builder.append(padded.substring(2, 4))
            return builder.toString()
        }

        private fun cursorPosition(digitCount: Int, max: Int): Int {
            if (digitCount <= 0) return 0
            val pos = if (digitCount <= 2) digitCount else digitCount + 1
            return pos.coerceAtMost(max)
        }
    }
}
