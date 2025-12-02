package org.zen.fortknox.activity.application

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.application.AppSettings
import org.zen.fortknox.databinding.ActivitySettingsBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.applySettings
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.tools.theme.changeTheme
import org.zen.fortknox.tools.theme.getThemeText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class SettingsActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    private lateinit var b: ActivitySettingsBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        loadSettings()

        b.layTheme.setOnClickListener(this)
        b.layPasscode.setOnClickListener(this)
        b.layLockTimeout.setOnClickListener(this)
        b.layScreenshot.setOnClickListener(this)
        b.layReportBug.setOnClickListener(this)

        b.switchAllowScreenshots.setOnClickListener(this)
        b.switchAllowScreenshots.setOnCheckedChangeListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layTheme -> {
                lifecycleScope.launch {
                    val selectedTheme = Dialogs.selectTheme(this@SettingsActivity, settings.theme)

                    if (selectedTheme != null) {/* Change application theme and update settings to the selected theme */
                        changeTheme(selectedTheme)

                        settings.theme = selectedTheme
                        applySettings(this@SettingsActivity)

                        loadSettings()
                    }
                }
            }

            R.id.layPasscode -> {
                lifecycleScope.launch {
                    val passcode = Dialogs.textInput(
                        context = this@SettingsActivity,
                        title = "New passcode",
                        message = "Please enter your new passcode",
                        hint = "New passcode",
                        isPassword = true,
                        isNumber = true,
                        cancellable = true
                    )

                    //change passcode here
                    if (passcode.isNotEmpty()) {
                        if (passcode.length == 4) {/* Read username from preferences */
                            val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
                            val username = pref.getString("Username", "")

                            /* Get the user from local database */
                            val user = databaseViewModel.getUser(username!!)
                            user!!.securityCode = passcode

                            /* Change the securityCode and update the user into the database */
                            databaseViewModel.updateUser(user)
                        } else {
                            Dialogs.showMessage(
                                this@SettingsActivity,
                                "Invalid passcode",
                                "The passcode must be 4 digit number.",
                                DialogType.Error
                            )
                        }
                    }
                }
            }

            R.id.layLockTimeout -> {
                lifecycleScope.launch {
                    val selectedLockTimeout =
                        Dialogs.selectLockTimeout(this@SettingsActivity, settings.lockTimeout)

                    if (selectedLockTimeout != null) {
                        settings.lockTimeout = selectedLockTimeout
                        applySettings(this@SettingsActivity)

                        loadSettings()
                    }
                }
            }

            R.id.layScreenshot -> {/* Only the check status change *//* The check change listener in this activity will handle the rest of it */
                b.switchAllowScreenshots.isChecked = !b.switchAllowScreenshots.isChecked
            }

            R.id.layReportBug -> {
                lifecycleScope.launch {
                    if (Dialogs.ask(
                            context = this@SettingsActivity,
                            title = "Open Gmail",
                            message = "You can report bug with gmail application.\nAre you sure you want to continue?",
                            icon = R.drawable.ic_email,
                            cancellable = true
                        )
                    ) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:mfcrisis2016@gmail.com?subject=Report bug".toUri()
                        }

                        /* Optional: restrict to Gmail app if installed */
                        intent.setPackage("com.google.android.gm")

                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    /* Switch check change listener */
    override fun onCheckedChanged(
        buttonView: CompoundButton, isChecked: Boolean
    ) {
        when (buttonView.id) {
            R.id.switchAllowScreenshots -> {
                settings.allowScreenshot = buttonView.isChecked
                applySettings(this)
            }
        }
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun loadSettings() {
        settings = getSettings()

        b.tvThemeValue.text = getThemeText(settings.theme)
        b.tvLockTimeoutValue.text = "${settings.lockTimeout} Seconds"
        b.switchAllowScreenshots.isChecked = settings.allowScreenshot
    }
}