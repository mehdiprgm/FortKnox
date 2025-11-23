package org.zen.fortknox.activity.start

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.zen.fortknox.activity.user.login.LoginActivity
import org.zen.fortknox.activity.login.PasscodeLoginActivity
import org.zen.fortknox.databinding.ActivitySplashScreenBinding
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.lockOrientation
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.loadSettings
import org.zen.fortknox.tools.theme.Theme
import org.zen.fortknox.tools.theme.changeTheme

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        lockOrientation()

        loadApplicationSettings()
        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 1000 /* 1.5 Seconds */

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            b.pbProgress.progress = progress
        }

        animator.start()

        /* Start next activity when animation complete */
        handler.postDelayed({
            startNextActivity()
        }, 1000)
    }

    private fun startNextActivity() {
        val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)

        /* Check the login status, if logged in go to the passcode page */
        /* If not logged in go to the login page */
        val isLoggedIn = pref.getBoolean("LoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, PasscodeLoginActivity::class.java))
        } else {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }

        finish()
    }

    private fun loadApplicationSettings() {
        /* Load settings from device into memory */
        loadSettings(this)

        val settings = getSettings()

        /* Check the theme */
        when(settings.theme) {
            Theme.Light -> {
                changeTheme(Theme.Light)
            }

            Theme.Dark -> {
                changeTheme(Theme.Dark)
            }

            Theme.System -> {
                changeTheme(Theme.System)
            }
        }
    }
}