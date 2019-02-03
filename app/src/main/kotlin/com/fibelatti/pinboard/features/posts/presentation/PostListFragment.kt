package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
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
import kotlinx.android.synthetic.main.layout_empty_list.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

@Suppress("ValidFragment")
class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter
) : BaseFragment() {

    private val navigationViewModel: NavigationViewModel by lazy {
        viewModelFactory.get<NavigationViewModel>(requireActivity())
    }
    private val postListViewModel: PostListViewModel by lazy {
        viewModelFactory.get<PostListViewModel>(requireActivity())
    }

    private val animTime by lazy {
        requireContext().resources.getInteger(R.integer.anim_time_long).toLong()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSharedTransition()
    }

    private fun setupSharedTransition() {
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

        recyclerViewPosts
            .withLinearLayoutManager()
            .adapter = postsAdapter

        postsAdapter.onItemClicked = {
            navigationViewModel.viewLink(it)
            showPostDetail()
        }
    }

    private fun setupViewModels() {
        with(postListViewModel) {
            observeEvent(posts, ::showPosts)
            observeEvent(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
                recyclerViewPosts.goneIf(it)
                layoutEmptyList.goneIf(it)
            }
        }

        with(navigationViewModel) {
            observe(content, ::load)
            observeEvent(newSort) { toggleSorting(it) }
        }
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
            NavigationViewModel.ContentType.ALL -> postListViewModel.getAll(content.sortType)
            NavigationViewModel.ContentType.RECENT -> postListViewModel.getRecent(content.sortType)
        }
    }

    private fun showPosts(list: List<Post>) {
        if (list.isNotEmpty()) {
            postsAdapter.addAll(list)
            recyclerViewPosts.visible()
            layoutEmptyList.gone()

            mainActivity?.updateTitleLayout { setPostCount(list.size) }
        } else {
            showEmptyLayout()

            mainActivity?.updateTitleLayout { hidePostCount() }
        }
    }

    private fun showEmptyLayout() {
        recyclerViewPosts.gone()
        layoutEmptyList.visible()
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemSearch -> showSearchView()
            R.id.menuItemSort -> navigationViewModel.toggleSorting()
        }

        return true
    }

    private fun addLink() {
        // TODO
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
