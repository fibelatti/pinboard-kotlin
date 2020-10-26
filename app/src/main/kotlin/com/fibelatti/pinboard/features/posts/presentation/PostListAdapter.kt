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
import com.fibelatti.pinboard.databinding.ListItemPostBinding
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class PostListAdapter @Inject constructor(
    private val dateFormatter: DateFormatter
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    interface QuickActionsCallback {

        fun onShareClicked(item: Post)

        fun onEditClicked(item: Post)

        fun onDeleteClicked(item: Post)
    }

    private val items: MutableList<Post> = mutableListOf()
    var showDescription: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemClicked: ((Post) -> Unit)? = null
    var onTagClicked: ((Tag) -> Unit)? = null
    var quickActionsCallback: QuickActionsCallback? = null

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
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

        private var quickActionsVisible: Boolean = false

        fun bind(item: Post) = itemView.bindView(item)

        private fun View.bindView(item: Post) {
            val binding = ListItemPostBinding.bind(this)

            binding.textViewPrivate.visibleIf(item.private, otherwiseVisibility = View.GONE)
            binding.textViewReadLater.visibleIf(item.readLater, otherwiseVisibility = View.GONE)

            binding.textViewLinkTitle.text = item.title

            val addedDate = dateFormatter.tzFormatToDisplayFormat(item.time)
            if (addedDate != null) {
                binding.textViewLinkAddedDate.visible()
                binding.textViewLinkAddedDate.text =
                    context.getString(R.string.posts_saved_on, addedDate)
            } else {
                binding.textViewLinkAddedDate.gone()
            }

            binding.textViewDescription.text = item.description
            binding.textViewDescription.visibleIf(
                showDescription && item.description.isNotBlank(),
                otherwiseVisibility = View.GONE
            )

            if (item.tags.isNullOrEmpty()) {
                binding.chipGroupTags.gone()
            } else {
                binding.layoutTags(item.tags)
            }

            binding.hideQuickActions()

            setOnClickListener { onItemClicked?.invoke(item) }
            setOnLongClickListener {
                if (quickActionsCallback == null) {
                    return@setOnLongClickListener false
                }

                if (!quickActionsVisible) {
                    binding.showQuickActions(item)
                } else {
                    binding.hideQuickActions()
                }

                true
            }
            binding.chipGroupTags.onTagChipClicked = onTagClicked
        }

        private fun ListItemPostBinding.layoutTags(tags: List<Tag>) {
            chipGroupTags.visible()
            chipGroupTags.removeAllViews()
            for (tag in tags) {
                chipGroupTags.addValue(tag.name, showRemoveIcon = false)
            }
        }

        private fun ListItemPostBinding.showQuickActions(item: Post) {
            quickActionsVisible = true
            layoutQuickActions.root.visible()

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

        private fun ListItemPostBinding.hideQuickActions() {
            quickActionsVisible = false
            layoutQuickActions.root.gone()
        }
    }
}
