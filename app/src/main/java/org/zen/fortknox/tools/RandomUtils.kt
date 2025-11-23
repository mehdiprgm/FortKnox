package org.zen.fortknox.tools

import android.graphics.Color
import kotlin.random.Random

fun generateRandomColor(): Int {
//    val red = Random.nextInt(150, 256)  // Higher range for lighter shades
//    val green = Random.nextInt(150, 256)
//    val blue = Random.nextInt(150, 256)
//
//    return Color.rgb(red, green, blue)

    val lightShades = listOf(
        Color.rgb(173, 216, 230), // Light Blue
        Color.rgb(255, 255, 153), // Light Yellow
        Color.rgb(144, 238, 144), // Light Green
        Color.rgb(255, 200, 120), // Light Orange
        Color.rgb(210, 180, 140)  // Light Brown (Tan)
    )

    val baseColor = lightShades.random()

// Add a small brightness variation
    val offset = Random.nextInt(-15, 15)
    val red = (Color.red(baseColor) + offset).coerceIn(0, 255)
    val green = (Color.green(baseColor) + offset).coerceIn(0, 255)
    val blue = (Color.blue(baseColor) + offset).coerceIn(0, 255)

    return Color.rgb(red, green, blue)
}

fun generateRandomColors(count: Int): List<Int> {
    val randomColors = mutableSetOf<Int>()
    val themeColor = Color.rgb(208, 135, 162)

    while (randomColors.size < count) {
        val newColor = generateRandomColor()

        if (newColor != themeColor) {
            randomColors.add(newColor)
        }
    }

    return randomColors.toList()
}

fun generatePassword(
    length: Int, includeNumbers: Boolean, includeSymbols: Boolean, toLowerCase: Boolean
): String {
    val letters = ('A'..'Z')
    val numbers = ('0'..'9')
    val symbols = listOf('!', '@', '#', '$', '%', '^', '&', '*')

    val charPool = buildList {
        addAll(letters)
        if (includeNumbers) addAll(numbers)
        if (includeSymbols) addAll(symbols)
    }

    if (charPool.isEmpty()) {
        throw kotlin.IllegalArgumentException("Character pool is empty.")
    }

    val password = (1..length).map { charPool.random() }.joinToString("")

    return if (toLowerCase) password.lowercase() else password
}