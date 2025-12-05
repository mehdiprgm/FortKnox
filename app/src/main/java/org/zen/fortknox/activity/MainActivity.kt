package org.zen.fortknox.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import org.zen.fortknox.R
import org.zen.fortknox.adapter.viewpager.ScreenNavigationAdapter
import org.zen.fortknox.databinding.ActivityMainBinding
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getResourceColor
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.setScreenshotStatus

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityMainBinding
    private lateinit var viewPagerAdapter: ScreenNavigationAdapter

    private var currentFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        setupViewPager()

        b.layHome.setOnClickListener(this)
        b.layMonitor.setOnClickListener(this)
        b.layProfile.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        setScreenshotStatus(getSettings().allowScreenshot)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        /* Save the current fragment index, so when activity re-create itself, we can read it and show the right fragment to the user */
        outState.putInt("CurrentFragmentIndex", currentFragmentIndex)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        /* Get the index and update the UI */
        currentFragmentIndex = savedInstanceState.getInt("CurrentFragmentIndex")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layHome -> {
                currentFragmentIndex = 0
            }

            R.id.layMonitor -> {
                currentFragmentIndex = 1
            }

            R.id.layProfile -> {
                currentFragmentIndex = 2
            }
        }

        b.vpMain.currentItem = currentFragmentIndex
        updateSelectedItem()
    }

    private fun setupViewPager() {
        viewPagerAdapter = ScreenNavigationAdapter(this)
        b.vpMain.apply {
            adapter = viewPagerAdapter
            isUserInputEnabled = true
        }

        b.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                currentFragmentIndex = position
                updateSelectedItem()
            }
        })
    }

    private fun updateSelectedItem() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        animation.duration = 300

        getAllViews(b.layNavigation, false).filterIsInstance<ShapeableImageView>()
            .forEach { imageView ->
                imageView.setBackgroundColor(getResourceColor(this, R.color.panelBackground))
            }

        when (currentFragmentIndex) {
            0 -> {
                b.imgHome.setBackgroundColor(getResourceColor(this, R.color.theme))
                b.imgHome.startAnimation(animation)
            }

            1 -> {
                b.imgMonitor.setBackgroundColor(getResourceColor(this, R.color.theme))
                b.imgMonitor.startAnimation(animation)
            }

            2 -> {
                b.imgProfile.setBackgroundColor(getResourceColor(this, R.color.theme))
                b.imgProfile.startAnimation(animation)
            }
        }
    }

    fun showBottomNavigation(isVisible: Boolean) {
        b.layNavigation.isVisible = isVisible
    }
}