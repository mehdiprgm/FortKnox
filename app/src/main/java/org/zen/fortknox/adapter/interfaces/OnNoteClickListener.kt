package org.zen.fortknox.adapter.interfaces

import com.google.android.material.checkbox.MaterialCheckBox
import org.zen.fortknox.database.entity.Note

interface OnNoteClickListener {
    fun onItemClick(checkBox: MaterialCheckBox, note: Note)

    fun onItemLongClick(checkBox: MaterialCheckBox, note: Note)
}