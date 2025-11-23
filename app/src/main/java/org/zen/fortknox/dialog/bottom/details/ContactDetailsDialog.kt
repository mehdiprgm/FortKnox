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
import org.zen.fortknox.activity.database.BankCardActivity
import org.zen.fortknox.activity.database.ContactActivity
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.databinding.DialogBankCardDetailsBinding
import org.zen.fortknox.databinding.DialogContactDetailsBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.copyTextToClipboard
import org.zen.fortknox.tools.shareText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class ContactDetailsDialog(private val context: Context, private var contact: Contact) :
    BottomSheetDialogFragment(), OnClickListener {

    private lateinit var b: DialogContactDetailsBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = DialogContactDetailsBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        setupViewModel()
        loadContactInformation()

        b.layCopy.setOnClickListener(this)
        b.layShare.setOnClickListener(this)
        b.layDelete.setOnClickListener(this)
        b.layEdit.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layEdit -> {
                val intent = Intent(context, ContactActivity::class.java)
                intent.putExtra("Contact", contact)

                startActivity(intent)
                dismiss()
            }

            R.id.layCopy -> {
                copyTextToClipboard(context, "Contact details", contact.toString())
            }

            R.id.layShare -> {
                shareText(context, "Contact details", contact.toString())
            }

            R.id.layDelete -> {
                lifecycleScope.launch {
                    try {
                        if (Dialogs.ask(
                                context,
                                icon = R.drawable.ic_warning,
                                title = "Delete contact",
                                message = "Are you sure you want to delete this contact?",
                                cancellable = true,
                                yesText = "Delete",
                                noText = "Cancel"
                            )
                        ) {
                            databaseViewModel.deleteContact(contact)
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

    private fun loadContactInformation() {
        b.tvName.text = contact.name
        b.tvPhoneNumber.text = contact.phoneNumber

        if (contact.extraInformation.isEmpty()) {
            b.tvExtraInformation.text = "Nothing to show"
        } else {
            b.tvExtraInformation.text = contact.extraInformation
        }

        b.tvCreateDate.text = contact.createDate
    }
}