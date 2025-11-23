package org.zen.fortknox.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.zen.fortknox.R
import org.zen.fortknox.adapter.interfaces.OnAccountClickListener
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.databinding.AccountLayoutBinding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.resizeTextViewDrawable
import org.zen.fortknox.tools.selectedItems

class AccountAdapter(private val context: Context) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var showCheckBoxes = false

    var accounts = emptyList<Account>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var itemClickListener: OnAccountClickListener? = null
    fun setOnItemClickListener(listener: OnAccountClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AccountViewHolder {
        val binding =
            AccountLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AccountViewHolder, position: Int
    ) {
        val account = accounts[position]
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 100

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 100
        b.cardAccount.animation = slideDownAnim

        getAllViews(b.layAccount, false).forEach { view ->
            popInAnim.duration += 30
            view.animation = popInAnim
        }

        when (account.accountType) {
            "Social Media" -> {
                resizeTextViewDrawable(context, b.tvAccountName, R.drawable.ic_telegram, 25)
            }

            "Website" -> {
                resizeTextViewDrawable(context, b.tvAccountName, R.drawable.ic_chrome, 25)
            }

            "Email Address" -> {
                resizeTextViewDrawable(context, b.tvAccountName, R.drawable.ic_google, 25)
            }

            "Others" -> {
                resizeTextViewDrawable(context, b.tvAccountName, R.drawable.ic_earth, 25)
            }
        }

        b.tvAccountName.text = account.name.replaceFirstChar { it.uppercase() }
        b.tvUsername.text = account.username
        b.tvAccountType.text = account.accountType

        b.chkSelected.isVisible = showCheckBoxes
        b.chkSelected.isChecked = selectedItems.contains(account)

        b.layAccount.setOnClickListener {
            itemClickListener?.onItemClick(b.chkSelected, account)
        }

        b.layAccount.setOnLongClickListener {
            itemClickListener?.onItemLongClick(b.chkSelected, account)
            true
        }
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    class AccountViewHolder(val binding: AccountLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setShowCheckboxes(show: Boolean) {
        showCheckBoxes = show
        notifyDataSetChanged()
    }
}