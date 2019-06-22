package com.fibelatti.pinboard.features.tags.presentation

import android.view.View
import com.fibelatti.core.android.base.BaseAdapter
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.list_item_tag.view.*
import javax.inject.Inject

class TagsAdapter @Inject constructor() : BaseAdapter<Tag>() {

    var onItemClicked: ((Tag) -> Unit)? = null

    override fun getLayoutRes(): Int = R.layout.list_item_tag

    override fun View.bindView(item: Tag, viewHolder: ViewHolder) {
        textViewTagName.text = item.name
        textViewPostCount.text =
            context.resources.getQuantityString(R.plurals.posts_quantity, item.posts, item.posts)

        setOnClickListener { onItemClicked?.invoke(item) }
    }
}
