package org.zen.fortknox.activity.user.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.user.register.RegisterProfileActivity
import org.zen.fortknox.databinding.ActivityLoginBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.viewmodel.ApiViewModel
import org.zen.fortknox.viewmodel.DatabaseViewModel
import androidx.core.content.edit
import com.google.gson.Gson
import org.zen.fortknox.activity.login.PasscodeLoginActivity
import org.zen.fortknox.api.entity.ApiEmail
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.tools.toDatabaseUser
import org.zen.fortknox.tools.AppException
import org.zen.fortknox.tools.createLocalDateTime
import org.zen.fortknox.tools.generatePassword
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.getTime
import org.zen.fortknox.tools.isInternetConnected

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityLoginBinding

    private lateinit var databaseViewModel: DatabaseViewModel
    private lateinit var apiViewModel: ApiViewModel


//    "User created successfully.",
//    "Your account has been created and you are ready to login to the application",

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()

        b.tvNewProfile.setOnClickListener(this)
        b.btnLogin.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvNewProfile -> {
                startActivity(Intent(this, RegisterProfileActivity::class.java))
            }

            R.id.btnLogin -> {
                login()
            }
        }
    }

    private fun initViewModels() {
        apiViewModel = ViewModelProvider(this)[ApiViewModel::class.java]
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun login() {
        val username = b.txtUsername.text.toString()
        val password = b.txtPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            if (isInternetConnected(this)) {
                lifecycleScope.launch {
                    try {
                        val loadingDialog = Dialogs.load(
                            this@LoginActivity, "Logging in", "Checking the username, please wait..."
                        )

                        /* Send request and get the user from server */
                        loadingDialog.show()
                        val result = apiViewModel.getUser(username)
                        loadingDialog.dismiss()

                        result.onSuccess { user ->
                            if (user.username == username) {
                                if (password == user.password) {
                                    val isLocked = user.isLocked

                                    /* The account is locked */
                                    if (isLocked) {
                                        Dialogs.showMessage(
                                            this@LoginActivity,
                                            "Profile closed",
                                            "The access of your profile is closed.\nContact the admin to open your account",
                                            DialogType.Error
                                        )
                                    } else {
                                        if (user.is2FAActivated) {
                                            val verificationCode =
                                                generatePassword(5, true, false, false)

                                            val timeDate = createLocalDateTime("${getDate()} ${getTime()}").toString()
                                            val apiEmail = ApiEmail(
                                                timeDate,
                                                user.emailAddress,
                                                verificationCode
                                            )

                                            loadingDialog.show()
                                            if (isEmailSendSuccessfully(apiEmail)) {
                                                //Remove this later
                                                Toast.makeText(
                                                    this@LoginActivity, apiEmail.code, Toast.LENGTH_LONG
                                                ).show()

                                                /* Send data into the verification activity to continue */
                                                val intent = Intent(
                                                    this@LoginActivity,
                                                    CodeVerificationActivity::class.java
                                                )

                                                intent.putExtra("ApiEmail", apiEmail)
                                                intent.putExtra("ApiUser", user)

                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Dialogs.showMessage(
                                                    this@LoginActivity,
                                                    "Sending failed",
                                                    "Failed to send the verification code to your email.\nCheck internet connection and try again",
                                                    DialogType.Error
                                                )
                                            }

                                            loadingDialog.dismiss()
                                        } else {/* Save the username into preferences *//* Save the user into local database */
                                            if (isUserSavedLocalDatabase(user)) {
                                                startActivity(
                                                    Intent(
                                                        this@LoginActivity,
                                                        PasscodeLoginActivity::class.java
                                                    )
                                                )

                                                finish()
                                            }
                                        }
                                    }
                                } else {
                                    Dialogs.showMessage(
                                        this@LoginActivity,
                                        "Invalid password",
                                        "The password is not correct",
                                        DialogType.Error
                                    )
                                }
                            } else {
                                Dialogs.showMessage(
                                    this@LoginActivity,
                                    "Invalid username",
                                    "The username is not correct",
                                    DialogType.Error
                                )
                            }

                        }

                        result.onFailure { ex ->
                            if (ex is AppException.UserNotFoundException) {
                                Dialogs.showMessage(
                                    this@LoginActivity,
                                    "No user found",
                                    "The username is not correct",
                                    DialogType.Error
                                )
                            } else {
                                Dialogs.showException(this@LoginActivity, ex as Exception)
                            }
                        }
                    } catch (ex: Exception) {
                        Dialogs.showException(this@LoginActivity, ex)
                    }
                }
            } else {
                lifecycleScope.launch {
                    Dialogs.showNoInternetConnection(this@LoginActivity)
                }
            }
        }
    }

    private suspend fun isEmailSendSuccessfully(apiEmail: ApiEmail): Boolean {
        return try {
//            val response = apiViewModel.sendVerificationEmail(apiEmail)
//            response.isSuccess
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    private suspend fun isUserSavedLocalDatabase(apiUser: ApiUser): Boolean {
        return try {
            /* Insert user into local database */
            /* Also put username into the preferences to get the user object from database later */
            val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
            databaseViewModel.addUser(apiUser.toDatabaseUser())

            pref.edit(commit = true) {
                putBoolean("LoggedIn", true)
                putString("Username", apiUser.username)
            }

            true
        } catch (ex: Exception) {
            Dialogs.showException(this, ex)
            false
        }
    }
}