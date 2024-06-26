package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import com.fibelatti.bookmarking.features.appstate.EditPost
import com.fibelatti.bookmarking.features.appstate.PopularPostDetailContent
import com.fibelatti.bookmarking.features.appstate.PostDetailContent
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.randomUUID
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostDetailFragment : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModel()
    private val mainViewModel: MainViewModel by activityViewModel()
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val popularPostsViewModel: PopularPostsViewModel by viewModel()

    private val actionId = randomUUID()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            BookmarkDetailsScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                postDetailViewModel = postDetailViewModel,
                popularPostsViewModel = popularPostsViewModel,
                onOpenInFileViewerClicked = ::openUrlInFileViewer,
                onOpenInBrowserClicked = ::openUrlInExternalBrowser,
            )
        }

        setupViewModels()
    }

    override fun onDestroyView() {
        mainViewModel.updateState { currentState ->
            currentState.copy(
                actionButton = if (currentState.actionButton.id == actionId) {
                    MainState.ActionButtonComponent.Gone
                } else {
                    currentState.actionButton
                },
            )
        }

        super.onDestroyView()
    }

    private fun openUrlInFileViewer(post: Post) {
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(post.url.substringAfterLast("."))
        val newIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(post.url), mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(newIntent)
        } catch (ignored: ActivityNotFoundException) {
            requireView().showBanner(getString(R.string.posts_open_with_file_viewer_error))
        }
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(post.url) })
    }

    private fun setupViewModels() {
        setupAppStateViewModel()
        setupMainViewModel()
        setupPostDetailViewModel()
        setupPopularPostsViewModel()
    }

    private fun setupAppStateViewModel() {
        appStateViewModel.content
            .onEach { content ->
                val (post, menuItems) = when (content) {
                    is PostDetailContent -> content.post to listOf(
                        MainState.MenuItemComponent.DeleteBookmark,
                        MainState.MenuItemComponent.EditBookmark,
                        MainState.MenuItemComponent.OpenInBrowser,
                    )

                    is PopularPostDetailContent -> content.post to listOf(
                        MainState.MenuItemComponent.SaveBookmark,
                        MainState.MenuItemComponent.OpenInBrowser,
                    )

                    else -> return@onEach
                }

                mainViewModel.updateState { currentState ->
                    val actionButtonState = if (post.readLater == true && !post.isFile()) {
                        MainState.ActionButtonComponent.Visible(
                            id = actionId,
                            label = getString(R.string.hint_mark_as_read),
                            data = post,
                        )
                    } else {
                        MainState.ActionButtonComponent.Gone
                    }

                    if (currentState.multiPanelEnabled) {
                        currentState.copy(
                            actionButton = actionButtonState,
                            sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                                id = actionId,
                                menuItems = listOf(
                                    MainState.MenuItemComponent.ShareBookmark,
                                    *menuItems.toTypedArray(),
                                    MainState.MenuItemComponent.CloseSidePanel,
                                ),
                                data = post,
                            ),
                        )
                    } else {
                        currentState.copy(
                            title = MainState.TitleComponent.Gone,
                            subtitle = MainState.TitleComponent.Gone,
                            navigation = MainState.NavigationComponent.Visible(actionId),
                            actionButton = actionButtonState,
                            bottomAppBar = MainState.BottomAppBarComponent.Visible(
                                id = actionId,
                                menuItems = menuItems,
                                navigationIcon = null,
                                data = post,
                            ),
                            floatingActionButton = MainState.FabComponent.Visible(
                                id = actionId,
                                icon = R.drawable.ic_share,
                                data = post,
                            ),
                        )
                    }
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupMainViewModel() {
        mainViewModel.navigationClicks(actionId)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.actionButtonClicks(actionId)
            .onEach { data: Any? -> (data as? Post)?.let(postDetailViewModel::toggleReadLater) }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.menuItemClicks(actionId)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.ShareBookmark -> shareBookmarkUrl(post)
                    is MainState.MenuItemComponent.DeleteBookmark -> deletePost(post)
                    is MainState.MenuItemComponent.EditBookmark -> appStateViewModel.runAction(EditPost(post))
                    is MainState.MenuItemComponent.SaveBookmark -> popularPostsViewModel.saveLink(post)
                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(post)
                    is MainState.MenuItemComponent.CloseSidePanel -> navigateBack()
                    else -> Unit
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(actionId)
            .onEach { data: Any? -> (data as? Post)?.let(::shareBookmarkUrl) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun shareBookmarkUrl(post: Post) {
        requireActivity().shareText(R.string.posts_share_title, post.url)
    }

    private fun setupPostDetailViewModel() {
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
                        }.applySecureFlag().show()
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

    private fun setupPopularPostsViewModel() {
        popularPostsViewModel.screenState
            .onEach { state ->
                state.savedMessage?.let { messageRes ->
                    requireView().showBanner(getString(messageRes))
                    popularPostsViewModel.userNotified()
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        popularPostsViewModel.error
            .onEach { throwable -> handleError(throwable, popularPostsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun deletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }.applySecureFlag().show()
    }

    companion object {

        @JvmStatic
        val TAG: String = "PostDetailFragment"
    }
}
