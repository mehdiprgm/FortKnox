package org.zen.fortknox.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.zen.fortknox.R
import org.zen.fortknox.tools.startDialogAnimation
import org.zen.fortknox.tools.theme.Theme
import kotlin.coroutines.resume

class Dialogs {
    companion object {

        @JvmStatic
        private fun createDialog(
            context: Context, layoutFile: Int, cancelable: Boolean = false
        ): Dialog {
            val dialog = Dialog(context)
            dialog.setContentView(layoutFile)

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

            dialog.window?.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    context, R.drawable.dialog_background
                )
            )

            dialog.setCancelable(cancelable)
            return dialog
        }

        @JvmStatic
        suspend fun ask(
            context: Context,
            icon: Int,
            title: String,
            message: String,
            yesText: String = "Yes",
            noText: String = "No",
            cancellable: Boolean = false
        ): Boolean = suspendCancellableCoroutine { continuation ->
            val dialog = createDialog(
                context, R.layout.dialog_ask, cancellable
            )

            startDialogAnimation(dialog.findViewById(R.id.main))

            val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

            val btnYes = dialog.findViewById<MaterialButton>(R.id.btnYes)
            val btnNo = dialog.findViewById<MaterialButton>(R.id.btnNo)

            btnYes.text = yesText
            btnNo.text = noText

            tvTitle.text = title
            tvMessage.text = message

            dialog.findViewById<ImageView>(R.id.imgIcon).setImageDrawable(
                ContextCompat.getDrawable(context, icon)
            )

            btnYes.setOnClickListener {
                continuation.resume(true)
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
                continuation.resume(false)
                dialog.dismiss()
            }

            dialog.setOnCancelListener {
                continuation.resume(false)
            }

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }

        @JvmStatic
        suspend fun textInput(
            context: Context,
            title: String,
            message: String,
            hint: String,
            defaultText: String = "",
            isPassword: Boolean = false,
            isNumber: Boolean = false,
            cancellable: Boolean = false
        ): String = suspendCancellableCoroutine { continuation ->
            val dialog = createDialog(
                context,
                R.layout.dialog_text_input
            )
            dialog.setCancelable(cancellable)
            startDialogAnimation(dialog.findViewById(R.id.main))

            val txtInput = dialog.findViewById<EditText>(R.id.txtInput)

            dialog.findViewById<TextView>(R.id.tvTitle).text = title
            dialog.findViewById<TextView>(R.id.tvMessage).text = message

            txtInput.hint = hint
            txtInput.setText(defaultText)

            if (isPassword) {
                if (isNumber) {
                    txtInput.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                } else {
                    txtInput.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                txtInput.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                if (isNumber) {
                    txtInput.inputType = InputType.TYPE_CLASS_NUMBER
                } else {
                    txtInput.inputType = InputType.TYPE_CLASS_TEXT
                }
            }

            dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                val text = txtInput.text.toString()
                continuation.resume(text)
                dialog.dismiss()
            }

            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                continuation.resume("")
                dialog.dismiss()
            }

            dialog.setOnCancelListener {
                continuation.resume("")
            }

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }

        @JvmStatic
        suspend fun showMessage(
            context: Context,
            title: String,
            message: String,
            dialogType: DialogType
        ): Unit = suspendCancellableCoroutine { continuation ->
            val dialog = createDialog(context, R.layout.dialog_show_messsage)
            startDialogAnimation(dialog.findViewById(R.id.main))

            val imgIcon = dialog.findViewById<ImageView>(R.id.imgIcon)

            val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

            tvTitle.text = title
            tvMessage.text = message

            when (dialogType) {
                DialogType.Error -> {
                    imgIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context, R.drawable.ic_error
                        )
                    )
                }

                DialogType.Warning -> {
                    imgIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context, R.drawable.ic_warning
                        )
                    )
                }

                DialogType.Information -> {
                    imgIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_info
                        )
                    )
                }
            }

            dialog.findViewById<MaterialButton>(R.id.btnOk).setOnClickListener {
                continuation.resume(Unit)
                dialog.dismiss()
            }

            dialog.show()
        }

        @JvmStatic
        suspend fun showException(context: Context, exception: Exception): Unit =
            withContext(Dispatchers.Main) {
                val message = "${exception.message}\n${exception.stackTraceToString()}"
                showMessage(context, "Error", message, DialogType.Error)
            }

        @JvmStatic
        suspend fun showNoInternetConnection(context: Context): Unit =
            withContext(Dispatchers.Main) {
                showMessage(
                    context,
                    "No connection",
                    "Device is not connected to the internet.\nUse wifi or mobile data and try again",
                    DialogType.Error
                )
            }

        @JvmStatic
        fun load(context: Context, title: String, message: String): Dialog {
            val dialog = createDialog(context, R.layout.dialog_load, false)
            startDialogAnimation(dialog.findViewById(R.id.main))

            dialog.findViewById<TextView>(R.id.tvTitle).text = title
            dialog.findViewById<TextView>(R.id.tvMessage).text = message

            return dialog
        }

        @SuppressLint("SetTextI18n")
        @JvmStatic
        suspend fun showNewProfileCreated(context: Context, username: String): Unit =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(context, R.layout.dialog_show_new_profile_created, false)

                val tvDescription = dialog.findViewById<TextView>(R.id.tvDescription)
                val btnOk = dialog.findViewById<MaterialButton>(R.id.btnOk)

                tvDescription.text = """
                    Welcome to FortKnox, $username
                    You can now use application to save your information in vault
                """.trimIndent()

                btnOk.setOnClickListener {
                    continuation.resume(Unit)
                    dialog.dismiss()
                }

                dialog.show()
            }

        @JvmStatic
        suspend fun selectAccountType(context: Context, defaultValue: String): String? =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(context, R.layout.dialog_select_account_type, true)
                startDialogAnimation(dialog.findViewById(R.id.main))

                var accountType: String? = null

                val btnOk = dialog.findViewById<MaterialButton>(R.id.btnOk)
                val buttons = listOf<RadioButton>(
                    dialog.findViewById(R.id.btnSocialMedia),
                    dialog.findViewById(R.id.btnWebsite),
                    dialog.findViewById(R.id.btnEmailAddress),
                    dialog.findViewById(R.id.btnOthers)
                )

                when (defaultValue) {
                    "Social Media" -> {
                        buttons[0].isChecked = true
                    }

                    "Website" -> {
                        buttons[1].isChecked = true
                    }

                    "Email Address" -> {
                        buttons[2].isChecked = true
                    }

                    "Others" -> {
                        buttons[3].isChecked = true
                    }
                }

                /* Write same click listener for all buttons */
                val clickListener = View.OnClickListener { view ->
                    when (view.id) {
                        R.id.btnSocialMedia,
                        R.id.btnWebsite,
                        R.id.btnEmailAddress,
                        R.id.btnOthers -> {
                            accountType = (view as RadioButton).text.toString()
                        }
                    }
                }

                /* Apply click listener to all buttons */
                buttons.forEach { button ->
                    button.setOnClickListener(clickListener)
                }

                btnOk.setOnClickListener {
                    continuation.resume(accountType)
                    dialog.dismiss()
                }

                dialog.show()
            }

        @JvmStatic
        suspend fun selectTheme(context: Context, theme: Theme): Theme? =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(context, R.layout.dialog_select_theme, true)
                startDialogAnimation(dialog.findViewById(R.id.main))

                var selectedTheme : Theme? = null

                val btnOk = dialog.findViewById<MaterialButton>(R.id.btnOk)

                val btnFollowSystem = dialog.findViewById<RadioButton>(R.id.btnFollowSystem)
                val btnDarkMode = dialog.findViewById<RadioButton>(R.id.btnDarkMode)
                val btnLightMode = dialog.findViewById<RadioButton>(R.id.btnLightMode)

                when(theme) {
                    Theme.System -> {
                        btnFollowSystem.isChecked = true
                    }

                    Theme.Dark -> {
                        btnDarkMode.isChecked = true
                    }

                    Theme.Light -> {
                        btnLightMode.isChecked = true
                    }
                }

                btnFollowSystem.setOnClickListener {
                    selectedTheme = Theme.System
                }

                btnDarkMode.setOnClickListener {
                    selectedTheme = Theme.Dark
                }

                btnLightMode.setOnClickListener {
                    selectedTheme = Theme.Light
                }

                btnOk.setOnClickListener {
                    continuation.resume(selectedTheme)
                    dialog.dismiss()
                }

                dialog.show()
            }

        @JvmStatic
        suspend fun selectLockTimeout(context: Context, defaultTimeOut: Int): Int? =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(context, R.layout.dialog_select_lock_timeout, true)
                startDialogAnimation(dialog.findViewById(R.id.main))

                var lockTimeout: Int? = null

                val btnOk = dialog.findViewById<MaterialButton>(R.id.btnOk)

                val btn5Seconds = dialog.findViewById<RadioButton>(R.id.btn5Seconds)
                val btn10Seconds = dialog.findViewById<RadioButton>(R.id.btn10Seconds)
                val btn15Seconds = dialog.findViewById<RadioButton>(R.id.btn15Seconds)
                val btn30Seconds = dialog.findViewById<RadioButton>(R.id.btn30Seconds)

                when(defaultTimeOut) {
                    5 -> {
                        btn5Seconds.isChecked = true
                    }

                    10 -> {
                        btn10Seconds.isChecked = true
                    }

                    15 -> {
                        btn15Seconds.isChecked = true
                    }

                    30 -> {
                        btn30Seconds.isChecked = true
                    }
                }

                btn5Seconds.setOnClickListener {
                    lockTimeout = 5
                }

                btn10Seconds.setOnClickListener {
                    lockTimeout = 10
                }

                btn15Seconds.setOnClickListener {
                    lockTimeout = 15
                }

                btn30Seconds.setOnClickListener {
                    lockTimeout = 30
                }

                btnOk.setOnClickListener {
                    continuation.resume(lockTimeout)
                    dialog.dismiss()
                }

                dialog.show()
            }


        @JvmStatic
        fun showAboutUs(context: Context) {
            val dialog = createDialog(context, R.layout.dialog_about_us, true)
            startDialogAnimation(dialog.findViewById(R.id.main))

            val imgGmail = dialog.findViewById<ImageView>(R.id.imgGmail)
            val imgTelegram = dialog.findViewById<ImageView>(R.id.imgTelegram)
            val imgInstagram = dialog.findViewById<ImageView>(R.id.imgInstagram)
            val imgGithub = dialog.findViewById<ImageView>(R.id.imgGithub)

            imgGmail.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:mfcrisis2016@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "")
                }

                /* Optional: restrict to Gmail app if installed */
                intent.setPackage("com.google.android.gm")

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }

            imgTelegram.setOnClickListener {
                val telegramIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://t.me/zenDEv2".toUri()/* optional: limit to Telegram app only */
                    setPackage("org.telegram.messenger")
                }

                if (telegramIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(telegramIntent)
                } else {/* fallback: open in browser if Telegram is not installed */
                    val browserIntent = Intent(Intent.ACTION_VIEW, "https://t.me/zenDEv2".toUri())
                    context.startActivity(browserIntent)
                }
            }

            imgInstagram.setOnClickListener {
                val uri = "http://instagram.com/_u/mehdi.la.79".toUri()
                val instagramIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.instagram.android")
                }

                if (instagramIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(instagramIntent)
                } else {/* fallback to browser if Instagram app isn't installed */
                    val webIntent = Intent(
                        Intent.ACTION_VIEW, "http://instagram.com/mehdi.la.79".toUri()
                    )

                    context.startActivity(webIntent)
                }
            }

            imgGithub.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/mehdiprgm".toUri())
                context.startActivity(intent)
            }

            dialog.show()
        }
    }
}