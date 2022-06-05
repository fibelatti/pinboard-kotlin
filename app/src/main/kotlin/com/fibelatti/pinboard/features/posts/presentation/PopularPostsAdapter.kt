package com.fibelatti.pinboard.features.posts.presentation

import android.view.LayoutInflater
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.fibelatti.core.android.recyclerview.BaseListAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.databinding.ListItemPopularPostBinding
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class PopularPostsAdapter @Inject constructor() : BaseListAdapter<Post, ListItemPopularPostBinding>(
    binding = { parent -> ListItemPopularPostBinding.inflate(LayoutInflater.from(parent.context), parent, false) },
    itemsTheSame = { oldItem, newItem -> oldItem.url == newItem.url },
) {

    interface QuickActionsCallback {

        fun onShareClicked(item: Post)

        fun onSaveClicked(item: Post)
    }

    var onItemClicked: ((Post) -> Unit)? = null
    var quickActionsCallback: QuickActionsCallback? = null

    private val quickActionsVisible: MutableMap<Int, Boolean> = mutableMapOf()

    override fun onBindViewHolder(holder: ViewHolder<ListItemPopularPostBinding>, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.textViewLinkTitle.text = item.title

        if (item.tags.isNullOrEmpty()) {
            binding.chipGroupTags.isGone = true
        } else {
            binding.layoutTags(item.tags)
        }

        binding.hideQuickActions(position)

        binding.root.setOnClickListener { onItemClicked?.invoke(item) }
        binding.root.setOnLongClickListener {
            if (quickActionsCallback == null) {
                return@setOnLongClickListener false
            }

            if (quickActionsVisible[position] == true) {
                binding.hideQuickActions(position)
            } else {
                binding.showQuickActions(item, position)
            }

            true
        }
    }

    private fun ListItemPopularPostBinding.layoutTags(tags: List<Tag>) {
        chipGroupTags.isVisible = true
        chipGroupTags.removeAllViews()
        for (tag in tags) {
            chipGroupTags.addValue(tag.name, showRemoveIcon = false)
        }
    }

    private fun ListItemPopularPostBinding.showQuickActions(item: Post, position: Int) {
        quickActionsVisible[position] = true
        layoutQuickActions.root.isVisible = true

        layoutQuickActions.buttonQuickActionShare.setOnClickListener { quickActionsCallback?.onShareClicked(item) }
        layoutQuickActions.buttonQuickActionSave.setOnClickListener { quickActionsCallback?.onSaveClicked(item) }
    }

    private fun ListItemPopularPostBinding.hideQuickActions(position: Int) {
        quickActionsVisible[position] = false
        layoutQuickActions.root.isGone = true
    }
}
