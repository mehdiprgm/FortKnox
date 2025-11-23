package org.zen.fortknox.tools.formatting

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher

class PhoneNumberFormattingTextWatcher : TextWatcher {
    private var current = ""
    private val maxDigits = 11 // Maximum number of digits (e.g., 09032748106)

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        if (s.toString() != current) {
            // 1. Remove all non-digit characters to get the raw number
            val rawNumber = s.toString().replace(nonDigits, "")

            // 2. Truncate the number if it exceeds the max allowed digits
            val userInput = if (rawNumber.length > maxDigits) rawNumber.substring(0, maxDigits) else rawNumber

            // 3. Apply the desired formatting
            val formattedText = when {
                userInput.length <= 4 -> {
                    // Up to 4 digits: e.g., "0903"
                    userInput
                }
                userInput.length <= 7 -> {
                    // 5 to 7 digits: e.g., "0903 - 274"
                    val part1 = userInput.substring(0, 4)
                    val part2 = userInput.substring(4)
                    "$part1 - $part2"
                }
                else -> {
                    // 8 to 11 digits: e.g., "0903 - 274 8106"
                    val part1 = userInput.substring(0, 4)
                    val part2 = userInput.substring(4, 7)
                    val part3 = userInput.substring(7)
                    "$part1 - $part2 $part3"
                }
            }

            // 4. Update the 'current' tracker and the Editable object
            current = formattedText

            // This is necessary to prevent infinite loop and apply the formatting
            s.replace(0, s.length, current, 0, current.length)
        }
    }

    companion object {
        // Regex to match any character that is NOT a digit
        private val nonDigits = Regex("[^\\d]")
    }
}