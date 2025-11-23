package org.zen.fortknox.dialog.bottom.details

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.database.AccountActivity
import org.zen.fortknox.activity.database.BankCardActivity
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.databinding.DialogAccountDetailsBinding
import org.zen.fortknox.databinding.DialogBankCardDetailsBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.copyTextToClipboard
import org.zen.fortknox.tools.shareText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class BankCardDetailsDialog(private val context: Context, private var bankCard: BankCard) :
    BottomSheetDialogFragment(), OnClickListener {

    private lateinit var b: DialogBankCardDetailsBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = DialogBankCardDetailsBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        setupViewModel()
        loadBankCardInformation()

        b.layCopy.setOnClickListener(this)
        b.layShare.setOnClickListener(this)
        b.layDelete.setOnClickListener(this)
        b.layEdit.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layEdit -> {
                val intent = Intent(context, BankCardActivity::class.java)
                intent.putExtra("BankCard", bankCard)

                startActivity(intent)
                dismiss()
            }

            R.id.layCopy -> {
                copyTextToClipboard(context, "Bank card details", bankCard.toString())
            }

            R.id.layShare -> {
                shareText(context, "Bank card details", bankCard.toString())
            }

            R.id.layDelete -> {
                lifecycleScope.launch {
                    try {
                        if (Dialogs.ask(
                                context,
                                icon = R.drawable.ic_warning,
                                title = "Delete bank card",
                                message = "Are you sure you want to delete this bank card?",
                                cancellable = true,
                                yesText = "Delete",
                                noText = "Cancel"
                            )
                        ) {
                            databaseViewModel.deleteBankCard(bankCard)
                            dismiss()
                        }
                    } catch (ex: Exception) {
                        Dialogs.showException(context, ex)
                    }
                }
            }
        }
    }

    private fun setupViewModel() {/* used indexing instead of get method (get(ViewModel::class.java)) */
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun loadBankCardInformation() {
        b.tvCardName.text = bankCard.cardName
        b.tvCvv2.text = bankCard.cvv2
        b.tvExpireDate.text = bankCard.expireDate
        b.tvCreateDate.text = bankCard.createDate
    }
}