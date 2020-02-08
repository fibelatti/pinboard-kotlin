package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.get
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.DefaultTransitionListener
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.GetNextPostPage
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.PostsDisplayed
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.ToggleSorting
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import kotlinx.android.synthetic.main.fragment_post_list.*
import kotlinx.android.synthetic.main.layout_offline_alert.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_search_active.*
import javax.inject.Inject

class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter
) : BaseFragment(R.layout.fragment_post_list) {

    companion object {
        @JvmStatic
        val TAG: String = "PostListFragment"
    }

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(requireActivity()) }
    private val postListViewModel by lazy { viewModelFactory.get<PostListViewModel>(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            setupSharedTransition()
        }
    }

    override fun onPause() {
        super.onPause()
        imageViewAppLogo?.gone()
    }

    @Suppress("MagicNumber")
    private fun setupSharedTransition() {
        val animTime = requireContext().resources.getInteger(R.integer.anim_time_long).toLong()
        val delayMillis = 100L

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .setDuration(animTime)
            .addListener(object : Transition.TransitionListener by DefaultTransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    // Changing the visibility immediately after the transition has finished
                    // won't work, so delay it a bit
                    Handler().postDelayed({ imageViewAppLogo?.gone() }, delayMillis)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewAppLogo?.goneIf(savedInstanceState != null)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO

        layoutRoot.animateChangingTransitions()

        buttonClearSearch.setOnClickListener { appStateViewModel.runAction(ClearSearch) }
        buttonRetryConnection.setOnClickListener { appStateViewModel.runAction(Refresh) }

        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(Refresh)
        }

        recyclerViewPosts
            .apply {
                setPageSize(AppConfig.DEFAULT_PAGE_SIZE)
                setMinDistanceToLastItem(AppConfig.DEFAULT_PAGE_SIZE / 2)
                onShouldRequestNextPage = {
                    progressBarNextPage.visible()
                    appStateViewModel.runAction(GetNextPostPage)
                }
            }
            .withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = postsAdapter

        postsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
        postsAdapter.onTagClicked = { appStateViewModel.runAction(PostsForTag(it)) }
        postsAdapter.quickActionsCallback = object : PostListAdapter.QuickActionsCallback {

            override fun onShareClicked(item: Post) {
                requireActivity().shareText(R.string.posts_share_title, item.url)
            }

            override fun onEditClicked(item: Post) {
                appStateViewModel.runAction(EditPost(item))
            }
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(postListViewModel.error, ::handleError)
        viewLifecycleOwner.observe(appStateViewModel.postListContent, ::updateContent)
    }

    private fun updateContent(content: PostListContent) {
        mainActivity?.updateTitleLayout { hideNavigateUp() }
        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(if (content.category != Recent) R.menu.menu_main else R.menu.menu_main_recent)
                setOnMenuItemClickListener(::handleMenuClick)
                visible()
                show()
            }
            fab.run {
                setImageResource(R.drawable.ic_pin)
                setOnClickListener { appStateViewModel.runAction(AddPost) }
                show()
            }
        }

        when (content.shouldLoad) {
            is ShouldLoadFirstPage -> {
                mainActivity?.updateTitleLayout { setTitle(content.title) }

                layoutProgressBar.visible()
                recyclerViewPosts.gone()
                layoutEmptyList.gone()

                postsAdapter.clearItems()

                postListViewModel.loadContent(content)
            }
            is ShouldLoadNextPage -> postListViewModel.loadContent(content)
            is Loaded -> showPosts(content)
        }
        layoutSearchActive.visibleIf(
            content.searchParameters.isActive(),
            otherwiseVisibility = View.GONE
        )
        layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
    }

    private fun showPosts(content: PostListContent) {
        layoutProgressBar.gone()
        progressBarNextPage.gone()
        recyclerViewPosts.onRequestNextPageCompleted()

        mainActivity?.updateTitleLayout {
            setPostListTitle(content.title, content.totalCount, content.sortType)
        }

        if (content.posts == null) {
            showEmptyLayout()
            return
        }

        postsAdapter.showDescription = content.showDescription
        if (!content.posts.alreadyDisplayed || postsAdapter.itemCount == 0) {
            recyclerViewPosts.visible()
            layoutEmptyList.gone()

            postsAdapter.addAll(content.posts.list, content.posts.diffUtil.result)
            appStateViewModel.runAction(PostsDisplayed)
        }
    }

    private fun showEmptyLayout() {
        mainActivity?.updateTitleLayout { hidePostCount() }
        recyclerViewPosts.gone()
        layoutEmptyList.apply {
            visible()
            setTitle(R.string.posts_empty_title)
            setDescription(R.string.posts_empty_description)
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemSearch -> appStateViewModel.runAction(ViewSearch)
            R.id.menuItemSort -> appStateViewModel.runAction(ToggleSorting)
        }

        return true
    }
}
