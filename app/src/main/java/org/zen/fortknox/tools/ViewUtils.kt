package org.zen.fortknox.tools

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import org.zen.fortknox.R
import org.zen.fortknox.tools.theme.Theme

fun setButtonBackground(context: Context, button: Button, colorResource: Int) {
    button.backgroundTintList = ContextCompat.getColorStateList(context, colorResource)
}

fun getAllViews(view: View, includeViewGroup: Boolean): MutableList<View> {
    val result = mutableListOf<View>()

    if (view is ViewGroup) {
        if (includeViewGroup) {
            result += view
        }

        for (i in 0 until view.childCount) {
            result += getAllViews(view.getChildAt(i), includeViewGroup)
        }
    } else {
        result += view
    }

    return result
}

fun resizeTextViewDrawable(context: Context, textView: TextView, drawableIcon: Int, size: Int) {
    val density = context.resources.displayMetrics.density
    val desiredWidthInPx = (size * density).toInt()
    val desiredHeightInPx = (size * density).toInt()

    val drawable = ContextCompat.getDrawable(context, drawableIcon)
    drawable?.setBounds(0, 0, desiredWidthInPx, desiredHeightInPx)
    textView.setCompoundDrawables(drawable, null, null, null)
}

fun disableScreenPadding(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
        v.setPadding(0, 0, 0, 0)
        insets
    }
}


fun switchFragment(
    activity: AppCompatActivity, activeFragment: Fragment, newFragment: Fragment
): Fragment {
    if (newFragment == activeFragment) {
        return newFragment
    }

    val transaction = activity.supportFragmentManager.beginTransaction()
    // Hide the current active fragment
    transaction.hide(activeFragment)
    // Show the new fragment
    transaction.show(newFragment)
    // Commit the transaction
    transaction.commit()

    // Update the active fragment
    return newFragment
}

/* This function help to reduce code to access resource color */
fun getResourceColor(context: Context, colorResource: Int): Int {
    return ContextCompat.getColor(context, colorResource)
}

fun changeImageViewColor(context: Context, imageView: ShapeableImageView, color: Int) {
    ImageViewCompat.setImageTintList(
        imageView,
        ColorStateList.valueOf(ContextCompat.getColor(context, color))
    )
}

fun changeThemeButtonIcon(fab: FloatingActionButton, theme: Theme) {
    when (theme) {
        Theme.System -> {
            fab.setImageResource(R.drawable.ic_star)
        }

        Theme.Light -> {
            fab.setImageResource(R.drawable.ic_sun)
        }

        Theme.Dark -> {
            fab.setImageResource(R.drawable.ic_moon)
        }
    }
}