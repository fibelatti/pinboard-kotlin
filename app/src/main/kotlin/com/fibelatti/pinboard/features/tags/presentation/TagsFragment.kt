package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.error
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
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.appstate.TagList
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_tags.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_tag_list.*
import javax.inject.Inject

class TagsFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = TagsFragment::class.java.simpleName
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(this)
    }
    private val tagsViewModel: TagsViewModel by lazy { viewModelFactory.get<TagsViewModel>(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tags, container, false)

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

        mainActivity?.updateTitleLayout {
            setTitle(R.string.tags_title)
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar: BottomAppBar, fab: FloatingActionButton ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                gone()
            }
            fab.hide()
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.getContent()) { content ->
            if (content is TagList) {
                handleLoading(content.shouldLoad)

                if (content.shouldLoad) {
                    tagsViewModel.getAll(TagsViewModel.Source.MENU)
                } else {
                    showTags(content.tags)
                }

                layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
            }
        }
        error(tagsViewModel.error, ::handleError)
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

            tagsAdapter.addAll(list)
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