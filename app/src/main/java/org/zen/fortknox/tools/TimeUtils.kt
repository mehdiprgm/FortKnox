package org.zen.fortknox.tools

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun getDate(): String {
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return String.Companion.format(Locale.getDefault(), "%04d/%02d/%02d", year, month, day)
}

fun getTime(): String {
    val calendar = Calendar.getInstance()

    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, seconds)
}

fun createLocalDateTime(input: String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(input, formatter)

    return dateTime
}