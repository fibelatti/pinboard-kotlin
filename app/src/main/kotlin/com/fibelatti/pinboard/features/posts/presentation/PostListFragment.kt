package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.databinding.FragmentPostListBinding
import com.fibelatti.pinboard.features.BottomBarHost.Companion.bottomBarHost
import com.fibelatti.pinboard.features.InAppReviewManager
import com.fibelatti.pinboard.features.TitleLayoutHost.Companion.titleLayoutHost
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
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
import com.fibelatti.pinboard.features.appstate.SearchParameters
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
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PostListFragment @Inject constructor(
    private val postsAdapter: PostListAdapter,
    private val inAppReviewManager: InAppReviewManager,
    private val userRepository: UserRepository,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PostListFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val postListViewModel: PostListViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()

    private val binding by viewBinding(FragmentPostListBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.supportFragmentManager?.setFragmentResultListener(
            UserPreferencesFragment.TAG,
            this
        ) { _, _ -> activity?.let(inAppReviewManager::checkForPlayStoreReview) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentPostListBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.root.animateChangingTransitions()

        binding.buttonFilterClear.setOnClickListener {
            appStateViewModel.runAction(ClearSearch)
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
                    binding.progressBar.isVisible = true
                    appStateViewModel.runAction(GetNextPostPage)
                }
            }
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = postsAdapter

        postsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
        postsAdapter.onItemLongClicked = ::showQuickActionsDialog
        postsAdapter.onTagClicked = { appStateViewModel.runAction(PostsForTag(it)) }
    }

    private fun showQuickActionsDialog(post: Post) {
        SelectionDialog.showSelectionDialog(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = PostQuickActions.allOptions(post),
            optionName = { option -> getString(option.title) },
            optionIcon = PostQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is PostQuickActions.Edit -> appStateViewModel.runAction(EditPost(option.post))
                    is PostQuickActions.Delete -> deletePost(option.post)
                    is PostQuickActions.Share -> requireActivity().shareText(
                        R.string.posts_share_title,
                        option.post.url,
                    )
                    is PostQuickActions.ExpandDescription -> PostDescriptionDialog.showPostDescriptionDialog(
                        context = requireContext(),
                        post = post,
                    )
                    is PostQuickActions.OpenBrowser -> startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(option.post.url))
                    )
                }
            }
        )
    }

    private fun deletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }.show()
    }

    private fun setupViewModels() {
        postListViewModel.error
            .onEach(::handleError)
            .launchIn(lifecycleScope)

        appStateViewModel.postListContent
            .onEach(::updateContent)
            .launchIn(lifecycleScope)

        postDetailViewModel.loading
            .onEach { binding.progressBar.isVisible = it }
            .launchIn(lifecycleScope)
        postDetailViewModel.deleted
            .onEach { binding.root.showBanner(getString(R.string.posts_deleted_feedback)) }
            .launchIn(lifecycleScope)
        postDetailViewModel.deleteError
            .onEach {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setMessage(R.string.posts_deleted_error)
                    setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                }.show()
            }
            .launchIn(lifecycleScope)
        postDetailViewModel.error
            .onEach(::handleError)
            .launchIn(lifecycleScope)
    }

    override fun handleError(error: Throwable) {
        super.handleError(error)
        binding.progressBar.isGone = true
    }

    private fun updateContent(content: PostListContent) {
        titleLayoutHost.update { hideNavigateUp() }
        bottomBarHost.update { bottomAppBar, fab ->
            bottomAppBar.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(R.menu.menu_main)
                if (content.category != All || !content.canForceSync) {
                    menu.removeItem(R.id.menuItemSync)
                }
                setOnMenuItemClickListener(::handleMenuClick)
                isVisible = true
                show()
            }
            fab.run {
                setImageResource(R.drawable.ic_pin)
                setOnClickListener { appStateViewModel.runAction(AddPost) }
                show()
            }
        }

        when (content.shouldLoad) {
            ShouldLoadFirstPage, ShouldForceLoad -> {
                titleLayoutHost.update {
                    setTitle(getCategoryTitle(content.category))
                    hideSubTitle()
                }

                binding.progressBar.isVisible = true
                postListViewModel.loadContent(content)
            }
            is ShouldLoadNextPage -> postListViewModel.loadContent(content)
            Syncing, Loaded -> showPosts(content)
        }

        binding.layoutSearchActive.isVisible = content.searchParameters.isActive()
        binding.buttonFilterShare.setOnClickListener { shareFilteredResults(content.searchParameters) }
    }

    private fun shareFilteredResults(searchParameters: SearchParameters) {
        val username = userRepository.getUsername()
        val queryUrl = "$PINBOARD_USER_URL$username?query=${searchParameters.term}"
        val tagsUrl = "$PINBOARD_USER_URL$username/${searchParameters.tags.joinToString { "t:${it.name}/" }}"

        when {
            searchParameters.term.isNotBlank() && searchParameters.tags.isEmpty() -> {
                requireActivity().shareText(R.string.search_share_title, queryUrl)
            }
            searchParameters.term.isBlank() && searchParameters.tags.isNotEmpty() -> {
                requireActivity().shareText(R.string.search_share_title, tagsUrl)
            }
            else -> {
                SelectionDialog.showSelectionDialog(
                    context = requireContext(),
                    title = getString(R.string.search_share_title),
                    options = ShareSearchOption.values().toList(),
                    optionName = { option ->
                        when (option) {
                            ShareSearchOption.QUERY -> getString(R.string.search_share_query)
                            ShareSearchOption.TAGS -> getString(R.string.search_share_tags)
                        }
                    },
                    onOptionSelected = { option ->
                        val url = when (option) {
                            ShareSearchOption.QUERY -> queryUrl
                            ShareSearchOption.TAGS -> tagsUrl
                        }
                        requireActivity().shareText(R.string.search_share_title, url)
                    },
                )
            }
        }
    }

    private fun getCategoryTitle(category: ViewCategory): String = when (category) {
        All -> getString(R.string.posts_title_all)
        Recent -> getString(R.string.posts_title_recent)
        Public -> getString(R.string.posts_title_public)
        Private -> getString(R.string.posts_title_private)
        Unread -> getString(R.string.posts_title_unread)
        Untagged -> getString(R.string.posts_title_untagged)
    }

    private fun showPosts(content: PostListContent) {
        binding.progressBar.isGone = content.shouldLoad == Loaded
        binding.recyclerViewPosts.onRequestNextPageCompleted()

        titleLayoutHost.update {
            setTitle(getCategoryTitle(content.category))
            setSubTitle(buildPostCountSubTitle(content.totalCount, content.sortType))
        }

        if (content.posts == null && content.shouldLoad == Loaded) {
            showEmptyLayout()
        } else if (content.posts != null) {
            postsAdapter.showDescription = content.showDescription
            if (!content.posts.alreadyDisplayed || postsAdapter.itemCount == 0) {
                binding.recyclerViewPosts.isVisible = true
                binding.layoutEmptyList.isGone = true

                postsAdapter.submitList(content.posts.list, content.posts.diffResult)
                appStateViewModel.runAction(PostsDisplayed)
            }
        }

        activity?.reportFullyDrawn()
    }

    private fun buildPostCountSubTitle(count: Int, sortType: SortType): String {
        val countFormatArg = if (count % AppConfig.API_PAGE_SIZE == 0) "$count+" else "$count"
        val countString = resources.getQuantityString(R.plurals.posts_quantity, count, countFormatArg)
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
        titleLayoutHost.update { hideSubTitle() }
        binding.recyclerViewPosts.isGone = true
        binding.layoutEmptyList.apply {
            isVisible = true
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

private sealed class PostQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val post: Post

    data class Edit(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_edit,
        icon = R.drawable.ic_edit,
    )

    data class Delete(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_delete,
        icon = R.drawable.ic_delete,
    )

    data class Share(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_share,
        icon = R.drawable.ic_share,
    )

    data class ExpandDescription(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_expand_description,
        icon = R.drawable.ic_expand,
    )

    data class OpenBrowser(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_open_in_browser,
        icon = R.drawable.ic_open_in_browser,
    )

    companion object {

        fun allOptions(
            post: Post,
        ): List<PostQuickActions> = buildList {
            add(Edit(post))
            add(Delete(post))
            add(Share(post))

            if (post.description.isNotBlank()) {
                add(ExpandDescription(post))
            }

            add(OpenBrowser(post))
        }
    }
}
