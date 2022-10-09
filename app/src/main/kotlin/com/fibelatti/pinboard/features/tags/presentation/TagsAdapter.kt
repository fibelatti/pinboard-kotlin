package com.fibelatti.pinboard.features.tags.presentation

import android.view.LayoutInflater
import com.fibelatti.core.android.recyclerview.BaseAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.ListItemTagBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class TagsAdapter @Inject constructor() : BaseAdapter<Tag, ListItemTagBinding>(
    binding = { parent -> ListItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false) },
    filter = { query, item -> item.name.startsWith(query, ignoreCase = true) },
) {

    var onItemClicked: ((Tag) -> Unit)? = null
    var onItemLongClicked: ((Tag) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder<ListItemTagBinding>, position: Int) {
        with(holder.binding) {
            val item = items[position]
            textViewTagName.text = item.name
            textViewPostCount.text = root.context.resources.getQuantityString(
                R.plurals.posts_quantity,
                item.posts,
                item.posts
            )

            root.setOnClickListener { onItemClicked?.invoke(item) }
            root.setOnLongClickListener {
                onItemLongClicked?.invoke(item)
                onItemLongClicked != null
            }
        }
    }
}
