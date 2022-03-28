package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.clearError
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.invisible
import com.fibelatti.core.extension.orZero
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.smoothScrollY
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentEditPostBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditPostFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "EditPostFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val editPostViewModel: EditPostViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()

    private var binding by viewBinding<FragmentEditPostBinding>()

    private var isRecreating: Boolean = false

    private var originalPost: Post? = null

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
    ): View = FragmentEditPostBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRecreating = savedInstanceState != null

        // This should be using viewLifecycleOwner instead...
        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), onBackPressedCallback)

        setupLayout()
        setupViewModels()
    }

    override fun onDestroyView() {
        // Needs to be manually removed because the activity is used as the lifecycleOwner
        onBackPressedCallback.remove()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.updateTitleLayout {
            hideActionButton()
        }
        mainActivity?.updateViews { bottomAppBar, _ ->
            bottomAppBar.hideKeyboard()
            bottomAppBar.isVisible = true
        }
    }

    private fun onBackPressed() {
        val isContentUnchanged = originalPost?.run {
            with(binding.layoutAddPost) {
                hash.isNotEmpty() &&
                    url == editTextUrl.textAsString() &&
                    title == editTextTitle.textAsString() &&
                    description == editTextDescription.textAsString() &&
                    private == togglePrivate.isActive &&
                    readLater == toggleReadLater.isActive &&
                    tags == binding.layoutAddTags.getTags().takeIf { it.isNotEmpty() }
            }
        } ?: true

        if (isContentUnchanged) {
            appStateViewModel.runAction(NavigateBack)
        } else {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setMessage(R.string.alert_confirm_unsaved_changes)
                setPositiveButton(R.string.hint_yes) { _, _ ->
                    appStateViewModel.runAction(NavigateBack)
                }
                setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
            }.show()
        }
    }

    private fun setupLayout() {
        handleKeyboardVisibility()
        binding.layoutAddTags.setup(
            afterTagInput = editPostViewModel::searchForTag,
            onTagRemoved = editPostViewModel::searchForTag
        )
    }

    private fun handleKeyboardVisibility() {
        binding.root.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                mainActivity?.updateTitleLayout { setActionButton(R.string.hint_save, ::saveLink) }

                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

                ViewCompat.setPaddingRelative(
                    view,
                    initialPadding.start,
                    initialPadding.top,
                    initialPadding.end,
                    initialPadding.bottom + imeInsets.bottom,
                )

                // In case what's below the focused view is also important
                val scrollOffset = resources.getDimensionPixelSize(R.dimen.scroll_offset)
                // We want to scroll enough for the focused view to be fully visible
                val focusedViewBottom = try {
                    requireActivity().currentFocus?.let { focusedView ->
                        val pos = IntArray(2)
                        focusedView.getLocationOnScreen(pos).run { pos[1] + focusedView.measuredHeight + scrollOffset }
                    }.orZero()
                } catch (ignored: IllegalStateException) {
                    // The activity is gone
                    return@doOnApplyWindowInsets
                }

                if (focusedViewBottom >= view.measuredHeight - imeInsets.bottom) {
                    view.smoothScrollY(focusedViewBottom)
                }
            } else {
                ViewCompat.setPaddingRelative(
                    view,
                    initialPadding.start,
                    initialPadding.top,
                    initialPadding.end,
                    initialPadding.bottom
                )

                mainActivity?.updateTitleLayout { hideActionButton() }
            }
        }
    }

    private fun saveLink() {
        mainActivity?.updateTitleLayout {
            hideActionButton()
        }
        mainActivity?.updateViews { _, fab ->
            fab.hide()
        }
        binding.root.hideKeyboard()

        with(binding.layoutAddPost) {
            editPostViewModel.saveLink(
                editTextUrl.textAsString(),
                editTextTitle.textAsString(),
                editTextDescription.textAsString(),
                togglePrivate.isActive,
                toggleReadLater.isActive,
                binding.layoutAddTags.getTags()
            )
        }
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.addPostContent.collect {
                setupActivityViews()

                if (!isRecreating) {
                    binding.layoutAddPost.togglePrivate.isActive = it.defaultPrivate
                    binding.layoutAddPost.toggleReadLater.isActive = it.defaultReadLater
                    binding.layoutAddTags.showTags(it.defaultTags)
                }
            }
        }
        lifecycleScope.launch {
            appStateViewModel.editPostContent.collect(::showPostDetails)
        }
        lifecycleScope.launch {
            editPostViewModel.loading.collect {
                binding.layoutProgressBar.root.isVisible = it
            }
        }
        lifecycleScope.launch {
            editPostViewModel.suggestedTags.collect {
                binding.layoutAddTags.showSuggestedValuesAsTags(it, showRemoveIcon = false)
            }
        }
        lifecycleScope.launch {
            editPostViewModel.saved.collect { mainActivity?.showBanner(getString(R.string.posts_saved_feedback)) }
        }
        lifecycleScope.launch {
            editPostViewModel.invalidUrlError.collect(::handleInvalidUrlError)
        }
        lifecycleScope.launch {
            editPostViewModel.invalidUrlTitleError.collect(::handleInvalidTitleError)
        }
        lifecycleScope.launch {
            editPostViewModel.error.collect {
                handleError(it)
                showFab()
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.loading.collect {
                binding.layoutProgressBar.root.isVisible = it
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.deleted.collect {
                mainActivity?.showBanner(getString(R.string.posts_deleted_feedback))
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.error.collect(::handleError)
        }
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.posts_add_title)
            hideSubTitle()
            setNavigateUp(R.drawable.ic_close) { onBackPressed() }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            // Using invisible() instead of gone() otherwise the fab will misbehave when
            // starting this fragment from share
            bottomAppBar.invisible()
            fab.run {
                setImageResource(R.drawable.ic_done)
                setOnClickListener { saveLink() }
                show()
            }
        }
    }

    private fun showPostDetails(content: EditPostContent) {
        setupActivityViews()
        mainActivity?.updateViews { bottomAppBar, _ ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_details)
                setOnMenuItemClickListener { item -> handleMenuClick(item, content.post) }
                isVisible = true
                show()
            }
        }

        originalPost = content.post

        if (isRecreating) {
            return
        }

        with(content.post) {
            with(binding.layoutAddPost) {
                editTextUrl.setText(url)
                editTextTitle.setText(title)
                editTextDescription.setText(description)
                togglePrivate.isActive = private
                toggleReadLater.isActive = readLater
            }

            tags?.let(binding.layoutAddTags::showTags)
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
                binding.layoutAddPost.textInputLayoutUrl.showError(it)
                showFab()
            }
            ?: binding.layoutAddPost.textInputLayoutUrl.clearError()
    }

    private fun handleInvalidTitleError(message: String) {
        message.takeIf(String::isNotEmpty)
            ?.let {
                binding.layoutAddPost.textInputLayoutTitle.showError(it)
                showFab()
            }
            ?: binding.layoutAddPost.textInputLayoutTitle.clearError()
    }

    private fun showFab() {
        mainActivity?.updateViews { _, fab -> fab.show() }
    }
}
