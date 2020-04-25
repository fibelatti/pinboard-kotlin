package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.activityViewModel
import com.fibelatti.pinboard.core.extension.viewModel
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.mainActivity
import kotlinx.android.synthetic.main.fragment_tags.*
import javax.inject.Inject

class TagsFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment(R.layout.fragment_tags) {

    companion object {
        @JvmStatic
        val TAG: String = "TagsFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val tagsViewModel by viewModel { viewModelProvider.tagsViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        tagListLayout.setAdapter(tagsAdapter) { appStateViewModel.runAction(PostsForTag(it)) }
        tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshTags) }
        tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.tagListContent) { content ->
            setupActivityViews()

            if (content.shouldLoad) {
                tagListLayout.showLoading()
                tagsViewModel.getAll(TagsViewModel.Source.MENU)
            } else {
                tagsViewModel.sortTags(content.tags, tagListLayout.getCurrentTagSorting())
            }

            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
        }
        viewLifecycleOwner.observe(tagsViewModel.tags, tagListLayout::showTags)
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
}
