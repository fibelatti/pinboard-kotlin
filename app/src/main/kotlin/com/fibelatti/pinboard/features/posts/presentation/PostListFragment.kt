package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultTransitionListener
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.snackbar
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.posts.domain.Sorting
import com.fibelatti.pinboard.features.posts.domain.model.Post
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

    private var sorting: Sorting = Sorting.NEWEST_FIRST

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

    private fun setupViewModels() {
        with(postListViewModel) {
            observeEvent(posts, ::showPosts)
            observeEvent(loading) {
                layoutProgressBar.visibleIf(it)
                if (it) {
                    recyclerViewPosts.gone()
                    layoutEmptyList.gone()
                }
            }
        }

        observe(navigationViewModel.contentType, ::load)
    }

    private fun setupLayout() {
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO

        recyclerViewPosts
            .withLinearLayoutManager()
            .apply { addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL)) }
            .adapter = postsAdapter

        postsAdapter.onItemClicked = {
            navigationViewModel.viewLink(it)
            inTransaction {
                setCustomAnimations(
                    R.anim.slide_right_in,
                    R.anim.slide_left_out,
                    R.anim.slide_left_in,
                    R.anim.slide_right_out
                )
                add(R.id.fragmentHost, requireActivity().createFragment<PostDetailFragment>(), PostDetailFragment.TAG)
                addToBackStack(PostDetailFragment.TAG)
            }
        }
    }

    private fun showPosts(list: List<Post>) {
        if (list.isNotEmpty()) {
            postsAdapter.addAll(list)
            navigationViewModel.setPostCount(postsAdapter.itemCount)
            recyclerViewPosts.visible()
            layoutEmptyList.gone()
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        recyclerViewPosts.gone()
        layoutEmptyList.visible()
    }

    fun toggleSorting() {
        when (sorting) {
            Sorting.NEWEST_FIRST -> {
                sorting = Sorting.OLDEST_FIRST
                layoutRoot.snackbar(getString(R.string.posts_sorting_oldest_first))
            }
            Sorting.OLDEST_FIRST -> {
                sorting = Sorting.NEWEST_FIRST
                layoutRoot.snackbar(getString(R.string.posts_sorting_newest_first))
            }
        }

        navigationViewModel.contentType.value?.let(::load)
    }

    private fun load(contentType: NavigationViewModel.ContentType) {
        when (contentType) {
            NavigationViewModel.ContentType.ALL -> postListViewModel.getAll(sorting)
            NavigationViewModel.ContentType.RECENT -> postListViewModel.getRecent(sorting)
        }
    }
}
