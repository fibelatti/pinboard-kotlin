package com.fibelatti.pinboard.features.posts.presentation

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.afterTextChanged
import com.fibelatti.core.extension.clearError
import com.fibelatti.core.extension.clearText
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.invisible
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.orZero
import com.fibelatti.core.extension.setOnClickListener
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.showStyledDialog
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.toast
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.activityViewModel
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.viewModel
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import kotlinx.android.synthetic.main.fragment_edit_post.*
import kotlinx.android.synthetic.main.layout_edit_description.*
import kotlinx.android.synthetic.main.layout_edit_post.*
import kotlinx.android.synthetic.main.layout_edit_tags.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class EditPostFragment @Inject constructor() : BaseFragment(R.layout.fragment_edit_post) {

    companion object {
        @JvmStatic
        val TAG: String = "EditPostFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val editPostViewModel by viewModel { viewModelProvider.editPostViewModel() }
    private val postDetailViewModel by viewModel { viewModelProvider.postDetailsViewModel() }

    private var initialInsetBottomValue = -1

    /**
     * Saved since different flavours have different visibilities
     */
    private var checkBoxPrivateVisibility: Int = View.VISIBLE

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
            bottomAppBar.visible()
        }
    }

    private fun onBackPressed() {
        val isContentUnchanged = originalPost?.run {
            url == editTextUrl.textAsString() &&
                title == editTextTitle.textAsString() &&
                description == editTextDescription.textAsString() &&
                private == checkboxPrivate.isChecked &&
                readLater == checkboxReadLater.isChecked &&
                tags == chipGroupTags.getAllTags().takeIf { it.isNotEmpty() }
        } ?: true

        if (isContentUnchanged) {
            appStateViewModel.runAction(NavigateBack)
        } else {
            context?.showStyledDialog(
                dialogStyle = R.style.AppTheme_AlertDialog,
                dialogBackground = R.drawable.background_contrast_rounded
            ) {
                setMessage(R.string.alert_confirm_unsaved_changes)
                setPositiveButton(R.string.hint_yes) { _, _ ->
                    appStateViewModel.runAction(NavigateBack)
                }
                setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
            }
        }
    }

    private fun setupLayout() {
        handleKeyboardVisibility()
        setupDescriptionLayouts()
        setupTagLayouts()
    }

    @Suppress("MagicNumber")
    private fun handleKeyboardVisibility() {
        layoutRoot.doOnApplyWindowInsets { view, windowInsets, _, _ ->
            // This is the first pass, just save the initial inset value
            if (initialInsetBottomValue == -1) {
                initialInsetBottomValue = windowInsets.systemWindowInsetBottom
                return@doOnApplyWindowInsets
            }

            val keyboardWillAppear = windowInsets.systemWindowInsetBottom > initialInsetBottomValue
            if (keyboardWillAppear) {
                mainActivity?.updateTitleLayout {
                    handler?.postDelayed({
                        // Has to be delayed because keyboard is still appearing
                        setActionButton(R.string.hint_save, ::saveLink)
                    }, 100L)
                }

                // In case what's below the focused view is also important
                val scrollOffset = resources.getDimensionPixelSize(R.dimen.scroll_offset)
                // We want to scroll enough for the focused view to be fully visible
                val focusedViewBottom = try {
                    requireActivity().currentFocus?.let { focusedView ->
                        val pos = IntArray(2)
                        focusedView.getLocationOnScreen(pos).run {
                            pos[1] + focusedView.measuredHeight + scrollOffset
                        }
                    }.orZero()
                } catch (ignored: IllegalStateException) {
                    // The activity is gone
                    return@doOnApplyWindowInsets
                }

                if (focusedViewBottom <= windowInsets.systemWindowInsetBottom) {
                    // View is already fully visible, there's no need to scroll
                    return@doOnApplyWindowInsets
                }

                // Finally animate
                ObjectAnimator.ofInt(view, "scrollY", focusedViewBottom)
                    .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                    .apply {
                        // Has to be delayed because keyboard is still appearing
                        startDelay = 100L
                    }
                    .start()
            } else {
                mainActivity?.updateTitleLayout {
                    hideActionButton()
                }
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
        layoutRoot?.hideKeyboard()

        editPostViewModel.saveLink(
            editTextUrl.textAsString(),
            editTextTitle.textAsString(),
            editTextDescription.textAsString(),
            checkboxPrivate.isChecked,
            checkboxReadLater.isChecked,
            chipGroupTags.getAllTags()
        )
    }

    private fun setupDescriptionLayouts() {
        checkBoxPrivateVisibility = checkboxPrivate.visibility

        buttonEditDescription.setText(
            if (editTextDescription.textAsString().isBlank()) {
                R.string.posts_add_url_add_description
            } else {
                R.string.posts_add_url_edit_description
            }
        )
        buttonEditDescription.setOnClickListener { focusOnDescription() }
    }

    private fun focusOnDescription() {
        textInputLayoutUrl.gone()
        textInputLayoutTitle.gone()
        textInputUrlDescription.visible()
        checkboxPrivate.gone()
        checkboxReadLater.gone()
        layoutAddTags.gone()

        buttonEditDescription.setOnClickListener(R.string.hint_done) { dismissDescriptionFocus() }
        hideFab()
    }

    private fun dismissDescriptionFocus() {
        textInputLayoutUrl.visible()
        textInputLayoutTitle.visible()
        textInputUrlDescription.gone()
        checkboxPrivate.visibility = checkBoxPrivateVisibility
        checkboxReadLater.visible()
        layoutAddTags.visible()

        setupDescriptionLayouts()
        showFab()
        delayedHideKeyboard()
    }

    private fun setupTagLayouts() {
        setupTagInput()
        buttonTagsAdd.setOnClickListener {
            editTextTags.textAsString().takeIf(String::isNotBlank)?.let {
                chipGroupTags.addValue(it, index = 0)
                editTextTags.clearText()
            }
        }

        chipGroupTags.onTagChipRemoved = {
            editTextTags.textAsString().takeIf(String::isNotBlank)?.let {
                editPostViewModel.searchForTag(it, chipGroupTags.getAllTags())
            }
        }

        chipGroupSuggestedTags.onTagChipClicked = {
            chipGroupTags.addTag(it, index = 0)
            editTextTags.clearText()
        }
    }

    private fun delayedHideKeyboard() {
        // Dirty hack because of animateLayoutChanges:
        // Without this the underlying fragment becomes visible for a split second where the keyboard
        // was because this fragment is still resizing
        Handler().postDelayed(
            { layoutRoot?.hideKeyboard() },
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        )
    }

    private fun setupTagInput() {
        editTextTags.afterTextChanged { text ->
            when {
                text.isNotBlank() && text.endsWith(" ") -> {
                    chipGroupTags.addValue(text, index = 0)
                    editTextTags.clearText()
                }
                text.isNotBlank() -> editPostViewModel.searchForTag(text, chipGroupTags.getAllTags())
                else -> chipGroupSuggestedTags.removeAllViews()
            }
        }
        editTextTags.onKeyboardSubmit {
            when (val text = textAsString().trim()) {
                "" -> hideKeyboard()
                else -> {
                    chipGroupTags.addValue(text, index = 0)
                    editTextTags.clearText()
                }
            }
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.addPostContent) {
            setupActivityViews()
            textInputUrlDescription.visibleIf(it.showDescription)
            buttonEditDescription.goneIf(it.showDescription)

            if (!isRecreating) {
                checkboxPrivate.isChecked = it.defaultPrivate
                checkboxReadLater.isChecked = it.defaultReadLater
            }
        }
        viewLifecycleOwner.observe(appStateViewModel.editPostContent, ::showPostDetails)
        with(editPostViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observe(suggestedTags) { tags ->
                chipGroupSuggestedTags.removeAllViews()
                tags.forEach { chipGroupSuggestedTags.addValue(it, showRemoveIcon = false) }
            }
            viewLifecycleOwner.observeEvent(saved) { mainActivity?.toast(getString(R.string.posts_saved_feedback)) }
            viewLifecycleOwner.observe(invalidUrlError, ::handleInvalidUrlError)
            viewLifecycleOwner.observe(invalidUrlTitleError, ::handleInvalidTitleError)
            viewLifecycleOwner.observe(error) {
                handleError(it)
                showFab()
            }
        }
        with(postDetailViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observeEvent(deleted) {
                mainActivity?.toast(getString(R.string.posts_deleted_feedback))
            }
            viewLifecycleOwner.observe(error, ::handleError)
        }
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.posts_add_title)
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
                visible()
                show()
            }
        }

        setupDescriptionLayouts()
        if (content.showDescription) {
            textInputUrlDescription.visible()
            buttonEditDescription.gone()
        }

        originalPost = content.post

        if (isRecreating) {
            return
        }

        with(content.post) {
            editTextUrl.setText(url)
            editTextTitle.setText(title)
            editTextDescription.setText(description)
            checkboxPrivate.isChecked = private
            checkboxReadLater.isChecked = readLater

            chipGroupTags.removeAllViews()
            tags?.forEach { chipGroupTags.addTag(it) }
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
        context?.showStyledDialog(
            dialogStyle = R.style.AppTheme_AlertDialog,
            dialogBackground = R.drawable.background_contrast_rounded
        ) {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(post.url) })
    }

    private fun handleInvalidUrlError(message: String) {
        message.takeIf(String::isNotEmpty)
            ?.let {
                textInputLayoutUrl.showError(it)
                showFab()
            }
            ?: textInputLayoutUrl.clearError()
    }

    private fun handleInvalidTitleError(message: String) {
        message.takeIf(String::isNotEmpty)
            ?.let {
                textInputLayoutTitle.showError(it)
                showFab()
            }
            ?: textInputLayoutTitle.clearError()
    }

    private fun showFab() {
        mainActivity?.updateViews { _, fab -> fab.show() }
    }

    private fun hideFab() {
        mainActivity?.updateViews { _, fab -> fab.hide() }
    }
}
