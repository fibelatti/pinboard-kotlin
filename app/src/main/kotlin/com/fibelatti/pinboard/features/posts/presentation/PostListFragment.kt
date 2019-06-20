package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultTransitionListener
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Tag
import com.fibelatti.pinboard.features.appstate.Tags
import com.fibelatti.pinboard.features.appstate.ToggleSorting
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_post_list.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_search_active.*
import javax.inject.Inject

class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostListFragment::class.java.simpleName
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(requireActivity())
    }
    private val postListViewModel: PostListViewModel by lazy {
        viewModelFactory.get<PostListViewModel>(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSharedTransition()
    }

    private fun setupSharedTransition() {
        val animTime = requireContext().resources.getInteger(R.integer.anim_time_long).toLong()

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
                .setDuration(animTime)
                .addListener(object : Transition.TransitionListener by DefaultTransitionListener {
                    override fun onTransitionEnd(transition: Transition) {
                        Handler().postDelayed({ imageViewAppLogo?.gone() }, animTime)
                    }
                })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_post_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO

        layoutRoot.animateChangingTransitions()

        buttonClearSearch.setOnClickListener { appStateViewModel.runAction(ClearSearch) }

        recyclerViewPosts
            .withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = postsAdapter

        postsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
    }

    private fun setupViewModels() {
        with(postListViewModel) {
            observeEvent(loading, ::handleLoading)
            error(error, ::handleError)
        }

        observe(appStateViewModel.getContent()) { content ->
            if (content is PostList) showPostList(content)
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        layoutProgressBar.visibleIf(isLoading, otherwiseVisibility = View.GONE)
        recyclerViewPosts.goneIf(isLoading)
        layoutEmptyList.goneIf(isLoading)
    }

    private fun showPostList(content: PostList) {
        mainActivity?.updateTitleLayout {
            hideNavigateUp()
            setTitle(content.title)
        }
        mainActivity?.updateViews { bottomAppBar: BottomAppBar, fab: FloatingActionButton ->
            bottomAppBar.run {
                fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(R.menu.menu_main)
                setOnMenuItemClickListener { item: MenuItem? -> handleMenuClick(item) }
                show()
            }
            fab.run {
                setImageResource(R.drawable.ic_pin)
                setOnClickListener { appStateViewModel.runAction(AddPost) }
            }
        }

        if (content.shouldLoad) {
            when (content.category) {
                is All -> {
                    postListViewModel.getAll(content.sortType, content.searchParameters.term, content.searchParameters.tags)
                }
                is Recent -> {
                    postListViewModel.getRecent(content.sortType, content.searchParameters.term, content.searchParameters.tags)
                }
                is Public -> {
                    postListViewModel.getPublic(content.sortType, content.searchParameters.term, content.searchParameters.tags)
                }
                is Private -> {
                    postListViewModel.getPrivate(content.sortType, content.searchParameters.term, content.searchParameters.tags)
                }
                is Unread -> {
                    postListViewModel.getUnread(content.sortType, content.searchParameters.term, content.searchParameters.tags)
                }
                is Untagged -> {
                    postListViewModel.getUntagged(content.sortType, content.searchParameters.term)
                }
                is Tags -> {
                    // TODO
                }
                is Tag -> {
                    // TODO
                }
            }.exhaustive
        } else {
            showPosts(content.posts, content.sortType)
        }

        layoutSearchActive.visibleIf(content.searchParameters.isActive(), otherwiseVisibility = View.GONE)
    }

    private fun showPosts(list: List<Post>, sortType: SortType) {
        if (list.isNotEmpty()) {
            recyclerViewPosts.visible()
            layoutEmptyList.gone()

            postsAdapter.addAll(list)
            mainActivity?.updateTitleLayout { setPostCount(postsAdapter.itemCount, sortType) }
        } else {
            showEmptyLayout(
                title = R.string.posts_empty_title,
                description = R.string.posts_empty_description
            )
        }
    }

    private fun showEmptyLayout(
        @StringRes title: Int,
        @StringRes description: Int
    ) {
        mainActivity?.updateTitleLayout { hidePostCount() }
        recyclerViewPosts.gone()
        layoutEmptyList.apply {
            visible()
            setTitle(title)
            setDescription(description)
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
