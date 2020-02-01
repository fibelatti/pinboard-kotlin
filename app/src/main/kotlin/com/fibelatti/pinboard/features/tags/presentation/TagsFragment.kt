package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.fragment_tags.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_tag_list.*
import javax.inject.Inject

class TagsFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment(R.layout.fragment_tags) {

    companion object {
        @JvmStatic
        val TAG: String = "TagsFragment"
    }

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(requireActivity()) }
    private val tagsViewModel by lazy { viewModelFactory.get<TagsViewModel>(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshTags)
        }

        recyclerViewTags
            .withLinearLayoutManager()
            .adapter = tagsAdapter

        tagsAdapter.onItemClicked = { appStateViewModel.runAction(PostsForTag(it)) }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.tagListContent) { content ->
            setupActivityViews()
            handleLoading(content.shouldLoad)

            if (content.shouldLoad) {
                tagsViewModel.getAll(TagsViewModel.Source.MENU)
            } else {
                showTags(content.tags)
            }

            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
        }
        viewLifecycleOwner.observe(tagsViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.tags_title)
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                gone()
            }
            fab.hide()
        }
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
}
