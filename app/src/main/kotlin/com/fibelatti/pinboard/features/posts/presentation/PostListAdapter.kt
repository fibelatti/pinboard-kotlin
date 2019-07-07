package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inflate
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
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    private val items: MutableList<Post> = mutableListOf()

    var onItemClicked: ((Post) -> Unit)? = null

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun View.bindView(item: Post) {
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

    fun addAll(newItems: List<Post>, diffResult: DiffUtil.DiffResult) {
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.list_item_post)
    ) {
        fun bind(item: Post) = itemView.bindView(item)
    }
}
