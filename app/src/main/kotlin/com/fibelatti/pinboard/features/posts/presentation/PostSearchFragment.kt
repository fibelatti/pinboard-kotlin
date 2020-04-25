package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.activityViewModel
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.viewModel
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagsAdapter
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import kotlinx.android.synthetic.main.fragment_search_post.*
import javax.inject.Inject

class PostSearchFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment(R.layout.fragment_search_post) {

    companion object {
        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val tagsViewModel by viewModel { viewModelProvider.tagsViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        layoutRoot.animateChangingTransitions()

        editTextSearchTerm.onKeyboardSubmit { editTextSearchTerm.hideKeyboard() }

        tagListLayout.setAdapter(tagsAdapter) { appStateViewModel.runAction(AddSearchTag(it)) }
        tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshSearchTags) }
        tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.searchContent) { content ->
            setupActivityViews()
            editTextSearchTerm.setText(content.searchParameters.term)

            if (content.searchParameters.tags.isNotEmpty()) {
                chipGroupSelectedTags.removeAllViews()
                for (tag in content.searchParameters.tags) {
                    chipGroupSelectedTags.addView(createTagChip(tag))
                }

                textViewSelectedTagsTitle.visible()
            } else {
                textViewSelectedTagsTitle.gone()
                chipGroupSelectedTags.removeAllViews()
            }

            if (content.shouldLoadTags) {
                tagListLayout.showLoading()
                tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
            } else {
                tagsViewModel.sortTags(content.availableTags, tagListLayout.getCurrentTagSorting())
            }
        }
        viewLifecycleOwner.observe(tagsViewModel.tags, tagListLayout::showTags)
        viewLifecycleOwner.observe(tagsViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.search_title)
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_search)
                setOnMenuItemClickListener(::handleMenuClick)
            }
            fab.run {
                blink {
                    setImageResource(R.drawable.ic_search)
                    setOnClickListener { appStateViewModel.runAction(Search(editTextSearchTerm.textAsString())) }
                }
            }
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> { appStateViewModel.runAction(ClearSearch) }
        }

        return true
    }

    private fun createTagChip(value: Tag): View {
        return chipGroupSelectedTags.inflate(R.layout.list_item_chip, false)
            .applyAs<View, TagChip> {
                setValue(value)
                setOnCloseIconClickListener { appStateViewModel.runAction(RemoveSearchTag(value)) }
            }
    }
}
