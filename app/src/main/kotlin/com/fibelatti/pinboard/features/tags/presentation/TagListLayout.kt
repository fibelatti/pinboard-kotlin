package com.fibelatti.pinboard.features.tags.presentation

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.fibelatti.core.extension.clearText
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.LayoutTagListBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting

class TagListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutTagListBinding.inflate(LayoutInflater.from(context), this, true)

    private var tagsAdapter: TagsAdapter? = null

    val isInputFocused: Boolean get() = binding.editTextTagFilter.isFocused
    var isLoading: Boolean by binding.progressBar::isVisible

    init {
        binding.root.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGE_APPEARING)
            enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
        }

        binding.editTextTagFilter.doAfterTextChanged {
            tagsAdapter?.filter(query = it.toString())
        }
        binding.editTextTagFilter.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_DONE) {
            hideKeyboard()
            clearFocus()
        }
    }

    fun setAdapter(
        adapter: TagsAdapter,
        onClickListener: (Tag) -> Unit = {},
        onLongClickListener: (Tag) -> Unit = {},
    ) {
        tagsAdapter = adapter
        adapter.onItemClicked = onClickListener
        adapter.onItemLongClicked = onLongClickListener
        binding.recyclerViewTags.adapter = adapter
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
            onModeSelected(onSortingSelected, TagSorting.AtoZ, filterVisible = false)
        }
        binding.buttonTagSortingMoreFirst.setOnClickListener {
            binding.buttonTagSortingMoreFirst.isChecked = true
            onModeSelected(onSortingSelected, TagSorting.MoreFirst, filterVisible = false)
        }
        binding.buttonTagSortingLessFirst.setOnClickListener {
            binding.buttonTagSortingLessFirst.isChecked = true
            onModeSelected(onSortingSelected, TagSorting.LessFirst, filterVisible = false)
        }
        binding.buttonTagSortingFilter.setOnClickListener {
            binding.buttonTagSortingFilter.isChecked = true
            onModeSelected(onSortingSelected, TagSorting.AtoZ, filterVisible = true)
        }
    }

    private fun onModeSelected(
        onSortingSelected: (List<Tag>, TagSorting) -> Unit,
        tagSorting: TagSorting,
        filterVisible: Boolean,
    ) {
        if (!filterVisible) {
            binding.root.hideKeyboard()
            binding.editTextTagFilter.clearText()
        }

        binding.textInputLayoutTagFilter.isVisible = filterVisible
        binding.recyclerViewTags.scrollToPosition(0)

        tagsAdapter?.let { onSortingSelected(it.items, tagSorting) }
    }

    fun setInputFocusChangeListener(onInputFocused: (hasFocus: Boolean) -> Unit) {
        binding.editTextTagFilter.setOnFocusChangeListener { _, hasFocus -> onInputFocused(hasFocus) }
    }

    fun showTags(list: List<Tag>) {
        if (list.isNotEmpty()) {
            binding.buttonGroupTagSorting.isVisible = true
            binding.swipeToRefresh.isVisible = true
            binding.layoutEmptyList.isGone = true

            tagsAdapter?.submitList(list)

            val currentFilter = binding.editTextTagFilter.text.toString()
            if (currentFilter.isNotBlank()) {
                tagsAdapter?.filter(query = currentFilter)
            }
        } else {
            showEmptyLayout()
        }
        binding.progressBar.isGone = true
    }

    private fun showEmptyLayout() {
        binding.buttonGroupTagSorting.isGone = true
        binding.swipeToRefresh.isGone = true
        binding.layoutEmptyList.apply {
            setIcon(R.drawable.ic_tag)
            setTitle(R.string.tags_empty_title)
            setDescription(R.string.tags_empty_description)
            isVisible = true
        }
    }

    fun getCurrentTagSorting(): TagSorting = when (binding.buttonGroupTagSorting.checkedButtonId) {
        R.id.buttonTagSortingMoreFirst -> TagSorting.MoreFirst
        R.id.buttonTagSortingLessFirst -> TagSorting.LessFirst
        else -> TagSorting.AtoZ
    }
}
