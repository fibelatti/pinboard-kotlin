package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.afterTextChanged
import com.fibelatti.core.extension.clearError
import com.fibelatti.core.extension.clearText
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.mainActivity
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.layout_add_post.*
import kotlinx.android.synthetic.main.layout_add_tags.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class PostAddFragment @Inject constructor(
    private val suggestedTagsAdapter: SuggestedTagsAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "PostAddFragment"
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(this)
    }
    private val postAddViewModel by lazy { viewModelFactory.get<PostAddViewModel>(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
        setupViewModels()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.updateViews { bottomAppBar, _ ->
            bottomAppBar.hideKeyboard()
            bottomAppBar.visible()
        }
    }

    private fun setupLayout() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.posts_add_title)
            setNavigateUp(R.drawable.ic_close) { navigateBack() }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.gone()
            fab.run {
                setImageResource(R.drawable.ic_done)
                setOnClickListener {
                    saveLink()
                    hide()
                }
                show()
            }
        }

        setupTagLayouts()
    }

    private fun saveLink() {
        postAddViewModel.saveLink(
            editTextUrl.textAsString(),
            editTextTitle.textAsString(),
            editTextDescription.textAsString(),
            checkboxPrivate.isChecked,
            checkboxReadLater.isChecked,
            chipGroupTags.getAllTags()
        )
    }

    private fun setupTagLayouts() {
        buttonEditTags.setOnClickListener { focusOnTags() }
        buttonTagsDone.setOnClickListener { focusOnPost() }

        setupTagInput()

        chipGroupTags.onTagChipAdded = { updateSuggestedTags() }
        chipGroupTags.onTagChipRemoved = { updateSuggestedTags() }

        recyclerViewSuggestedTags
            .withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = suggestedTagsAdapter

        suggestedTagsAdapter.onTagClicked = {
            chipGroupTags.addValue(it, index = 0)
            editTextTags.clearText()
        }
    }

    private fun focusOnTags() {
        layoutAddPost.gone()

        textViewTagsTitle.gone()
        buttonEditTags.gone()

        textInputLayoutTags.visible()
        buttonTagsDone.visible()
        recyclerViewSuggestedTags.visible()
    }

    private fun focusOnPost() {
        layoutAddPost.visible()

        textViewTagsTitle.visible()
        buttonEditTags.visible()

        textInputLayoutTags.gone()
        buttonTagsDone.gone()
        recyclerViewSuggestedTags.gone()

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
                text.isNotBlank() -> postAddViewModel.searchForTag(text, chipGroupTags.getAllTags())
                else -> suggestedTagsAdapter.clearItems()
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

    private fun updateSuggestedTags() {
        editTextTags.textAsString().takeIf { it.isNotBlank() }?.let {
            postAddViewModel.searchForTag(it, chipGroupTags.getAllTags())
        }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.editPostContent, ::showPostDetails)
        with(postAddViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observe(suggestedTags, suggestedTagsAdapter::submitList)
            viewLifecycleOwner.observeEvent(saved) { mainActivity?.toast(getString(R.string.posts_saved_feedback)) }
            viewLifecycleOwner.observe(invalidUrlError, ::handleInvalidUrlError)
            viewLifecycleOwner.observe(invalidUrlTitleError, ::handleInvalidTitleError)
            viewLifecycleOwner.error(error) {
                handleError(it)
                showFab()
            }
        }
    }

    private fun showPostDetails(content: EditPostContent) {
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

    private fun handleInvalidUrlError(message: String) {
        textInputLayoutUrl.run {
            message.takeIf(String::isNotEmpty)?.let {
                showError(it)
                showFab()
            } ?: clearError()
        }
    }

    private fun handleInvalidTitleError(message: String) {
        textInputLayoutTitle.run {
            message.takeIf(String::isNotEmpty)?.let {
                showError(it)
                showFab()
            } ?: clearError()
        }
    }

    private fun showFab() {
        mainActivity?.updateViews { _, fab -> fab.show() }
    }
}
