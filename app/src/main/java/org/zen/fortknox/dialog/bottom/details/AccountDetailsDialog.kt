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
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.databinding.DialogAccountDetailsBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.copyTextToClipboard
import org.zen.fortknox.tools.shareText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class AccountDetailsDialog(private val context: Context, private var account: Account) :
    BottomSheetDialogFragment(), OnClickListener {

    private lateinit var b: DialogAccountDetailsBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = DialogAccountDetailsBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        setupViewModel()
        loadAccountInformation()

        b.layCopy.setOnClickListener(this)
        b.layShare.setOnClickListener(this)
        b.layDelete.setOnClickListener(this)
        b.layEdit.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layEdit -> {
                val intent = Intent(context, AccountActivity::class.java)
                intent.putExtra("Account", account)

                startActivity(intent)
                dismiss()
            }

            R.id.layCopy -> {
                copyTextToClipboard(context, "Account details", account.toString())
            }

            R.id.layShare -> {
                shareText(context, "Account details", account.toString())
            }

            R.id.layDelete -> {
                lifecycleScope.launch {
                    try {
                        if (Dialogs.ask(
                                context,
                                icon = R.drawable.ic_warning,
                                title = "Delete account",
                                message = "Are you sure you want to delete this account?",
                                cancellable = true,
                                yesText = "Delete",
                                noText = "Cancel"
                            )
                        ) {
                            databaseViewModel.deleteAccount(account)
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

    private fun loadAccountInformation() {
        b.tvAccountName.text = account.name
        when (account.accountType) {
            "Social media" -> {
                b.imgIcon.setImageResource(R.drawable.ic_telegram)
            }

            "Website" -> {
                b.imgIcon.setImageResource(R.drawable.ic_chrome)
            }

            "Email Address" -> {
                b.imgIcon.setImageResource(R.drawable.ic_google)
            }

            "Others" -> {
                b.imgIcon.setImageResource(R.drawable.ic_earth)
            }
        }

        b.tvUsername.text = account.username
        b.tvAccountType.text = account.accountType
        b.tvCreateDate.text = account.createDate
    }
}