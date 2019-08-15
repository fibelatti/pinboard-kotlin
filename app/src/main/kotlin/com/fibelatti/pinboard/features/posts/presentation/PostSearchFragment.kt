package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagsAdapter
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import kotlinx.android.synthetic.main.fragment_search_post.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_tag_list.*
import javax.inject.Inject

class PostSearchFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(this)
    }
    private val tagsViewModel: TagsViewModel by lazy { viewModelFactory.get<TagsViewModel>(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        layoutRoot.animateChangingTransitions()

        editTextSearchTerm.onKeyboardSubmit { editTextSearchTerm.hideKeyboard() }

        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshSearchTags)
        }

        recyclerViewTags
            .withLinearLayoutManager()
            .adapter = tagsAdapter

        tagsAdapter.onItemClicked = { appStateViewModel.runAction(AddSearchTag(it)) }

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
                setOnMenuItemClickListener { item: MenuItem? -> handleMenuClick(item) }
            }
            fab.run {
                blink {
                    setImageResource(R.drawable.ic_search)
                    setOnClickListener { appStateViewModel.runAction(Search(editTextSearchTerm.textAsString())) }
                }
            }
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.searchContent) { content ->
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

            handleLoading(content.shouldLoadTags)

            if (content.shouldLoadTags) {
                tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
            } else {
                showTags(content.availableTags)
            }
        }
        observe(tagsViewModel.error, ::handleError)
    }

    private fun handleLoading(loading: Boolean) {
        layoutProgressBar.visibleIf(loading, otherwiseVisibility = View.GONE)
        recyclerViewTags.goneIf(loading)
        layoutEmptyList.goneIf(loading)
    }

    private fun showTags(list: List<Tag>) {
        if (list.isNotEmpty()) {
            recyclerViewTags.visible()
            layoutEmptyList.gone()

            tagsAdapter.submitList(list)
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        recyclerViewTags.gone()
        layoutEmptyList.apply {
            setIcon(R.drawable.ic_tag)
            setTitle(R.string.tags_empty_title)
            setDescription(R.string.tags_empty_description)
            visible()
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> { appStateViewModel.runAction(ClearSearch) }
        }

        return true
    }

    private fun createTagChip(value: Tag): View {
        return layoutInflater.inflate(R.layout.list_item_chip, chipGroupSelectedTags, false)
            .applyAs<View, TagChip> {
                setValue(value)
                setOnCloseIconClickListener { appStateViewModel.runAction(RemoveSearchTag(value)) }
            }
    }
}
