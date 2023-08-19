package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.ToggleSorting
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.ui.foundation.toStableList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PostListFragment @Inject constructor(
    private val userRepository: UserRepository,
) : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val postListViewModel: PostListViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            BookmarkListScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                postListViewModel = postListViewModel,
                postDetailViewModel = postDetailViewModel,
                onPostLongClicked = ::showQuickActionsDialog,
                onShareClicked = ::shareFilteredResults,
            )
        }

        setupViewModels()
    }

    private fun showQuickActionsDialog(post: Post) {
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = PostQuickActions.allOptions(post).toStableList(),
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
                    options = ShareSearchOption.values().toStableList(),
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

    private fun setupViewModels() {
        appStateViewModel.content
            .combine(mainViewModel.state) { content, mainState ->
                when (content) {
                    is PostListContent -> content
                    is PostDetailContent -> content.previousContent.takeIf { mainState.multiPanelEnabled }
                    else -> null
                }
            }
            .filterNotNull()
            .onEach { content ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getCategoryTitle(content.category)),
                        subtitle = when {
                            content.posts == null && content.shouldLoad is Loaded -> MainState.TitleComponent.Gone
                            else -> MainState.TitleComponent.Visible(
                                buildPostCountSubTitle(content.totalCount, content.sortType),
                            )
                        },
                        navigation = MainState.NavigationComponent.Gone,
                        bottomAppBar = MainState.BottomAppBarComponent.Visible(
                            id = ACTION_ID,
                            menuItems = buildList {
                                add(MainState.MenuItemComponent.SearchBookmarks)
                                add(MainState.MenuItemComponent.SortBookmarks)

                                if (content.category == All && content.canForceSync) {
                                    add(MainState.MenuItemComponent.SyncBookmarks)
                                }
                            }.toStableList(),
                            navigationIcon = R.drawable.ic_menu,
                        ),
                        floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_pin),
                    )
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItem, _) ->
                val action = when (menuItem) {
                    is MainState.MenuItemComponent.SearchBookmarks -> ViewSearch
                    is MainState.MenuItemComponent.SortBookmarks -> ToggleSorting
                    is MainState.MenuItemComponent.SyncBookmarks -> Refresh(force = true)
                    else -> return@onEach
                }

                appStateViewModel.runAction(action)
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(ACTION_ID)
            .onEach { appStateViewModel.runAction(AddPost) }
            .launchInAndFlowWith(viewLifecycleOwner)

        postListViewModel.error
            .onEach { throwable -> handleError(throwable, postListViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)

        postDetailViewModel.screenState
            .onEach { state ->
                when {
                    state.deleted is Success<Boolean> && state.deleted.value -> {
                        requireView().showBanner(getString(R.string.posts_deleted_feedback))
                        postDetailViewModel.userNotified()
                    }

                    state.deleted is Failure -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setMessage(R.string.posts_deleted_error)
                            setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                        }.show()
                    }

                    state.updated is Success<Boolean> && state.updated.value -> {
                        requireView().showBanner(getString(R.string.posts_marked_as_read_feedback))
                        postDetailViewModel.userNotified()
                        mainViewModel.updateState { currentState ->
                            currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                        }
                    }

                    state.updated is Failure -> {
                        requireView().showBanner(getString(R.string.posts_marked_as_read_error))
                        postDetailViewModel.userNotified()
                    }
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        postDetailViewModel.error
            .onEach { throwable -> handleError(throwable, postDetailViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun getCategoryTitle(category: ViewCategory): String = when (category) {
        All -> getString(R.string.posts_title_all)
        Recent -> getString(R.string.posts_title_recent)
        Public -> getString(R.string.posts_title_public)
        Private -> getString(R.string.posts_title_private)
        Unread -> getString(R.string.posts_title_unread)
        Untagged -> getString(R.string.posts_title_untagged)
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

    companion object {

        @JvmStatic
        val TAG: String = "PostListFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
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
        title = if (post.readLater == true) {
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
