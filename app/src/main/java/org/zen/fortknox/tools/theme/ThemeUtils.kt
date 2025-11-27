package org.zen.fortknox.tools.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

fun isDarkTheme(context: Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}

fun changeTheme(theme: Theme) {
    when (theme) {
        Theme.Light -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        Theme.Dark -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        Theme.System -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

fun getThemeText(theme: Theme) : String {
    return when(theme) {
        Theme.System -> "System"
        Theme.Dark -> "Dark"
        Theme.Light -> "Light"
    }
}