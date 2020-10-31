package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.showStyledDialog
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentPostListBinding
import com.fibelatti.pinboard.features.InAppReviewManager
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.GetNextPostPage
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.PostsDisplayed
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Syncing
import com.fibelatti.pinboard.features.appstate.ToggleSorting
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter,
    private val inAppReviewManager: InAppReviewManager,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PostListFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val postListViewModel by viewModel { viewModelProvider.postListViewModel() }
    private val postDetailViewModel by viewModel { viewModelProvider.postDetailsViewModel() }

    private var binding by viewBinding<FragmentPostListBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            setupSharedTransition()
        }

        activity?.supportFragmentManager?.setFragmentResultListener(
            UserPreferencesFragment.TAG,
            this,
            { _, _ ->
                activity?.let(inAppReviewManager::checkForPlayStoreReview)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentPostListBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onPause() {
        super.onPause()
        binding.imageViewAppLogo.gone()
    }

    private fun setupSharedTransition() {
        val animTime = requireContext().resources.getInteger(R.integer.anim_time_long).toLong()
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .setDuration(animTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewAppLogo.goneIf(savedInstanceState != null)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO
        binding.imageViewAppLogo.doOnApplyWindowInsets { view, insets, _, initialMargin ->
            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                leftMargin = initialMargin.left
                topMargin = initialMargin.top
                rightMargin = initialMargin.right
                bottomMargin = initialMargin.bottom + insets.systemWindowInsetBottom
            }

            CoroutineScope(postListViewModel.coroutineContext).launch {
                delay(view.resources.getInteger(R.integer.anim_time_long).toLong())
                view.gone()
            }
        }

        binding.root.animateChangingTransitions()

        binding.layoutSearchActive.buttonClearSearch.setOnClickListener {
            appStateViewModel.runAction(ClearSearch)
        }
        binding.layoutOfflineAlert.buttonRetryConnection.setOnClickListener {
            appStateViewModel.runAction(Refresh())
        }

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(Refresh())
        }

        binding.recyclerViewPosts
            .apply {
                setPageSize(AppConfig.DEFAULT_PAGE_SIZE)
                setMinDistanceToLastItem(AppConfig.DEFAULT_PAGE_SIZE / 2)
                onShouldRequestNextPage = {
                    binding.progressBar.visible()
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

            override fun onDeleteClicked(item: Post) {
                deletePost(item)
            }
        }
    }

    private fun deletePost(post: Post) {
        context?.showStyledDialog(
            dialogStyle = R.style.AppTheme_AlertDialog,
            dialogBackground = R.drawable.background_contrast_rounded
        ) {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(postListViewModel.error, ::handleError)
        viewLifecycleOwner.observe(appStateViewModel.postListContent, ::updateContent)
        with(postDetailViewModel) {
            viewLifecycleOwner.observe(loading) {
                binding.progressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observeEvent(deleted) {
                mainActivity?.showBanner(getString(R.string.posts_deleted_feedback))
            }
            viewLifecycleOwner.observeEvent(deleteError) { error ->
                activity?.sendErrorReport(
                    error,
                    altMessage = getString(R.string.posts_deleted_error)
                )
            }
            viewLifecycleOwner.observe(error, ::handleError)
        }
    }

    override fun handleError(error: Throwable) {
        super.handleError(error)
        binding.progressBar.gone()
    }

    private fun updateContent(content: PostListContent) {
        mainActivity?.updateTitleLayout { hideNavigateUp() }
        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(if (content.category != Recent) R.menu.menu_main else R.menu.menu_main_recent)
                if (!content.canForceSync) {
                    bottomAppBar.menu.removeItem(R.id.menuItemSync)
                }
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
            is ShouldLoadFirstPage, ShouldForceLoad -> {
                mainActivity?.updateTitleLayout {
                    setTitle(getCategoryTitle(content.category))
                    hideSubTitle()
                }

                binding.progressBar.visible()
                binding.recyclerViewPosts.gone()
                binding.layoutEmptyList.gone()

                postsAdapter.clearItems()

                postListViewModel.loadContent(content)
            }
            is ShouldLoadNextPage -> postListViewModel.loadContent(content)
            is Syncing, is Loaded -> showPosts(content)
        }.exhaustive

        binding.layoutSearchActive.root.visibleIf(
            content.searchParameters.isActive(),
            otherwiseVisibility = View.GONE
        )
        binding.layoutOfflineAlert.root.goneIf(
            content.isConnected,
            otherwiseVisibility = View.VISIBLE
        )
    }

    private fun getCategoryTitle(category: ViewCategory): String {
        return when (category) {
            All -> getString(R.string.posts_title_all)
            Recent -> getString(R.string.posts_title_recent)
            Public -> getString(R.string.posts_title_public)
            Private -> getString(R.string.posts_title_private)
            Unread -> getString(R.string.posts_title_unread)
            Untagged -> getString(R.string.posts_title_untagged)
        }
    }

    private fun showPosts(content: PostListContent) {
        binding.progressBar.goneIf(content.shouldLoad == Loaded)
        binding.recyclerViewPosts.onRequestNextPageCompleted()

        mainActivity?.updateTitleLayout {
            setTitle(getCategoryTitle(content.category))
            setSubTitle(buildPostCountSubTitle(content.totalCount, content.sortType))
        }

        when {
            content.posts == null && content.shouldLoad == Loaded -> {
                showEmptyLayout()
                return
            }
            content.posts == null -> {
                // Still syncing with the API
                return
            }
            else -> {
                postsAdapter.showDescription = content.showDescription
                if (!content.posts.alreadyDisplayed || postsAdapter.itemCount == 0) {
                    binding.recyclerViewPosts.visible()
                    binding.layoutEmptyList.gone()

                    postsAdapter.addAll(content.posts.list, content.posts.diffUtil.result)
                    appStateViewModel.runAction(PostsDisplayed)
                }
            }
        }
    }

    private fun buildPostCountSubTitle(count: Int, sortType: SortType): String {
        val countFormatArg = if (count % AppConfig.API_PAGE_SIZE == 0) "$count+" else "$count"
        val countString = resources.getQuantityString(
            R.plurals.posts_quantity,
            count,
            countFormatArg
        )
        return resources.getString(
            if (sortType == NewestFirst) {
                R.string.posts_sorting_newest_first
            } else {
                R.string.posts_sorting_oldest_first
            },
            countString
        )
    }

    private fun showEmptyLayout() {
        mainActivity?.updateTitleLayout { hideSubTitle() }
        binding.recyclerViewPosts.gone()
        binding.layoutEmptyList.apply {
            visible()
            setTitle(R.string.posts_empty_title)
            setDescription(R.string.posts_empty_description)
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemSync -> appStateViewModel.runAction(Refresh(force = true))
            R.id.menuItemSearch -> appStateViewModel.runAction(ViewSearch)
            R.id.menuItemSort -> appStateViewModel.runAction(ToggleSorting)
        }

        return true
    }
}
