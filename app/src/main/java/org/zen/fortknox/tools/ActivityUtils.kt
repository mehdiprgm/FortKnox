package org.zen.fortknox.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager

fun Activity.disableScreenshot() {
    this.window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
    )
}

fun Activity.enableScreenshot() {
    this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.lockOrientation() {
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}