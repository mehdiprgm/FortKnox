package org.zen.fortknox.activity.login

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.MainActivity
import org.zen.fortknox.databinding.ActivityPasscodeLoginBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.isDeviceSecure
import org.zen.fortknox.tools.lockOrientation
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.viewmodel.DatabaseViewModel

class PasscodeLoginActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {
    private lateinit var b: ActivityPasscodeLoginBinding

    private lateinit var databaseViewModel: DatabaseViewModel
    private lateinit var databaseSecurityCode: String

    private var resetJob: Job? = null

    private val requestCode = 1001
    private var attempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityPasscodeLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()

        b.imgFingerprint.setOnClickListener(this)
        b.txtPasscode.addTextChangedListener(this)

        b.btn0.setOnClickListener(this)
        b.btn1.setOnClickListener(this)
        b.btn2.setOnClickListener(this)
        b.btn3.setOnClickListener(this)
        b.btn4.setOnClickListener(this)
        b.btn5.setOnClickListener(this)
        b.btn6.setOnClickListener(this)
        b.btn7.setOnClickListener(this)
        b.btn8.setOnClickListener(this)
        b.btn9.setOnClickListener(this)

        b.btnDelete.setOnClickListener(this)
        b.btnClear.setOnClickListener(this)

        lockOrientation()
    }

    override fun onResume() {
        super.onResume()

        startKeyPadAnimation()
        loadDatabaseSecurityCode()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9 -> {/* Get the textview text and append it to the edittext */
                val currentPasscode = b.txtPasscode.text.toString()
                val newText = (view as TextView).text.toString()

                b.txtPasscode.setText("$currentPasscode$newText")
            }

            R.id.btnDelete -> {
                val text = b.txtPasscode.text.toString()

                if (text.isNotEmpty()) {/* Remove the last character */
                    b.txtPasscode.setText(text.dropLast(1))

                    /* Move cursor to the end */
                    b.txtPasscode.setSelection(b.txtPasscode.text.length)
                }
            }

            R.id.btnClear -> {
                b.txtPasscode.text.clear()
            }

            R.id.imgFingerprint -> {
                if (isDeviceSecure(this)) {
                    requestDevicePin()
                } else {
                    lifecycleScope.launch {
                        Dialogs.showMessage(
                            this@PasscodeLoginActivity,
                            title = "Fingerprint is unavailable",
                            message = "This device doesn't have lock screen or registered fingerprint.\nChange it from device settings and try again.",
                            dialogType = DialogType.Error
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        /* Get the fingerprint result */
        if (requestCode == this@PasscodeLoginActivity.requestCode) {
            if (resultCode == RESULT_OK) {
                b.txtPasscode.text.clear()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = b.txtPasscode.text.toString()

        /* Security code max length is 4 characters */
        if (text.isNotEmpty() && text.length == 4) {
            if (text == databaseSecurityCode) {/* Cancel any pending reset */
                resetJob?.cancel()

                attempts = 0
                b.txtPasscode.text.clear()

                startActivity(Intent(this, MainActivity::class.java))
            } else {
                attempts++

                /* After 3 times lock the application until cooldown */
                if (attempts == 3) {
                    attempts = 0
                    startCountdownTimer(getSettings().lockTimeout)
                } else {
                    b.tvMessage.text = "The passcode is not valid"

                    /* In 2 seconds, run the following code on the main screen thread */
                    Handler(Looper.getMainLooper()).postDelayed({
                        b.tvMessage.text = "Enter Passcode"
                    }, 2000)

                    b.txtPasscode.setText("")
                    b.tvMessage.text = "Passcode is incorrect"

                    /* Cancel previous reset job */
                    resetJob?.cancel()

                    /* After 2 seconds reset the text */
                    resetJob = lifecycleScope.launch {
                        delay(2000)
                        b.tvMessage.text = "Enter Passcode"
                    }
                }
            }
        }
    }

    private fun startCountdownTimer(seconds: Int) {
        isLoginOptionsEnabled(false)

        val timer =
            object :
                CountDownTimer(seconds.toLong() * 1000, 1000) { /* 10 seconds, tick every 1 sec */
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000

                    /* When no seconds left finish the timer */
                    if (secondsRemaining == 0L) {
                        onFinish()
                    } else {
                        /* Update text on every tick */
                        b.tvMessage.text =
                            "Too many attempts\ntry again in $secondsRemaining seconds"
                    }
                }

                override fun onFinish() {
                    /* Reset everything to normal */
                    b.tvMessage.text = "Enter Passcode"
                    isLoginOptionsEnabled(true)
                }
            }

        timer.start()
    }

    private fun isLoginOptionsEnabled(enabled: Boolean) {
        getAllViews(b.main, false).forEach { view ->
            view.isEnabled = enabled
        }

        /* this must be always disabled */
        b.txtPasscode.isEnabled = false
    }

    private fun startKeyPadAnimation() {
        var animationDuration = 500L
        val views = getAllViews(b.layPasscode, false)

        views.forEachIndexed { _, v ->
            animationDuration += 50

            val animator = ObjectAnimator.ofFloat(v, "rotationY", 100f, 0f).apply {
                duration = animationDuration
                interpolator = AccelerateDecelerateInterpolator()
            }

            animator.start()
        }
    }

    private fun loadDatabaseSecurityCode() {
        try {/* Read the user from local database and load the security code */

            val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
            val username = pref.getString("Username", "")

            val user = databaseViewModel.getUser(username!!)
            databaseSecurityCode = user?.securityCode ?: ""
        } catch (ex: Exception) {
            lifecycleScope.launch {
                Dialogs.showException(this@PasscodeLoginActivity, ex)
            }
        }
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun requestDevicePin() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val intent =
            keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Please enter your PIN")

        if (intent != null) {
            startActivityForResult(intent, requestCode)
        }
    }
}