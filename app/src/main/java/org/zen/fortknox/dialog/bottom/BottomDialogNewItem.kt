package org.zen.fortknox.dialog.bottom

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.database.AccountActivity
import org.zen.fortknox.activity.database.BankCardActivity
import org.zen.fortknox.activity.database.ContactActivity
import org.zen.fortknox.activity.database.NoteActivity
import org.zen.fortknox.databinding.DialogBottomNewItemBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.viewmodel.DatabaseViewModel

class BottomDialogNewItem() : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var b: DialogBottomNewItemBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = DialogBottomNewItemBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        b.layAccount.setOnClickListener(this)
        b.layBankCard.setOnClickListener(this)
        b.layContact.setOnClickListener(this)
        b.layNote.setOnClickListener(this)
        b.layPassword.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layAccount -> {
                startActivity(Intent(requireContext(), AccountActivity::class.java))
                dismiss()
            }

            R.id.layBankCard -> {
                startActivity(Intent(requireContext(), BankCardActivity::class.java))
                dismiss()
            }

            R.id.layContact -> {
                startActivity(Intent(requireContext(), ContactActivity::class.java))
                dismiss()
            }

            R.id.layNote -> {
                startActivity(Intent(requireContext(), NoteActivity::class.java))
                dismiss()
            }

            R.id.layPassword -> {
                Dialogs.generateNewPassword(requireContext())
                dismiss()
            }
        }
    }
}