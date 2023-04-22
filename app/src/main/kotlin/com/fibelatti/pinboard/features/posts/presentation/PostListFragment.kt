package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.databinding.FragmentPostListBinding
import com.fibelatti.pinboard.features.InAppReviewManager
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
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
import kotlinx.coroutines.flow.onEach
import java.util.UUID
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

        private val ACTION_ID = UUID.randomUUID().toString()
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val postListViewModel: PostListViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()

    private val binding by viewBinding(FragmentPostListBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.supportFragmentManager?.setFragmentResultListener(
            UserPreferencesFragment.TAG,
            this,
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
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = PostQuickActions.allOptions(post),
            optionName = { option -> getString(option.title) },
            optionIcon = PostQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is PostQuickActions.ToggleReadLater -> postDetailViewModel.toggleReadLater(
                        post = option.post,
                    )
                    is PostQuickActions.Edit -> appStateViewModel.runAction(
                        action = EditPost(option.post),
                    )
                    is PostQuickActions.Delete -> deletePost(
                        post = option.post,
                    )
                    is PostQuickActions.CopyUrl -> requireContext().copyToClipboard(
                        label = post.title,
                        text = post.url,
                    )
                    is PostQuickActions.Share -> requireActivity().shareText(
                        title = R.string.posts_share_title,
                        text = option.post.url,
                    )
                    is PostQuickActions.ExpandDescription -> PostDescriptionDialog.showPostDescriptionDialog(
                        context = requireContext(),
                        post = post,
                    )
                    is PostQuickActions.OpenBrowser -> startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(option.post.url)),
                    )
                }
            },
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
            .onEach { throwable -> handleError(throwable, postListViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItemId, _) ->
                when (menuItemId) {
                    R.id.menuItemSync -> appStateViewModel.runAction(Refresh(force = true))
                    R.id.menuItemSearch -> appStateViewModel.runAction(ViewSearch)
                    R.id.menuItemSort -> appStateViewModel.runAction(ToggleSorting)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(ACTION_ID)
            .onEach { appStateViewModel.runAction(AddPost) }
            .launchInAndFlowWith(viewLifecycleOwner)

        appStateViewModel.postListContent
            .onEach(::updateContent)
            .launchInAndFlowWith(viewLifecycleOwner)

        postDetailViewModel.loading
            .onEach { binding.progressBar.isVisible = it }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.deleted
            .onEach { binding.root.showBanner(getString(R.string.posts_deleted_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.deleteError
            .onEach {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setMessage(R.string.posts_deleted_error)
                    setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                }.show()
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.updated
            .onEach { binding.root.showBanner(getString(R.string.posts_marked_as_read_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.updateError
            .onEach { binding.root.showBanner(getString(R.string.posts_marked_as_read_error)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.error
            .onEach { throwable -> handleError(throwable, postDetailViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    override fun handleError(error: Throwable?, postAction: () -> Unit) {
        binding.progressBar.isGone = true
        super.handleError(error, postAction)
    }

    private fun updateContent(content: PostListContent) {
        mainViewModel.updateState { currentState ->
            currentState.copy(
                navigation = MainState.NavigationComponent.Gone,
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    id = ACTION_ID,
                    menu = if (content.category != All || !content.canForceSync) {
                        R.menu.menu_main_no_sync
                    } else {
                        R.menu.menu_main
                    },
                    navigationIcon = R.drawable.ic_menu,
                ),
                floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_pin),
            )
        }

        when (content.shouldLoad) {
            ShouldLoadFirstPage, ShouldForceLoad -> {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getCategoryTitle(content.category)),
                        subtitle = MainState.TitleComponent.Gone,
                    )
                }

                binding.progressBar.isVisible = true
                postListViewModel.loadContent(content)
            }
            is ShouldLoadNextPage -> postListViewModel.loadContent(content)
            Syncing, Loaded -> showPosts(content)
        }

        binding.groupSearchActive.isVisible = content.searchParameters.isActive()
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
                SelectionDialog.show(
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

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(getCategoryTitle(content.category)),
                subtitle = MainState.TitleComponent.Visible(
                    buildPostCountSubTitle(content.totalCount, content.sortType),
                ),
            )
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
            countString,
        )
    }

    private fun showEmptyLayout() {
        mainViewModel.updateState { currentState ->
            currentState.copy(subtitle = MainState.TitleComponent.Gone)
        }

        binding.recyclerViewPosts.isGone = true
        binding.layoutEmptyList.apply {
            isVisible = true
            setTitle(R.string.posts_empty_title)
            setDescription(R.string.posts_empty_description)
        }
    }
}

private sealed class PostQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val post: Post

    data class ToggleReadLater(
        override val post: Post,
    ) : PostQuickActions(
        title = if (post.readLater) {
            R.string.quick_actions_remove_read_later
        } else {
            R.string.quick_actions_add_read_later
        },
        icon = R.drawable.ic_read_later,
    )

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

    data class CopyUrl(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_copy_url,
        icon = R.drawable.ic_copy,
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
            add(ToggleReadLater(post))
            add(Edit(post))
            add(Delete(post))
            add(CopyUrl(post))
            add(Share(post))

            if (post.description.isNotBlank()) {
                add(ExpandDescription(post))
            }

            add(OpenBrowser(post))
        }
    }
}
