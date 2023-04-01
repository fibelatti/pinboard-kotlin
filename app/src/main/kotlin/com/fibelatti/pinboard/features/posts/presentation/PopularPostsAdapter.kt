package com.fibelatti.pinboard.features.posts.presentation

import android.view.LayoutInflater
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.fibelatti.core.android.recyclerview.BaseListAdapter
import com.fibelatti.core.android.recyclerview.ViewHolder
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.databinding.ListItemPopularPostBinding
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import javax.inject.Inject

class PopularPostsAdapter @Inject constructor() : BaseListAdapter<Post, ListItemPopularPostBinding>(
    binding = { parent ->
        ListItemPopularPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    },
    itemsTheSame = { oldItem, newItem -> oldItem.url == newItem.url },
) {

    var onItemClicked: ((Post) -> Unit)? = null
    var onItemLongClicked: ((Post) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder<ListItemPopularPostBinding>, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.textViewLinkTitle.text = item.title

        if (item.tags.isNullOrEmpty()) {
            binding.chipGroupTags.isGone = true
        } else {
            binding.chipGroupTags.isVisible = true
            binding.chipGroupTags.setContent {
                AppTheme {
                    MultilineChipGroup(
                        items = item.tags.map { tag -> ChipGroup.Item(text = tag.name) },
                        onItemClick = {},
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
