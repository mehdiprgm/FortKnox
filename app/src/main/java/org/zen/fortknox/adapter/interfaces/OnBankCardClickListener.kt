package org.zen.fortknox.adapter.interfaces

import com.google.android.material.checkbox.MaterialCheckBox
import org.zen.fortknox.database.entity.BankCard

interface OnBankCardClickListener {
    fun onItemClick(checkBox: MaterialCheckBox, bankCard: BankCard)

    fun onItemLongClick(checkBox: MaterialCheckBox, bankCard: BankCard)
}