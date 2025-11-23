package org.zen.fortknox.activity.database

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.databinding.ActivityNoteBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.pasteFromClipboard
import org.zen.fortknox.viewmodel.DatabaseViewModel

class NoteActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityNoteBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private var note: Note? = null
    private var isUpdatingEntity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        setupBackPressListener()
        loadIntentData()

        b.btnClose.setOnClickListener(this)
        b.btnMenu.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnClose -> {
                checkTheInformation()
            }

            R.id.btnMenu -> {
                showPopupMenu(view)
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_editor, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSelectAll -> {
                    b.txtContent.selectAll()
                }

                R.id.menuSave -> {
                    lifecycleScope.launch {
                        try {
                            if (isUpdatingEntity) {
                                updateNote(note!!.name)
                            } else {
                                /* New note name */
                                val noteName = Dialogs.textInput(
                                    context = this@NoteActivity,
                                    title = "File name",
                                    message = "Please enter note name",
                                    hint = "My notes",
                                    cancellable = true
                                )

                                if (noteName.isNotEmpty()) {
                                    createNewNote(noteName)
                                }
                            }
                        } catch (ex: Exception) {
                            Dialogs.showException(this@NoteActivity, ex)
                        }
                    }
                }

                R.id.menuPaste -> {
                    /* Get the current edittext cursor position */
                    val cursorPosition = b.txtContent.selectionStart
                    b.txtContent.text.insert(cursorPosition, pasteFromClipboard(this))
                }

                R.id.menuRename -> {
                    lifecycleScope.launch {
                        try {
                            val newName = Dialogs.textInput(
                                context = this@NoteActivity,
                                title = "New name",
                                message = "Please enter new note name",
                                hint = "My notes",
                                cancellable = true
                            )

                            if (newName.isNotEmpty()) {
                                updateNote(newName)
                            }
                        } catch (ex: Exception) {
                            Dialogs.showException(this@NoteActivity, ex)
                        }
                    }
                }
            }

            true
        }

        popup.show()
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
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
                "${note!!.name},${note!!.content}"

            return databaseText != b.txtContent.text.toString()
        } else {/* Append all text and check to see if form is empty or not */
            return b.txtContent.text.toString().isNotEmpty()
        }
    }

    /* Check the form information before exits the screen */
    private fun checkTheInformation() {
        if (getSettings().confirmExit) {
            if (isFormInformationChanged()) {
                lifecycleScope.launch {
                    if (Dialogs.ask(
                            context = this@NoteActivity,
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
        note = intent.getParcelableExtra("Note", Note::class.java)
        isUpdatingEntity = note != null

        if (note != null) {
            b.tvTitle.text = note!!.name
            b.txtContent.setText(note!!.content)
        }
    }

    private fun createNewNote(noteName: String) {
        lifecycleScope.launch {
            try {
                if (isNoteExists(noteName)) {
                    Dialogs.showMessage(
                        this@NoteActivity,
                        "Note exists",
                        "A note with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {
                    val newNote = Note(
                        name = noteName,
                        content = b.txtContent.text.toString(),
                        modifyDate = getDate(),
                        createDate = getDate()
                    )

                    databaseViewModel.addNote(newNote)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@NoteActivity, ex)
            }
        }
    }

    private fun updateNote(noteName: String) {
        lifecycleScope.launch {
            try {
                if (isNoteExists(noteName) && noteName != note!!.name) {
                    Dialogs.showMessage(
                        this@NoteActivity,
                        "Note exists",
                        "A note with this name already exists in the database.\nConsider using different name",
                        DialogType.Error
                    )
                } else {
                    /* Update note content */
                    note!!.name = noteName
                    note!!.content = b.txtContent.text.toString()
                    note!!.modifyDate = getDate()

                    databaseViewModel.updateNote(note!!)
                    finish()
                }
            } catch (ex: Exception) {
                Dialogs.showException(this@NoteActivity, ex)
            }
        }

        /* Update note content */
        note!!.content = b.txtContent.text.toString()
        note!!.modifyDate = getDate()

        databaseViewModel.updateNote(note!!)
    }

    private suspend fun isNoteExists(name: String): Boolean {
        return try {
            val note = databaseViewModel.getNote(name)
            note != null
        } catch (ex: Exception) {
            Dialogs.showException(this@NoteActivity, ex)
            false
        }
    }
}