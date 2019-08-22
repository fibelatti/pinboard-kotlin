package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import android.view.ViewGroup
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

private const val MAX_TAGS_PER_ITEM = 3

class PostListAdapter @Inject constructor(
    private val dateFormatter: DateFormatter
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    private val items: MutableList<Post> = mutableListOf()
    var showDescription: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemClicked: ((Post) -> Unit)? = null
    var onTagClicked: ((Tag) -> Unit)? = null

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun View.bindView(item: Post) {
        textViewPrivate.visibleIf(item.private, otherwiseVisibility = View.GONE)
        textViewReadLater.visibleIf(item.readLater, otherwiseVisibility = View.GONE)

        textViewLinkTitle.text = item.title
        textViewLinkAddedDate.text = context.getString(
            R.string.posts_saved_on,
            dateFormatter.tzFormatToDisplayFormat(item.time)
        )

        textViewDescription.text = item.description
        textViewDescription.visibleIf(
            showDescription && item.description.isNotBlank(),
            otherwiseVisibility = View.GONE
        )

        when {
            item.tags.isNullOrEmpty() -> {
                chipGroupTags.gone()
                textViewOtherTagsAvailable.gone()
            }
            item.tags.size <= MAX_TAGS_PER_ITEM -> {
                layoutTags(item.tags)
                textViewOtherTagsAvailable.gone()
            }
            else -> {
                val otherAmount = item.tags.size - MAX_TAGS_PER_ITEM

                layoutTags(item.tags.take(MAX_TAGS_PER_ITEM))
                textViewOtherTagsAvailable.visible(
                    resources.getQuantityString(R.plurals.posts_tags_more, otherAmount, otherAmount)
                )
            }
        }

        setOnClickListener { onItemClicked?.invoke(item) }
        chipGroupTags.onTagChipClicked = onTagClicked
    }

    private fun View.layoutTags(tags: List<Tag>) {
        chipGroupTags.visible()
        chipGroupTags.removeAllViews()
        for (tag in tags) {
            chipGroupTags.addValue(tag.name, showRemoveIcon = false)
        }
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
