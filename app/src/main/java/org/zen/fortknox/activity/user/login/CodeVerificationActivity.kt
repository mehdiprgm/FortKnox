package org.zen.fortknox.activity.user.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.login.PasscodeLoginActivity
import org.zen.fortknox.api.entity.ApiEmail
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.databinding.ActivityCodeVerificationBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.toDatabaseUser
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.viewmodel.DatabaseViewModel
import kotlin.collections.iterator

class CodeVerificationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityCodeVerificationBinding

    private lateinit var databaseViewModel: DatabaseViewModel

    private lateinit var apiEmail: ApiEmail
    private lateinit var apiUser: ApiUser

    private var countDownTimer: CountDownTimer? = null

    private val totalTime = 3 * 60 * 1000L /* 3 Minutes in milliseconds */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityCodeVerificationBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        setupTextChangedListeners()
        loadInformation()

        startTimer()

        b.btnVerify.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnVerify -> {
                verifyCode()
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {/* Update text every seconds */
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                b.tvTimer.text = "Code has been expired"

                lifecycleScope.launch {
                    Dialogs.showMessage(
                        this@CodeVerificationActivity,
                        "Code expired",
                        "The code has been expired, please try again later",
                        DialogType.Error
                    )

                    finish()
                }
            }
        }.start()
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60

        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        b.tvTimer.text = "Reaming time $timeFormatted"
    }

    private fun loadInformation() {
        val tmpApiEmail = intent.getParcelableExtra<ApiEmail>("ApiEmail")
        val tmpApiUser = intent.getParcelableExtra<ApiUser>("ApiUser")

        if (tmpApiEmail != null) {
            apiEmail = ApiEmail(
                timeDate = tmpApiEmail.timeDate,
                emailAddress = tmpApiEmail.emailAddress,
                code = tmpApiEmail.code
            )

            b.tvEmailAddress.text = apiEmail.emailAddress
        }

        if (tmpApiUser != null) {
            apiUser = tmpApiUser
        }
    }

    private fun setupTextChangedListeners() {
        val map = mapOf<EditText, () -> Unit>(
            b.txtCode1 to {
                b.txtCode2.requestFocus()
            },

            b.txtCode2 to {
                b.txtCode3.requestFocus()
            },

            b.txtCode3 to {
                b.txtCode4.requestFocus()
            },

            b.txtCode4 to {
                b.txtCode5.requestFocus()
            },

            b.txtCode5 to {
                /* Empty body, just to execute the event listener *//* Convert the letter to the uppercase */
            })

        for ((editText, focusAction) in map) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) {
                        val originalText = s.toString()
                        val uppercaseText = originalText.uppercase()

                        if (originalText != uppercaseText) {
                            s.replace(0, s.length, uppercaseText)
                        }

                        focusAction.invoke()
                    }
                }
            })
        }
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private suspend fun isUserSavedLocalDatabase(apiUser: ApiUser): Boolean {
        return try {
            val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
            databaseViewModel.addUser(apiUser.toDatabaseUser())

            val gson = Gson()
            val userJson = gson.toJson(apiUser)

            pref.edit(commit = true) {
                putBoolean("LoggedIn", true)
                putString("User", userJson)
            }

            true
        } catch (ex: Exception) {
            Dialogs.showException(this, ex)
            false
        }
    }

    private fun verifyCode() {
        lifecycleScope.launch {
            /* Assemble verification code */
            val verificationCode =
                "${b.txtCode1.text}${b.txtCode2.text}${b.txtCode3.text}${b.txtCode4.text}${b.txtCode5.text}"

            if (verificationCode == apiEmail.code) {/* These is still time, so go to the next activity */
                if (isUserSavedLocalDatabase(apiUser)) {
                    startActivity(
                        Intent(
                            this@CodeVerificationActivity,
                            PasscodeLoginActivity::class.java
                        )
                    )
                    finish()
                }
            } else {
                Dialogs.showMessage(
                    this@CodeVerificationActivity,
                    "Invalid code",
                    "The verification code is invalid",
                    DialogType.Error
                )
            }
        }
    }
}