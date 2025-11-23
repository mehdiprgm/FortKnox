package org.zen.fortknox.adapter.interfaces

import com.google.android.material.checkbox.MaterialCheckBox
import org.zen.fortknox.database.entity.Contact

interface OnContactClickListener {
    fun onItemClick(checkBox: MaterialCheckBox, contact: Contact)

    fun onItemLongClick(checkBox: MaterialCheckBox, contact: Contact)
}