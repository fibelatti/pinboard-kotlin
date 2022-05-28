package com.fibelatti.pinboard.features.posts.presentation

import android.view.LayoutInflater
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.fibelatti.core.android.recyclerview.BaseAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.databinding.ListItemPostBinding
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class PostListAdapter @Inject constructor(
    private val dateFormatter: DateFormatter,
) : BaseAdapter<Post, ListItemPostBinding>(
    binding = { parent -> ListItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false) },
) {

    interface QuickActionsCallback {

        fun onShareClicked(item: Post)

        fun onEditClicked(item: Post)

        fun onDeleteClicked(item: Post)
    }

    var showDescription: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemClicked: ((Post) -> Unit)? = null
    var onTagClicked: ((Tag) -> Unit)? = null
    var quickActionsCallback: QuickActionsCallback? = null

    private val quickActionsVisible: MutableMap<Int, Boolean> = mutableMapOf()

    fun submitList(newItems: List<Post>, diffResult: DiffUtil.DiffResult) {
        submitList(newItems) { diffResult.dispatchUpdatesTo(this) }
    }

    override fun onBindViewHolder(holder: ViewHolder<ListItemPostBinding>, position: Int) {
        val binding = holder.binding
        val item = items[position]

        binding.textViewPendingSync.isVisible = item.pendingSync != null
        when (item.pendingSync) {
            PendingSync.ADD -> binding.textViewPendingSync.setText(R.string.posts_pending_add)
            PendingSync.UPDATE -> binding.textViewPendingSync.setText(R.string.posts_pending_update)
            PendingSync.DELETE -> binding.textViewPendingSync.setText(R.string.posts_pending_delete)
            else -> Unit
        }

        binding.textViewPrivate.isVisible = item.private
        binding.textViewReadLater.isVisible = item.readLater

        binding.textViewLinkTitle.text = item.title

        val addedDate = dateFormatter.tzFormatToDisplayFormat(item.time)
        if (addedDate != null) {
            binding.textViewLinkAddedDate.isVisible = true
            binding.textViewLinkAddedDate.text = binding.root.context.getString(R.string.posts_saved_on, addedDate)
        } else {
            binding.textViewLinkAddedDate.isGone = true
        }

        binding.textViewDescription.text = item.description
        binding.textViewDescription.isVisible = showDescription && item.description.isNotBlank()

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
        binding.chipGroupTags.onTagChipClicked = onTagClicked
    }

    private fun ListItemPostBinding.layoutTags(tags: List<Tag>) {
        chipGroupTags.isVisible = true
        chipGroupTags.removeAllViews()
        for (tag in tags) {
            chipGroupTags.addValue(tag.name, showRemoveIcon = false)
        }
    }

    private fun ListItemPostBinding.showQuickActions(item: Post, position: Int) {
        quickActionsVisible[position] = true
        layoutQuickActions.root.isVisible = true

        layoutQuickActions.buttonQuickActionEdit.setOnClickListener {
            quickActionsCallback?.onEditClicked(item)
        }
        layoutQuickActions.buttonQuickActionShare.setOnClickListener {
            quickActionsCallback?.onShareClicked(item)
        }
        layoutQuickActions.buttonQuickActionDelete.setOnClickListener {
            quickActionsCallback?.onDeleteClicked(item)
        }
    }

    private fun ListItemPostBinding.hideQuickActions(position: Int) {
        quickActionsVisible[position] = false
        layoutQuickActions.root.isGone = true
    }
}
