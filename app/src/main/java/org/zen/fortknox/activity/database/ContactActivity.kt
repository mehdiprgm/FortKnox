package org.zen.fortknox.activity.database

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.databinding.ActivityContactBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.formatting.PhoneNumberFormattingTextWatcher
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.validateData
import org.zen.fortknox.viewmodel.DatabaseViewModel
import org.zendev.keepergen.tools.formatting.CreditCardNumberFormattingTextWatcher
import org.zendev.keepergen.tools.formatting.DateFormattingTextWatcher

class ContactActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityContactBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private var contact: Contact? = null
    private var isUpdatingEntity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityContactBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        loadIntentData()

        setupBackPressListener()
        setupTextFormatters()

        b.btnClose.setOnClickListener(this)
        b.btnFinish.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnClose -> {
                checkTheInformation()
            }

            R.id.btnFinish -> {/* No matter you want to create or update, all the inputs must be valid before database operation */
                if (isFormInformationValid()) {
                    if (isUpdatingEntity) {
                        updateContact()
                    } else {
                        createNewContact()
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

    private fun setupTextFormatters() {
        b.txtPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun isFormInformationChanged(): Boolean {/* For update scenario, it should check all the information and compare to original *//* If anything changed it means, it has to give warning, otherwise it's good and can exit *//* For create scenario, all it needs is to check all the field for inputs *//* If any information entered, it will give warning to the user */
        if (isUpdatingEntity) {
            val databaseText =
                "${contact!!.name},${contact!!.phoneNumber},${contact!!.extraInformation}"

            return databaseText != getFormText()
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
                            context = this@ContactActivity,
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
        contact = intent.getParcelableExtra("Contact", Contact::class.java)
        isUpdatingEntity = contact != null

        if (contact != null) {
            b.tvTitle.text = "Update Contact Information"
            b.btnFinish.text = "Update information"

            b.txtName.setText(contact!!.name)
            b.txtPhoneNumber.setText(contact!!.phoneNumber)
            b.txtExtraInformation.setText(contact!!.extraInformation)
        }
    }

    /* If the form gets the valid score, everything is correct */
    private fun isFormInformationValid(): Boolean {
        var score = 2

        score += b.txtLayName.validateData(
            b.txtName.text.toString().isEmpty(), "Name is empty"
        )

        score += b.txtLayPhoneNumber.validateData(
            b.txtPhoneNumber.text.toString().isEmpty(), "Phone number is empty"
        )

        score += b.txtLayPhoneNumber.validateData(
            b.txtPhoneNumber.text.toString().length != 15, "Phone number is not valid"
        )

        return score == 2
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun createNewContact() {
        lifecycleScope.launch {
            try {
                if (isContactExists(b.txtName.text.toString())) {
                    Dialogs.showMessage(
                        this@ContactActivity,
                        "Contact exists",
                        "A contact with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {
                    val newContact = Contact(
                        name = b.txtName.text.toString(),
                        phoneNumber = b.txtPhoneNumber.text.toString(),
                        extraInformation = b.txtExtraInformation.text.toString(),
                        createDate = getDate()
                    )

                    databaseViewModel.addContact(newContact)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@ContactActivity, ex)
            }
        }
    }

    private fun updateContact() {
        lifecycleScope.launch {
            try {
                val name = b.txtName.text.toString()

                if (isContactExists(name) && name != contact!!.name) {
                    Dialogs.showMessage(
                        this@ContactActivity,
                        "Contact exists",
                        "A contact with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {/* Only update necessary information */
                    contact!!.name = b.txtName.text.toString()
                    contact!!.phoneNumber = b.txtPhoneNumber.text.toString()
                    contact!!.extraInformation = b.txtExtraInformation.text.toString()

                    databaseViewModel.updateContact(contact!!)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@ContactActivity, ex)
            }
        }
    }

    private suspend fun isContactExists(name: String): Boolean {
        return try {
            val contact = databaseViewModel.getContact(name)
            contact != null
        } catch (ex: Exception) {
            Dialogs.showException(this@ContactActivity, ex)
            false
        }
    }

}