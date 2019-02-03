package com.fibelatti.pinboard.features.tags.presentation

import android.view.View
import com.fibelatti.core.android.base.BaseAdapter
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.list_item_tag.view.*
import javax.inject.Inject

class TagsAdapter @Inject constructor() : BaseAdapter<Tag>(hasFilter = true) {

    var onItemClicked: ((Tag) -> Unit)? = null
    var onEmptyFilter: (() -> Unit)? = null

    override fun getLayoutRes(): Int = R.layout.list_item_tag

    override fun View.bindView(item: Tag, viewHolder: ViewHolder) {
        textViewTagName.text = item.name
        textViewPostCount.text =
            context.resources.getQuantityString(R.plurals.posts_quantity, item.posts, item.posts)

        setOnClickListener { onItemClicked?.invoke(item) }
    }

    override fun filterCriteria(query: String, item: Tag): Boolean = item.name !in query.split(",")

    override fun onEmptyFilterResult() {
        onEmptyFilter?.invoke()
    }
}
