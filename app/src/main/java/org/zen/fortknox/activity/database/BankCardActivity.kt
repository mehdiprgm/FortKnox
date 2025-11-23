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
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.databinding.ActivityBankCardBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.validateData
import org.zen.fortknox.viewmodel.DatabaseViewModel
import org.zendev.keepergen.tools.formatting.CreditCardNumberFormattingTextWatcher
import org.zendev.keepergen.tools.formatting.DateFormattingTextWatcher

class BankCardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityBankCardBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private var bankCard: BankCard? = null
    private var isUpdatingEntity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityBankCardBinding.inflate(layoutInflater)
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
        when(view?.id) {
            R.id.btnClose -> {
                checkTheInformation()
            }

            R.id.btnFinish -> {/* No matter you want to create or update, all the inputs must be valid before database operation */
                if (isFormInformationValid()) {
                    if (isUpdatingEntity) {
                        updateBankCard()
                    } else {
                        createNewBankCard()
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
        val creditCardFormatter = CreditCardNumberFormattingTextWatcher()
        val dateFormatter = DateFormattingTextWatcher()

        b.txtCardNumber.addTextChangedListener(creditCardFormatter)
        b.txtExpireDate.addTextChangedListener(dateFormatter)
    }

    private fun isFormInformationChanged(): Boolean {/* For update scenario, it should check all the information and compare to original *//* If anything changed it means, it has to give warning, otherwise it's good and can exit *//* For create scenario, all it needs is to check all the field for inputs *//* If any information entered, it will give warning to the user */
        if (isUpdatingEntity) {
            val databaseText =
                "${bankCard!!.cardName},${bankCard!!.cardNumber},${bankCard!!.password},${bankCard!!.cvv2},${bankCard!!.expireDate}"

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
                            context = this@BankCardActivity,
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
        bankCard = intent.getParcelableExtra("BankCard", BankCard::class.java)
        isUpdatingEntity = bankCard != null

        if (bankCard != null) {
            b.tvTitle.text = "Update Bank Card Information"
            b.btnFinish.text = "Update information"

            b.txtName.setText(bankCard!!.cardName)
            b.txtCardNumber.setText(bankCard!!.cardNumber)
            b.txtPassword.setText(bankCard!!.password)
            b.txtCvv2.setText(bankCard!!.cvv2)
            b.txtExpireDate.setText(bankCard!!.expireDate)
        }
    }

    /* If the form gets the valid score, everything is correct */
    private fun isFormInformationValid(): Boolean {
        var score = 4

        score += b.txtLayName.validateData(
            b.txtName.text.toString().isEmpty(), "Card name is empty"
        )

        score += b.txtLayCardNumber.validateData(
            b.txtCardNumber.text.toString().isEmpty(), "Card number is empty"
        )

        score += b.txtLayCardNumber.validateData(
            b.txtCardNumber.text.toString().length != 19, "Card number is not valid"
        )

        score += b.txtLayPassword.validateData(
            b.txtPassword.text.toString().isEmpty(), "Password is empty"
        )

        score += b.txtLayCvv2.validateData(
            b.txtCvv2.text.toString().isEmpty(), "Cvv2 is empty"
        )

        score += b.txtLayCvv2.validateData(
            b.txtCvv2.text.toString().length < 3, "Cvv2 is not valid"
        )

        score += b.txtLayExpireDate.validateData(
            b.txtExpireDate.text.toString().isEmpty(), "Expire date is empty"
        )

        score += b.txtLayExpireDate.validateData(
            b.txtExpireDate.text.toString().length != 7, "Expire date is not valid"
        )

        return score == 4
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun createNewBankCard() {
        lifecycleScope.launch {
            try {
                if (isBankCardExists(b.txtName.text.toString())) {
                    Dialogs.showMessage(
                        this@BankCardActivity,
                        "Bank card exists",
                        "A bank card with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {
                    val newBankCard = BankCard(
                        cardName = b.txtName.text.toString(),
                        cardNumber = b.txtCardNumber.text.toString(),
                        password = b.txtPassword.text.toString(),
                        cvv2 = b.txtCvv2.text.toString(),
                        expireDate = b.txtExpireDate.text.toString(),
                        createDate = getDate()
                    )

                    databaseViewModel.addBankCard(newBankCard)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@BankCardActivity, ex)
            }
        }
    }

    private fun updateBankCard() {
        lifecycleScope.launch {
            try {
                val name = b.txtName.text.toString()

                if (isBankCardExists(name) && name != bankCard!!.cardName) {
                    Dialogs.showMessage(
                        this@BankCardActivity,
                        "Bank card exists",
                        "A bank card with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {/* Only update necessary information */
                    bankCard!!.cardName = b.txtName.text.toString()
                    bankCard!!.cardNumber = b.txtCardNumber.text.toString()
                    bankCard!!.password = b.txtPassword.text.toString()
                    bankCard!!.cvv2 = b.txtCvv2.text.toString()
                    bankCard!!.expireDate = b.txtExpireDate.text.toString()

                    databaseViewModel.updateBankCard(bankCard!!)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@BankCardActivity, ex)
            }
        }
    }

    private suspend fun isBankCardExists(cardName: String) : Boolean {
        return try {
            val bankCard = databaseViewModel.getBankCard(cardName)
            bankCard != null
        } catch (ex: Exception) {
            Dialogs.showException(this@BankCardActivity, ex)
            false
        }
    }
}