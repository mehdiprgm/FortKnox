package org.zen.fortknox.tools

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

fun startDialogAnimation(view: View) {
    var animationDuration = 100L
    val views = getAllViews(view, false)

    views.forEachIndexed { _, v ->
        animationDuration += 15

        val animator = ObjectAnimator.ofFloat(v, "translationY", 100f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
        }

        animator.start()
    }
}