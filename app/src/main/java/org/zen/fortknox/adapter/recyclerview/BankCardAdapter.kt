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
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.databinding.BankcardLayoutBinding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.selectedItems

class BankCardAdapter(private val context: Context) :
    RecyclerView.Adapter<BankCardAdapter.BankCardViewHolder>() {
    private var showCheckBoxes = false

    var bankCards = emptyList<BankCard>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var itemClickListener: OnBankCardClickListener? = null
    fun setOnItemClickListener(listener: OnBankCardClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BankCardViewHolder {
        val binding =
            BankcardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankCardViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BankCardViewHolder, position: Int
    ) {
        val bankCard = bankCards[position]
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 100

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 100

        b.cardBankCard.animation = slideDownAnim

        getAllViews(b.layBankCard, false).forEach { view ->
            popInAnim.duration += 30
            view.animation = popInAnim
        }

        b.tvCardName.text = bankCard.cardName.replaceFirstChar { it.uppercase() }
        b.tvCardNumber.text = bankCard.cardNumber
        b.tvExpireDate.text = bankCard.expireDate

        b.chkSelected.isVisible = showCheckBoxes
        b.chkSelected.isChecked = selectedItems.contains(bankCard)

        b.layBankCard.setOnClickListener {
            itemClickListener?.onItemClick(b.chkSelected, bankCard)
        }

        b.layBankCard.setOnLongClickListener {
            itemClickListener?.onItemLongClick(b.chkSelected, bankCard)
            true
        }
    }

    override fun getItemCount(): Int {
        return bankCards.size
    }

    class BankCardViewHolder(val binding: BankcardLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setShowCheckboxes(show: Boolean) {
        showCheckBoxes = show
        notifyDataSetChanged()
    }
}