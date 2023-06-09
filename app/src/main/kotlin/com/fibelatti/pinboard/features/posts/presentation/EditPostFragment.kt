package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.presentation.TagManagerViewModel
import com.fibelatti.ui.foundation.toStableList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditPostFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
) : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val editPostViewModel: EditPostViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()
    private val tagManagerViewModel: TagManagerViewModel by viewModels()

    /**
     * This hackery is needed because [androidx.activity.OnBackPressedDispatcher] is misbehaving
     * after a configuration change. In normal circumstances the callbacks are ordered correctly,
     * but after a configuration change the activity callback is at the top so it is invoked first
     * and the only way to solve it at this time is to also scope this callback to the activity.
     *
     * This would obviously result in a leak and/or crashes since the callback would outlive the
     * fragment, which is why it is manually removed when [onDestroyView] is called to prevent
     * such issues.
     */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This should be using viewLifecycleOwner instead...
        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), onBackPressedCallback)

        setThemedContent {
            EditBookmarkScreen(
                appStateViewModel = appStateViewModel,
                editPostViewModel = editPostViewModel,
                postDetailViewModel = postDetailViewModel,
                tagManagerViewModel = tagManagerViewModel,
                mainVariant = mainVariant,
            )
        }

        handleKeyboardVisibility()

        setupViewModels()
    }

    override fun onDestroyView() {
        // Needs to be manually removed because the activity is used as the lifecycleOwner
        onBackPressedCallback.remove()

        mainViewModel.updateState { currentState ->
            currentState.copy(
                actionButton = if (currentState.actionButton.id == ACTION_ID) {
                    MainState.ActionButtonComponent.Gone
                } else {
                    currentState.actionButton
                },
            )
        }

        requireView().hideKeyboard()

        super.onDestroyView()
    }

    private fun onBackPressed() {
        if (editPostViewModel.hasPendingChanges()) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setMessage(R.string.alert_confirm_unsaved_changes)
                setPositiveButton(R.string.hint_yes) { _, _ -> appStateViewModel.runAction(NavigateBack) }
                setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
            }.show()
        } else {
            appStateViewModel.runAction(NavigateBack)
        }
    }

    private fun handleKeyboardVisibility() {
        requireView().doOnApplyWindowInsets { _, insets, _, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        actionButton = MainState.ActionButtonComponent.Visible(
                            label = getString(R.string.hint_save),
                            id = ACTION_ID,
                        ),
                    )
                }
            } else {
                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
            }
        }
    }

    private fun setupViewModels() {
        setupMainViewModel()
        setupEditPostViewModel()
        setupPostDetailViewModel()
    }

    private fun setupMainViewModel() {
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { onBackPressed() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.actionButtonClicks(ACTION_ID)
            .onEach { saveLink() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.DeleteBookmark -> deletePost(post)
                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(post)
                    else -> Unit
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(ACTION_ID)
            .onEach { saveLink() }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupEditPostViewModel() {
        editPostViewModel.suggestedTags
            .onEach(tagManagerViewModel::setSuggestedTags)
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.saved
            .onEach { requireView().showBanner(getString(R.string.posts_saved_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.invalidUrlError
            .onEach { errorMessage -> if (errorMessage.isNotEmpty()) showFab() }
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.invalidUrlTitleError
            .onEach { errorMessage -> if (errorMessage.isNotEmpty()) showFab() }
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.error
            .onEach { throwable ->
                handleError(throwable, editPostViewModel::errorHandled)
                showFab()
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        editPostViewModel.postState
            .onEach { post ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getString(R.string.posts_add_title)),
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(id = ACTION_ID, icon = R.drawable.ic_close),
                        bottomAppBar = MainState.BottomAppBarComponent.Visible(
                            id = ACTION_ID,
                            menuItems = listOf(
                                MainState.MenuItemComponent.DeleteBookmark,
                                MainState.MenuItemComponent.OpenInBrowser,
                            ).toStableList(),
                            navigationIcon = null,
                            data = post,
                        ),
                        floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_done),
                    )
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupPostDetailViewModel() {
        postDetailViewModel.deleted
            .onEach { requireView().showBanner(getString(R.string.posts_deleted_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.error
            .onEach { throwable -> handleError(throwable, postDetailViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun saveLink() {
        requireView().hideKeyboard()

        mainViewModel.updateState { currentState ->
            currentState.copy(
                actionButton = MainState.ActionButtonComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }
        editPostViewModel.saveLink()
    }

    private fun deletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }.show()
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.url)))
    }

    private fun showFab() {
        mainViewModel.updateState { currentState ->
            currentState.copy(
                floatingActionButton = MainState.FabComponent.Visible(
                    id = ACTION_ID,
                    icon = R.drawable.ic_done,
                ),
            )
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "EditPostFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
