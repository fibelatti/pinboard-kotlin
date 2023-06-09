package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.foundation.toStableList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()
    private val popularPostsViewModel: PopularPostsViewModel by viewModels()

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
                actionButton = if (currentState.actionButton.id == ACTION_ID) {
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
                    currentState.copy(
                        title = MainState.TitleComponent.Gone,
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                        actionButton = if (post.readLater && !post.isFile()) {
                            MainState.ActionButtonComponent.Visible(
                                id = ACTION_ID,
                                label = getString(R.string.hint_mark_as_read),
                                data = post,
                            )
                        } else {
                            MainState.ActionButtonComponent.Gone
                        },
                        bottomAppBar = MainState.BottomAppBarComponent.Visible(
                            id = ACTION_ID,
                            menuItems = menuItems.toStableList(),
                            navigationIcon = null,
                            data = post,
                        ),
                        floatingActionButton = MainState.FabComponent.Visible(
                            id = ACTION_ID,
                            icon = R.drawable.ic_share,
                            data = post,
                        ),
                    )
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupMainViewModel() {
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.actionButtonClicks(ACTION_ID)
            .onEach { data: Any? -> (data as? Post)?.let(postDetailViewModel::toggleReadLater) }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.DeleteBookmark -> deletePost(post)
                    is MainState.MenuItemComponent.EditBookmark -> appStateViewModel.runAction(EditPost(post))
                    is MainState.MenuItemComponent.SaveBookmark -> popularPostsViewModel.saveLink(post)
                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(post)
                    else -> Unit
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(ACTION_ID)
            .onEach { data: Any? ->
                (data as? Post)?.let { requireActivity().shareText(R.string.posts_share_title, it.url) }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupPostDetailViewModel() {
        postDetailViewModel.deleted
            .onEach { requireView().showBanner(getString(R.string.posts_deleted_feedback)) }
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
            .onEach {
                requireView().showBanner(getString(R.string.posts_marked_as_read_feedback))
                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.updateError
            .onEach { requireView().showBanner(getString(R.string.posts_marked_as_read_error)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.error
            .onEach { throwable -> handleError(throwable, postDetailViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupPopularPostsViewModel() {
        popularPostsViewModel.saved
            .onEach { requireView().showBanner(getString(R.string.posts_saved_feedback)) }
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
        }.show()
    }

    companion object {

        @JvmStatic
        val TAG: String = "PostDetailFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
