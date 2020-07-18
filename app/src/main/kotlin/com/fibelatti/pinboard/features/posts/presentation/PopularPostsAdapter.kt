package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.layout_popular_quick_actions.view.*
import kotlinx.android.synthetic.main.layout_quick_actions.view.buttonQuickActionShare
import kotlinx.android.synthetic.main.list_item_popular_post.view.*
import javax.inject.Inject

class PopularPostsAdapter @Inject constructor() :
    ListAdapter<Post, PopularPostsAdapter.ViewHolder>(DIFF_UTIL) {

    interface QuickActionsCallback {

        fun onShareClicked(item: Post)

        fun onSaveClicked(item: Post)
    }

    var onItemClicked: ((Post) -> Unit)? = null
    var quickActionsCallback: QuickActionsCallback? = null

    override fun getItemCount(): Int = currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.list_item_popular_post)
    ) {

        private var quickActionsVisible: Boolean = false

        fun bind(item: Post) = itemView.bindView(item)

        private fun View.bindView(item: Post) {

            textViewLinkTitle.text = item.title

            if (item.tags.isNullOrEmpty()) {
                chipGroupTags.gone()
            } else {
                layoutTags(item.tags)
            }

            hideQuickActions()

            setOnClickListener { onItemClicked?.invoke(item) }
            setOnLongClickListener {
                if (quickActionsCallback == null) {
                    return@setOnLongClickListener false
                }

                if (!quickActionsVisible) {
                    showQuickActions(item)
                } else {
                    hideQuickActions()
                }

                true
            }
        }

        private fun View.layoutTags(tags: List<Tag>) {
            chipGroupTags.visible()
            chipGroupTags.removeAllViews()
            for (tag in tags) {
                chipGroupTags.addValue(tag.name, showRemoveIcon = false)
            }
        }

        private fun View.showQuickActions(item: Post) {
            quickActionsVisible = true
            layoutQuickActions.visible()

            buttonQuickActionShare.setOnClickListener { quickActionsCallback?.onShareClicked(item) }
            buttonQuickActionSave.setOnClickListener { quickActionsCallback?.onSaveClicked(item) }
        }

        private fun View.hideQuickActions() {
            quickActionsVisible = false
            layoutQuickActions.gone()
        }
    }

    companion object {

        @JvmStatic
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<Post>() {

            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.url == newItem.url

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
        }
    }
}
