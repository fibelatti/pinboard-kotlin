package com.fibelatti.pinboard.features.posts.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.fibelatti.core.android.recyclerview.BaseAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.databinding.ListItemPostBinding
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import javax.inject.Inject

class PostListAdapter @Inject constructor(
    private val dateFormatter: DateFormatter,
) : BaseAdapter<Post, ListItemPostBinding>(
    binding = { parent ->
        ListItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    },
) {

    var showDescription: Boolean = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemClicked: ((Post) -> Unit)? = null
    var onItemLongClicked: ((Post) -> Unit)? = null
    var onTagClicked: ((Tag) -> Unit)? = null

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
            binding.chipGroupTags.isVisible = true
            binding.chipGroupTags.setContent {
                AppTheme {
                    MultilineChipGroup(
                        items = item.tags.map { tag -> ChipGroup.Item(text = tag.name) },
                        onItemClick = { chipGroupItem ->
                            onTagClicked?.invoke(item.tags.first { it.name == chipGroupItem.text })
                        },
                        itemColors = ChipGroup.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        itemTextStyle = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                        )
                    )
                }
            }
        }

        binding.root.setOnClickListener { onItemClicked?.invoke(item) }
        binding.root.setOnLongClickListener {
            onItemLongClicked?.invoke(item)
            true
        }
    }
}
