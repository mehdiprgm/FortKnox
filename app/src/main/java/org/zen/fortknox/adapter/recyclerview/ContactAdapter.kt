package org.zen.fortknox.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.zen.fortknox.R
import org.zen.fortknox.adapter.interfaces.OnBankCardClickListener
import org.zen.fortknox.adapter.interfaces.OnContactClickListener
import org.zen.fortknox.adapter.recyclerview.BankCardAdapter.BankCardViewHolder
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.databinding.BankcardLayoutBinding
import org.zen.fortknox.databinding.ContactLayoutBinding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.selectedItems

class ContactAdapter(private val context: Context) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private var showCheckBoxes = false

    var contacts = emptyList<Contact>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var itemClickListener: OnContactClickListener? = null
    fun setOnItemClickListener(listener: OnContactClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ContactViewHolder {
        val binding =
            ContactLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ContactViewHolder, position: Int
    ) {
        val contact = contacts[position]
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 100

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 100

        getAllViews(b.layContact, false).forEach {
            popInAnim.duration += 30
            it.animation = popInAnim
        }

        b.tvName.text = contact.name.replaceFirstChar { it.uppercase() }
        b.tvPhoneNumber.text = contact.phoneNumber
        b.tvExtraInformation.text = contact.extraInformation

        if (contact.extraInformation.isEmpty()) {
            b.tvExtraInformation.text = "Nothing to show"
        }

        b.chkSelected.isVisible = showCheckBoxes
        b.chkSelected.isChecked = selectedItems.contains(contact)

        b.layContact.setOnClickListener {
            itemClickListener?.onItemClick(b.chkSelected, contact)
        }

        b.layContact.setOnLongClickListener {
            itemClickListener?.onItemLongClick(b.chkSelected, contact)
            true
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    class ContactViewHolder(val binding: ContactLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setShowCheckboxes(show: Boolean) {
        showCheckBoxes = show
        notifyDataSetChanged()
    }
}