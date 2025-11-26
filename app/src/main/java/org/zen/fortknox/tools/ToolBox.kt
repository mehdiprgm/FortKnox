package org.zen.fortknox.tools

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.zen.fortknox.application.AppSettings
import org.zen.fortknox.tools.theme.Theme
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.text.any
import kotlin.text.isDigit
import kotlin.text.isLetterOrDigit
import kotlin.text.isLowerCase
import kotlin.text.isUpperCase

const val preferencesName = "Preferences"

val variables = mutableMapOf<String, Any>()

val selectedItems = mutableSetOf<Any>()
val selectedViews = mutableSetOf<MaterialCheckBox>()

fun getPasswordStrength(password: String): Int {
    var score = 0

    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.length >= 16) score++

    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return score
}

/* Read the settings from memory and save it inside shared preferences */
fun applySettings(context: Context) {
    /* Read settings from memory */
    val memorySettings = variables["Settings"] as AppSettings

    /* Load shared preferences */
    val pref = context.getSharedPreferences(preferencesName, MODE_PRIVATE)

    /* Convert settings into json (serialize) */
    val gson = Gson()
    val settingsJson = gson.toJson(memorySettings)

    /* Write changes into system */
    pref.edit(commit = true) {
        putString("Settings", settingsJson)
    }
}

/* Load settings from device preferences into the application memory */
fun loadSettings(context: Context) {
    /* Load shared preferences */
    val pref = context.getSharedPreferences(preferencesName, MODE_PRIVATE)

    /* Load json from device */
    val gson = Gson()
    val settingsJson = pref.getString("Settings", "")

    /* Convert json into object (deserialize) */
    val type = object : TypeToken<AppSettings>() {}.type
    val settingsObject = gson.fromJson<AppSettings>(settingsJson, type)

    /* Write changes into the memory */
    if (settingsObject == null) {
        variables["Settings"] = createNewSettingsObject()
    } else {
        variables["Settings"] = settingsObject
    }
}

fun getSettings(): AppSettings {
    return variables["Settings"] as AppSettings
}

fun createNewSettingsObject() : AppSettings {
    val settings = AppSettings(
        theme = Theme.System,
        confirmExit = true,
        allowScreenshot = false,
        lockTimeout = 10
    )

    return settings
}