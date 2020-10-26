package com.fibelatti.pinboard.features.tags.presentation

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.LayoutTagListBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting

class TagListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutTagListBinding.inflate(LayoutInflater.from(context), this)

    private var tagsAdapter: TagsAdapter? = null

    init {
        orientation = VERTICAL
        layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGE_APPEARING)
            enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
        }
        binding.recyclerViewTags.withLinearLayoutManager()
    }

    fun setAdapter(adapter: TagsAdapter, onClickListener: (Tag) -> Unit = {}) {
        tagsAdapter = adapter
        setAdapterItemOnClickListener(onClickListener)
        binding.recyclerViewTags.adapter = adapter
    }

    fun setAdapterItemOnClickListener(onClickListener: (Tag) -> Unit) {
        tagsAdapter?.onItemClicked = onClickListener
    }

    fun setOnRefreshListener(onRefresh: () -> Unit) {
        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            onRefresh()
        }
    }

    fun setSortingClickListener(onSortingSelected: (List<Tag>, TagSorting) -> Unit) {
        binding.buttonTagSortingAtoZ.isChecked = true

        binding.buttonTagSortingAtoZ.setOnClickListener {
            binding.buttonTagSortingAtoZ.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.AtoZ) }
            binding.recyclerViewTags.scrollToPosition(0)
        }
        binding.buttonTagSortingMoreFirst.setOnClickListener {
            binding.buttonTagSortingMoreFirst.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.MoreFirst) }
            binding.recyclerViewTags.scrollToPosition(0)
        }
        binding.buttonTagSortingLessFirst.setOnClickListener {
            binding.buttonTagSortingLessFirst.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.LessFirst) }
            binding.recyclerViewTags.scrollToPosition(0)
        }
    }

    fun showLoading() {
        binding.layoutProgressBar.root.visible()
        binding.buttonGroupTagSorting.gone()
        binding.recyclerViewTags.gone()
        binding.layoutEmptyList.gone()
    }

    fun showTags(list: List<Tag>) {
        if (list.isNotEmpty()) {
            binding.buttonGroupTagSorting.visible()
            binding.recyclerViewTags.visible()
            binding.layoutEmptyList.gone()

            tagsAdapter?.submitList(list)
        } else {
            showEmptyLayout()
        }
        binding.layoutProgressBar.root.gone()
    }

    private fun showEmptyLayout() {
        binding.buttonGroupTagSorting.gone()
        binding.recyclerViewTags.gone()
        binding.layoutEmptyList.apply {
            setIcon(R.drawable.ic_tag)
            setTitle(R.string.tags_empty_title)
            setDescription(R.string.tags_empty_description)
            visible()
        }
    }

    fun getCurrentTagSorting(): TagSorting {
        return when (binding.buttonGroupTagSorting.checkedButtonId) {
            R.id.buttonTagSortingMoreFirst -> TagSorting.MoreFirst
            R.id.buttonTagSortingLessFirst -> TagSorting.LessFirst
            else -> TagSorting.AtoZ
        }
    }
}
