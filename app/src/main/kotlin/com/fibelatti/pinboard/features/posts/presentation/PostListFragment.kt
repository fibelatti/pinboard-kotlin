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
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.isAtTheTop
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultTransitionListener
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.NewestFirst
import com.fibelatti.pinboard.features.posts.domain.usecase.SortType
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_post_list.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_search_active.*
import javax.inject.Inject

class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter
) : BaseFragment() {

    private val navigationViewModel: NavigationViewModel by lazy {
        viewModelFactory.get<NavigationViewModel>(requireActivity())
    }
    private val postListViewModel: PostListViewModel by lazy {
        viewModelFactory.get<PostListViewModel>(requireActivity())
    }

    private var searchTerm: String = ""

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

        buttonClearSearch.setOnClickListener { navigationViewModel.clearSearch() }

        recyclerViewPosts
            .withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = postsAdapter

        postsAdapter.onItemClicked = {
            navigationViewModel.viewLink(it)
            showPostDetail()
        }
        postsAdapter.onEmptyFilter = {
            showEmptyLayout(
                title = R.string.posts_empty_filter_title,
                description = R.string.posts_empty_filter_description
            )
        }
    }

    private fun setupViewModels() {
        with(postListViewModel) {
            observeEvent(posts, ::showPosts)
            observeEvent(loading, ::handleLoading)
            error(error, ::handleError)
        }

        with(navigationViewModel) {
            observe(content, ::load)
            observeEvent(newSort) { toggleSorting(it) }
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        layoutProgressBar.visibleIf(isLoading, otherwiseVisibility = View.GONE)
        recyclerViewPosts.goneIf(isLoading)
        layoutEmptyList.goneIf(isLoading)
    }

    private fun load(content: NavigationViewModel.Content) {
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
                setOnClickListener { addLink() }
            }
        }

        when (content.contentType) {
            is NavigationViewModel.ContentType.All -> {
                postListViewModel.getAll(content.sortType, content.search.tags)
            }
            is NavigationViewModel.ContentType.Recent -> {
                postListViewModel.getRecent(content.sortType, content.search.tags)
            }
            is NavigationViewModel.ContentType.Public -> {
                postListViewModel.getPublic(content.sortType, content.search.tags)
            }
            is NavigationViewModel.ContentType.Private -> {
                postListViewModel.getPrivate(content.sortType, content.search.tags)
            }
            is NavigationViewModel.ContentType.Unread -> {
                postListViewModel.getUnread(content.sortType, content.search.tags)
            }
            is NavigationViewModel.ContentType.Untagged -> {
                postListViewModel.getUntagged(content.sortType)
            }
            is NavigationViewModel.ContentType.Tags -> TODO()
            is NavigationViewModel.ContentType.Tag -> TODO()
        }.exhaustive

        searchTerm = content.search.term

        layoutSearchActive.visibleIf(
            searchTerm.isNotEmpty() || content.search.tags.isNotEmpty(),
            otherwiseVisibility = View.GONE
        )
    }

    private fun showPosts(list: List<Post>) {
        if (this.isAtTheTop()) {
            if (list.isNotEmpty()) {
                recyclerViewPosts.visible()
                layoutEmptyList.gone()

                postsAdapter.addAll(list)
                postsAdapter.filter(searchTerm)
                mainActivity?.updateTitleLayout { setPostCount(postsAdapter.itemCount) }
            } else {
                showEmptyLayout(
                    title = R.string.posts_empty_title,
                    description = R.string.posts_empty_description
                )
            }
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
            R.id.menuItemSearch -> showSearchView()
            R.id.menuItemSort -> navigationViewModel.toggleSorting()
        }

        return true
    }

    private fun addLink() {
        inTransaction {
            setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
            add(R.id.fragmentHost, requireActivity().createFragment<PostAddFragment>(), PostAddFragment.TAG)
            addToBackStack(PostAddFragment.TAG)
        }
    }

    private fun showPostDetail() {
        inTransaction {
            setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out)
            add(R.id.fragmentHost, requireActivity().createFragment<PostDetailFragment>(), PostDetailFragment.TAG)
            addToBackStack(PostDetailFragment.TAG)
        }
    }

    private fun showSearchView() {
        inTransaction {
            setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
            add(R.id.fragmentHost, requireActivity().createFragment<PostSearchFragment>(), PostSearchFragment.TAG)
            addToBackStack(PostSearchFragment.TAG)
        }
    }

    private fun toggleSorting(sortType: SortType) {
        mainActivity?.snackbar(
            getString(if (sortType == NewestFirst) R.string.posts_sorting_newest_first else R.string.posts_sorting_oldest_first)
        )
    }
}
