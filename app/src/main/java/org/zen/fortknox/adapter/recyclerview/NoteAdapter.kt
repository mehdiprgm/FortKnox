package org.zen.fortknox.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.zen.fortknox.R
import org.zen.fortknox.adapter.interfaces.OnNoteClickListener
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.databinding.NoteLayoutBinding
import org.zen.fortknox.tools.getAllViews
import org.zen.fortknox.tools.getFirstLine
import org.zen.fortknox.tools.isMultiText
import org.zen.fortknox.tools.selectedItems

class NoteAdapter(private val context: Context) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var showCheckBoxes = false

    var notes = emptyList<Note>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var itemClickListener: OnNoteClickListener? = null
    fun setOnItemClickListener(listener: OnNoteClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): NoteViewHolder {
        val binding = NoteLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder, position: Int
    ) {
        val note = notes[position]
        var content = note.content
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 100

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 100

        getAllViews(b.layNote, false).forEach { view ->
            popInAnim.duration += 30
            view.animation = popInAnim
        }

        b.tvNoteName.text = note.name.replaceFirstChar { it.uppercase() }
        b.tvContent.text = note.content
        b.tvModifyDate.text = note.modifyDate

        /* Only get the first line */
        if (content.isMultiText()) {
            content = content.getFirstLine()
        }

        /* Only get first 50 characters */
        if (content.length > 50) {
            content = "${content.take(57)} ..."
        }

        b.chkSelected.isVisible = showCheckBoxes
        b.chkSelected.isChecked = selectedItems.contains(note)

        b.layNote.setOnClickListener {
            itemClickListener?.onItemClick(b.chkSelected, note)
        }

        b.layNote.setOnLongClickListener {
            itemClickListener?.onItemLongClick(b.chkSelected, note)
            true
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    class NoteViewHolder(val binding: NoteLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    fun setShowCheckboxes(show: Boolean) {
        showCheckBoxes = show
        notifyDataSetChanged()
    }
}