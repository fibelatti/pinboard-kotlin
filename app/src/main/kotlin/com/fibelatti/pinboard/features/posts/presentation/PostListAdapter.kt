package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import android.widget.TextView
import com.fibelatti.core.android.base.BaseAdapter
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.list_item_post.view.*
import javax.inject.Inject

private const val MAX_TAGS_PER_ITEM = 4

class PostListAdapter @Inject constructor(
    private val dateFormatter: DateFormatter
) : BaseAdapter<Post>() {

    var onItemClicked: ((Post) -> Unit)? = null

    override fun getLayoutRes(): Int = R.layout.list_item_post

    override fun View.bindView(item: Post, viewHolder: ViewHolder) {
        textViewPrivate.visibleIf(item.private, otherwiseVisibility = View.GONE)
        textViewReadLater.visibleIf(item.readLater, otherwiseVisibility = View.GONE)

        textViewLinkTitle.text = item.title
        textViewLinkAddedDate.text = context.getString(R.string.posts_saved_on, dateFormatter.tzFormatToDisplayFormat(item.time))

        when {
            item.tags.isNullOrEmpty() -> layoutTags.gone()
            item.tags.size <= MAX_TAGS_PER_ITEM -> layoutTags(item.tags)
            else -> {
                val otherAmount = item.tags.size - MAX_TAGS_PER_ITEM

                layoutTags(item.tags.take(MAX_TAGS_PER_ITEM))
                addTagView(resources.getQuantityString(R.plurals.posts_tags_more, otherAmount, otherAmount))
            }
        }

        setOnClickListener { onItemClicked?.invoke(item) }
    }

    private fun View.layoutTags(tags: List<Tag>) {
        layoutTags.visible()
        layoutTags.removeAllViews()
        for (tag in tags) {
            addTagView(tag.name)
        }
    }

    private fun View.addTagView(value: String) {
        layoutTags.addView(
            TextView(context, null, 0, R.style.AppTheme_Text_Tag)
                .apply { text = value }
        )
    }
}
