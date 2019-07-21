package com.fibelatti.pinboard.features.notes.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.android.synthetic.main.list_item_note.view.*
import javax.inject.Inject

class NoteListAdapter @Inject constructor() :
    ListAdapter<Note, NoteListAdapter.ViewHolder>(DIFF_UTIL) {

    var onNoteClicked: ((id: String) -> Unit)? = null

    override fun getItemCount(): Int = currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun View.bindView(item: Note) {
        textViewNoteTitle.text = item.title
        textViewNoteSavedDate.text = context.getString(R.string.notes_saved_at, item.createdAt)

        textViewNoteUpdatedDate.visibleIf(
            predicate = item.updatedAt != item.createdAt,
            text = context.getString(R.string.notes_updated_at, item.updatedAt),
            otherwiseVisibility = View.GONE
        )

        setOnClickListener { onNoteClicked?.invoke(item.id) }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.list_item_note)
    ) {
        fun bind(item: Note) = itemView.bindView(item)
    }

    companion object {

        @JvmStatic
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem == newItem
        }
    }
}
