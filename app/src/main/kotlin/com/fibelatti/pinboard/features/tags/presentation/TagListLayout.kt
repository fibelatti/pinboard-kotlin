package com.fibelatti.pinboard.features.tags.presentation

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import kotlinx.android.synthetic.main.layout_progress_bar.view.*
import kotlinx.android.synthetic.main.layout_tag_list.view.*

class TagListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var tagsAdapter: TagsAdapter? = null

    init {
        inflate(R.layout.layout_tag_list, true)
        orientation = VERTICAL
        layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGE_APPEARING)
            enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
        }
        recyclerViewTags.withLinearLayoutManager()
    }

    fun setAdapter(adapter: TagsAdapter, onClickListener: (Tag) -> Unit = {}) {
        tagsAdapter = adapter
        setAdapterItemOnClickListener(onClickListener)
        recyclerViewTags.adapter = adapter
    }

    fun setAdapterItemOnClickListener(onClickListener: (Tag) -> Unit) {
        tagsAdapter?.onItemClicked = onClickListener
    }

    fun setOnRefreshListener(onRefresh: () -> Unit) {
        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            onRefresh()
        }
    }

    fun setSortingClickListener(onSortingSelected: (List<Tag>, TagSorting) -> Unit) {
        buttonTagSortingAtoZ.isChecked = true

        buttonTagSortingAtoZ.setOnClickListener {
            buttonTagSortingAtoZ.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.AtoZ) }
            recyclerViewTags.scrollToPosition(0)
        }
        buttonTagSortingMoreFirst.setOnClickListener {
            buttonTagSortingMoreFirst.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.MoreFirst) }
            recyclerViewTags.scrollToPosition(0)
        }
        buttonTagSortingLessFirst.setOnClickListener {
            buttonTagSortingLessFirst.isChecked = true
            tagsAdapter?.let { onSortingSelected(it.getItems(), TagSorting.LessFirst) }
            recyclerViewTags.scrollToPosition(0)
        }
    }

    fun showLoading() {
        layoutProgressBar.visible()
        buttonGroupTagSorting.gone()
        recyclerViewTags.gone()
        layoutEmptyList.gone()
    }

    fun showTags(list: List<Tag>) {
        if (list.isNotEmpty()) {
            buttonGroupTagSorting.visible()
            recyclerViewTags.visible()
            layoutEmptyList.gone()

            tagsAdapter?.submitList(list)
        } else {
            showEmptyLayout()
        }
        layoutProgressBar.gone()
    }

    private fun showEmptyLayout() {
        buttonGroupTagSorting.gone()
        recyclerViewTags.gone()
        layoutEmptyList.apply {
            setIcon(R.drawable.ic_tag)
            setTitle(R.string.tags_empty_title)
            setDescription(R.string.tags_empty_description)
            visible()
        }
    }

    fun getCurrentTagSorting(): TagSorting {
        return when (buttonGroupTagSorting.checkedButtonId) {
            R.id.buttonTagSortingMoreFirst -> TagSorting.MoreFirst
            R.id.buttonTagSortingLessFirst -> TagSorting.LessFirst
            else -> TagSorting.AtoZ
        }
    }
}
