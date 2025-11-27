package org.zen.fortknox.activity.database

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.databinding.ActivityAccountBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.setScreenshotStatus
import org.zen.fortknox.tools.validateData
import org.zen.fortknox.viewmodel.DatabaseViewModel

class AccountActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityAccountBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private var account: Account? = null
    private var isUpdatingEntity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        setupBackPressListener()
        loadIntentData()

        b.layAccountType.setOnClickListener(this)
        b.btnClose.setOnClickListener(this)
        b.btnFinish.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        /* Allow taking screenshots */
        setScreenshotStatus(getSettings().allowScreenshot)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layAccountType -> {
                lifecycleScope.launch {
                    val result = Dialogs.selectAccountType(
                        this@AccountActivity, b.tvAccountType.text.toString()
                    )

                    if (result != null) {
                        b.tvAccountType.text = result
                    }
                }
            }

            R.id.btnClose -> {
                checkTheInformation()
            }

            R.id.btnFinish -> {/* No matter you want to create or update, all the inputs must be valid before database operation */
                if (isFormInformationValid()) {
                    if (isUpdatingEntity) {
                        updateAccount()
                    } else {
                        createNewAccount()
                    }
                }
            }
        }
    }

    private fun setupBackPressListener() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {/* Have to check the information before exit */
                checkTheInformation()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun isFormInformationChanged(): Boolean {/* For update scenario, it should check all the information and compare to original *//* If anything changed it means, it has to give warning, otherwise it's good and can exit *//* For create scenario, all it needs is to check all the field for inputs *//* If any information entered, it will give warning to the user */
        if (isUpdatingEntity) {
            val databaseText =
                "${account!!.name},${account!!.username},${account!!.password},${account!!.extraInformation},${account!!.accountType}"

            /* The last one is textview, so i manually add it to the form text */
            val formText = "${getFormText()},${b.tvAccountType.text}"

            return databaseText != formText
        } else {/* Append all text and check to see if form is empty or not */
            return getFormText().isNotEmpty()
        }
    }

    /* Iterate over all edittext in the form and get all their text separated by ,*/
    private fun getFormText(): String {
        return getAllViews(b.layInformation, false).filterIsInstance<TextInputEditText>()
            .filter { it.text.toString().isNotEmpty() }
            .joinToString(separator = ",") { it.text.toString() }
    }

    /* Check the form information before exits the screen */
    private fun checkTheInformation() {
        if (getSettings().confirmExit) {
            if (isFormInformationChanged()) {
                lifecycleScope.launch {
                    if (Dialogs.ask(
                            context = this@AccountActivity,
                            icon = R.drawable.ic_warning,
                            "Discard changes",
                            "You changed some of the information.\nAre you sure you want to exit?",
                            yesText = "Yes",
                            noText = "No",
                            cancellable = false
                        )
                    ) {
                        finish()
                    }
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun loadIntentData() {/* Get the account object *//* Null means new entity */
        account = intent.getParcelableExtra("Account", Account::class.java)
        isUpdatingEntity = account != null

        if (account != null) {
            b.tvTitle.text = "Update Account Information"
            b.btnFinish.text = "Update information"

            b.txtName.setText(account!!.name)
            b.txtUsername.setText(account!!.username)
            b.txtPassword.setText(account!!.password)
            b.txtExtraInformation.setText(account!!.extraInformation)
            b.tvAccountType.setText(account!!.accountType)
        }
    }

    /* If the form gets the valid score, everything is correct */
    private fun isFormInformationValid(): Boolean {
        var score = 3

        score += b.txtLayName.validateData(
            b.txtName.text.toString().isEmpty(), "Name is empty"
        )

        score += b.txtLayUsername.validateData(
            b.txtUsername.text.toString().isEmpty(), "Username is empty"
        )

        score += b.txtLayPassword.validateData(
            b.txtPassword.text.toString().isEmpty(), "Password is empty"
        )

        return score == 3
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun createNewAccount() {
        lifecycleScope.launch {
            try {
                if (isAccountExists(b.txtName.text.toString())) {
                    Dialogs.showMessage(
                        this@AccountActivity,
                        "Account exists",
                        "An account with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {
                    val newAccount = Account(
                        name = b.txtName.text.toString(),
                        username = b.txtUsername.text.toString(),
                        password = b.txtPassword.text.toString(),
                        accountType = b.tvAccountType.text.toString(),
                        extraInformation = b.txtExtraInformation.text.toString(),
                        createDate = getDate()
                    )

                    databaseViewModel.addAccount(newAccount)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@AccountActivity, ex)
            }
        }
    }

    private fun updateAccount() {
        lifecycleScope.launch {
            try {
                val name = b.txtName.text.toString()

                if (isAccountExists(name) && name != account!!.name) {
                    Dialogs.showMessage(
                        this@AccountActivity,
                        "Account exists",
                        "An account with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {/* Only update necessary information */
                    account!!.name = b.txtName.text.toString()
                    account!!.username = b.txtUsername.text.toString()
                    account!!.password = b.txtPassword.text.toString()
                    account!!.extraInformation = b.txtExtraInformation.text.toString()
                    account!!.accountType = b.tvAccountType.text.toString()

                    databaseViewModel.updateAccount(account!!)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@AccountActivity, ex)
            }
        }
    }

    private suspend fun isAccountExists(name: String): Boolean {
        return try {
            val account = databaseViewModel.getAccount(name)
            account != null
        } catch (ex: Exception) {
            Dialogs.showException(this@AccountActivity, ex)
            false
        }
    }
}