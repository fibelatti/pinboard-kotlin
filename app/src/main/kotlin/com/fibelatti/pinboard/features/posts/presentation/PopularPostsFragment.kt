package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.get
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.toast
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import kotlinx.android.synthetic.main.fragment_popular_posts.*
import kotlinx.android.synthetic.main.layout_offline_alert.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class PopularPostsFragment @Inject constructor(
    private val popularPostsAdapter: PopularPostsAdapter
) : BaseFragment(R.layout.fragment_popular_posts) {

    companion object {
        @JvmStatic
        val TAG: String = "PopularPostsFragment"
    }

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(requireActivity()) }
    private val popularPostsViewModel by lazy { viewModelFactory.get<PopularPostsViewModel>(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        buttonRetryConnection.setOnClickListener { appStateViewModel.runAction(RefreshPopular) }

        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshPopular)
        }

        recyclerViewPosts.withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = popularPostsAdapter

        popularPostsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
        popularPostsAdapter.quickActionsCallback =
            object : PopularPostsAdapter.QuickActionsCallback {

                override fun onShareClicked(item: Post) {
                    requireActivity().shareText(R.string.posts_share_title, item.url)
                }

                override fun onSaveClicked(item: Post) {
                    popularPostsViewModel.saveLink(item)
                }
            }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.popularPostsContent) { content ->
            mainActivity?.updateTitleLayout {
                setTitle(R.string.popular_title)
                setNavigateUp { navigateBack() }
            }

            mainActivity?.updateViews { bottomAppBar, fab ->
                bottomAppBar.run {
                    navigationIcon = null
                    menu.clear()
                    gone()
                }
                fab.hide()
            }

            if (content.shouldLoad) {
                layoutProgressBar.visible()
                recyclerViewPosts.gone()
                layoutEmptyList.gone()
                popularPostsViewModel.getPosts()
            } else {
                showPosts(content)
            }

            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
        }
        with(popularPostsViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
                recyclerViewPosts.goneIf(it, otherwiseVisibility = View.VISIBLE)
            }
            viewLifecycleOwner.observe(saved) {
                requireActivity().toast(getString(R.string.posts_saved_feedback))
            }
            viewLifecycleOwner.observe(error, ::handleError)
        }
    }

    private fun showPosts(content: PopularPostsContent) {
        layoutProgressBar.gone()

        if (content.posts.isEmpty()) {
            recyclerViewPosts.gone()
            layoutEmptyList.apply {
                visible()
                setTitle(R.string.posts_empty_title)
                setDescription(R.string.posts_empty_description)
            }
            return
        }

        layoutEmptyList.gone()
        recyclerViewPosts.visible()
        popularPostsAdapter.submitList(content.posts)
    }
}
