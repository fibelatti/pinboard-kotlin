package com.fibelatti.pinboard.features.notes.presentation

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.fibelatti.core.android.base.BaseListAdapter
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.android.synthetic.main.list_item_note.view.*
import javax.inject.Inject

class NoteListAdapter @Inject constructor() : BaseListAdapter<Note>(DIFF_UTIL) {

    var onNoteClicked: ((id: String) -> Unit)? = null

    override fun getLayoutRes(): Int = R.layout.list_item_note

    override fun View.bindView(item: Note, viewHolder: BaseListAdapter<Note>.ViewHolder) {
        textViewNoteTitle.text = item.title
        textViewNoteSavedDate.text = context.getString(R.string.notes_saved_at, item.createdAt)

        textViewNoteUpdatedDate.visibleIf(
            predicate = item.updatedAt != item.createdAt,
            text = context.getString(R.string.notes_updated_at, item.updatedAt),
            otherwiseVisibility = View.GONE
        )

        setOnClickListener { onNoteClicked?.invoke(item.id) }
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
