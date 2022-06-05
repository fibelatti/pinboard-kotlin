package com.fibelatti.pinboard.features.notes.presentation

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.fibelatti.core.android.recyclerview.BaseListAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.ListItemNoteBinding
import com.fibelatti.pinboard.features.notes.domain.model.Note
import javax.inject.Inject

class NoteListAdapter @Inject constructor() : BaseListAdapter<Note, ListItemNoteBinding>(
    binding = { parent -> ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false) },
    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
) {

    var onNoteClicked: ((id: String) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder<ListItemNoteBinding>, position: Int) {
        with(holder.binding) {
            val item = getItem(position)

            textViewNoteTitle.text = item.title
            textViewNoteSavedDate.text = root.context.getString(R.string.notes_saved_at, item.createdAt)
            textViewNoteUpdatedDate.isVisible = item.updatedAt != item.createdAt
            textViewNoteUpdatedDate.text = root.context.getString(R.string.notes_updated_at, item.updatedAt)

            root.setOnClickListener { onNoteClicked?.invoke(item.id) }
        }
    }
}
