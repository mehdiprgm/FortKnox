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
import org.zen.fortknox.activity.database.NoteActivity
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.databinding.DialogBankCardDetailsBinding
import org.zen.fortknox.databinding.DialogContactDetailsBinding
import org.zen.fortknox.databinding.DialogNoteDetailsBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.copyTextToClipboard
import org.zen.fortknox.tools.getFirstLine
import org.zen.fortknox.tools.isMultiText
import org.zen.fortknox.tools.shareText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class NoteDetailsDialog(private val context: Context, private var note: Note) :
    BottomSheetDialogFragment(), OnClickListener {

    private lateinit var b: DialogNoteDetailsBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = DialogNoteDetailsBinding.inflate(layoutInflater)
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
                val intent = Intent(context, NoteActivity::class.java)
                intent.putExtra("Note", note)

                startActivity(intent)
                dismiss()
            }

            R.id.layCopy -> {
                copyTextToClipboard(context, "Note details", note.toString())
            }

            R.id.layShare -> {
                shareText(context, "Note details", note.toString())
            }

            R.id.layDelete -> {
                lifecycleScope.launch {
                    try {
                        if (Dialogs.ask(
                                context,
                                icon = R.drawable.ic_warning,
                                title = "Delete note",
                                message = "Are you sure you want to delete this note?",
                                cancellable = true,
                                yesText = "Delete",
                                noText = "Cancel"
                            )
                        ) {
                            databaseViewModel.deleteNote(note)
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
        var content = note.content

        b.tvName.text = note.name
        b.tvCreateDate.text = note.createDate

        /* Only get the first line */
        if (content.isMultiText()) {
            content = content.getFirstLine()
        }

        /* Only get first 50 characters */
        if (content.length > 50) {
            content = "${content.take(57)} ..."
        }

        b.tvContent.text = content
    }
}