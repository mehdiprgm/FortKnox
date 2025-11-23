package org.zen.fortknox.adapter.interfaces

import com.google.android.material.checkbox.MaterialCheckBox
import org.zen.fortknox.database.entity.Account

interface OnAccountClickListener {
    fun onItemClick(checkBox: MaterialCheckBox, account: Account)

    fun onItemLongClick(checkBox: MaterialCheckBox, account: Account)
}