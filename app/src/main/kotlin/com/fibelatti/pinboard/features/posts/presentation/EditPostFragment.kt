package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.clearError
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.smoothScrollY
import com.fibelatti.pinboard.databinding.FragmentEditPostBinding
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.pinboard.features.tags.presentation.TagManagerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditPostFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "EditPostFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val editPostViewModel: EditPostViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()
    private val tagManagerViewModel: TagManagerViewModel by viewModels()

    private val binding by viewBinding(FragmentEditPostBinding::bind)

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentEditPostBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This should be using viewLifecycleOwner instead...
        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), onBackPressedCallback)

        setupLayout()
        setupViewModels()
    }

    override fun onResume() {
        super.onResume()
        restoreSelection()
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

    private fun setupLayout() {
        handleKeyboardVisibility()

        binding.editTextUrl.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_NEXT) {
            binding.editTextTitle.requestFocus()
        }

        binding.togglePrivate.isVisible = mainVariant
    }

    private fun handleKeyboardVisibility() {
        binding.root.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        actionButton = MainState.ActionButtonComponent.Visible(
                            label = getString(R.string.hint_save),
                            id = ACTION_ID,
                        ),
                    )
                }

                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

                view.updatePadding(bottom = initialPadding.bottom + imeInsets.bottom)

                // In case what's below the focused view is also important
                val scrollOffset = resources.getDimensionPixelSize(R.dimen.scroll_offset)
                // We want to scroll enough for the focused view to be fully visible
                val focusedViewBottom = try {
                    requireActivity().currentFocus?.let { focusedView ->
                        val pos = IntArray(2)
                        focusedView.getLocationOnScreen(pos).run { pos[1] + focusedView.measuredHeight + scrollOffset }
                    } ?: 0
                } catch (ignored: IllegalStateException) {
                    // The activity is gone
                    return@doOnApplyWindowInsets
                }

                if (focusedViewBottom >= view.measuredHeight - imeInsets.bottom) {
                    view.smoothScrollY(focusedViewBottom)
                }
            } else {
                view.updatePadding(bottom = initialPadding.bottom)

                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
            }
        }
    }

    private fun saveLink() {
        mainViewModel.updateState { currentState ->
            currentState.copy(
                actionButton = MainState.ActionButtonComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }
        binding.root.hideKeyboard()
        editPostViewModel.saveLink()
    }

    private fun setupViewModels() {
        setupAppStateViewModel()
        setupMainViewModel()
        setupEditPostViewModel()
        setupPostDetailViewModel()

        tagManagerViewModel.state
            .onEach {
                editPostViewModel.searchForTag(it.currentQuery, it.tags)
                editPostViewModel.updatePost { post -> post.copy(tags = it.tags) }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupAppStateViewModel() {
        appStateViewModel.addPostContent
            .onEach {
                val emptyPost = Post(
                    url = "",
                    title = "",
                    description = "",
                    private = it.defaultPrivate,
                    readLater = it.defaultReadLater,
                    tags = it.defaultTags,
                )

                editPostViewModel.initializePost(emptyPost)
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        appStateViewModel.editPostContent
            .onEach { editPostViewModel.initializePost(it.post) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupMainViewModel() {
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { onBackPressed() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.actionButtonClicks(ACTION_ID)
            .onEach { saveLink() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItemId, post) ->
                if (post !is Post) return@onEach
                when (menuItemId) {
                    R.id.menuItemDelete -> deletePost(post)
                    R.id.menuItemOpenInBrowser -> openUrlInExternalBrowser(post)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.fabClicks(ACTION_ID)
            .onEach { saveLink() }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupEditPostViewModel() {
        editPostViewModel.loading
            .onEach {
                binding.layoutContent.isGone = it
                binding.progressBar.isVisible = it
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.suggestedTags
            .onEach(tagManagerViewModel::setSuggestedTags)
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.saved
            .onEach { binding.root.showBanner(getString(R.string.posts_saved_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.invalidUrlError
            .onEach(::handleInvalidUrlError)
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.invalidUrlTitleError
            .onEach(::handleInvalidTitleError)
            .launchInAndFlowWith(viewLifecycleOwner)
        editPostViewModel.error
            .onEach { throwable ->
                handleError(throwable, editPostViewModel::errorHandled)
                showFab()
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        editPostViewModel.postState
            .take(1) // The UI updates itself, so take only 1 for the initial setup
            .onEach(::showPostDetails)
            .launchInAndFlowWith(viewLifecycleOwner)

        editPostViewModel.searchForTag(tag = "", currentTags = emptyList())
    }

    private fun setupPostDetailViewModel() {
        postDetailViewModel.loading
            .onEach {
                binding.layoutContent.isGone = it
                binding.progressBar.isVisible = it
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.deleted
            .onEach { binding.root.showBanner(getString(R.string.posts_deleted_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        postDetailViewModel.error
            .onEach { throwable -> handleError(throwable, postDetailViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun showPostDetails(post: Post) {
        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(getString(R.string.posts_add_title)),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(id = ACTION_ID, icon = R.drawable.ic_close),
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    id = ACTION_ID,
                    menu = R.menu.menu_details,
                    navigationIcon = null,
                    data = post,
                ),
                floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_done),
            )
        }

        with(post) {
            with(binding) {
                textViewPendingSync.isVisible = pendingSync != null
                when (pendingSync) {
                    PendingSync.ADD -> textViewPendingSync.setText(R.string.posts_pending_add_expanded)
                    PendingSync.UPDATE -> textViewPendingSync.setText(R.string.posts_pending_update_expanded)
                    PendingSync.DELETE -> textViewPendingSync.setText(R.string.posts_pending_delete_expanded)
                    null -> Unit
                }

                editTextUrl.setText(url)
                editTextTitle.setText(title)
                editTextDescription.setText(description)
                restoreSelection()

                togglePrivate.isActive = private
                toggleReadLater.isActive = readLater

                tags?.let(tagManagerViewModel::setTags)
            }
        }

        setPostChangedListeners()

        editPostViewModel.searchForTag(tag = "", currentTags = post.tags.orEmpty())
    }

    private fun restoreSelection() = with(binding) {
        if (editTextUrl.hasFocus()) {
            editTextUrl.setSelection(editTextUrl.length())
        }
        if (editTextTitle.hasFocus()) {
            editTextTitle.setSelection(editTextTitle.length())
        }
        if (editTextDescription.hasFocus()) {
            editTextDescription.setSelection(editTextDescription.length())
        }
    }

    private fun setPostChangedListeners() {
        binding.editTextUrl.doAfterTextChanged { editable ->
            editPostViewModel.updatePost { post -> post.copy(url = editable.toString()) }
        }
        binding.editTextTitle.doAfterTextChanged { editable ->
            editPostViewModel.updatePost { post -> post.copy(title = editable.toString()) }
        }
        binding.editTextDescription.doAfterTextChanged { editable ->
            editPostViewModel.updatePost { post -> post.copy(description = editable.toString()) }
        }
        binding.togglePrivate.setOnChangedListener { newValue ->
            editPostViewModel.updatePost { post -> post.copy(private = newValue) }
        }
        binding.toggleReadLater.setOnChangedListener { newValue ->
            editPostViewModel.updatePost { post -> post.copy(readLater = newValue) }
        }
        binding.composeViewTagManager.setContent {
            AppTheme {
                TagManager(tagManagerViewModel = tagManagerViewModel)
            }
        }
    }

    private fun handleMenuClick(item: MenuItem?, post: Post): Boolean {
        when (item?.itemId) {
            R.id.menuItemDelete -> deletePost(post)
            R.id.menuItemOpenInBrowser -> openUrlInExternalBrowser(post)
        }

        return true
    }

    private fun deletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }.show()
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(post.url) })
    }

    private fun handleInvalidUrlError(message: String) {
        message.takeIf(String::isNotEmpty)
            ?.let {
                binding.textInputLayoutUrl.showError(it)
                showFab()
            }
            ?: binding.textInputLayoutUrl.clearError()
    }

    private fun handleInvalidTitleError(message: String) {
        message.takeIf(String::isNotEmpty)
            ?.let {
                binding.textInputLayoutTitle.showError(it)
                showFab()
            }
            ?: binding.textInputLayoutTitle.clearError()
    }

    private fun showFab() {
        mainViewModel.updateState { currentState ->
            currentState.copy(floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_done))
        }
    }
}
