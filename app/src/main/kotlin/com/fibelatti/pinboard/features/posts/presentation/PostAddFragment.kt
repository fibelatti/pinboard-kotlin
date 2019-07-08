package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.afterTextChanged
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.children
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
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class PostAddFragment @Inject constructor() : BaseFragment() {

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

        setupTagInput()
    }

    private fun saveLink() {
        postAddViewModel.saveLink(
            editTextUrl.textAsString(),
            editTextTitle.textAsString(),
            editTextDescription.textAsString(),
            checkboxPrivate.isChecked,
            checkboxReadLater.isChecked,
            chipGroupTags.children.filterIsInstance<TagChip>().mapNotNull(TagChip::getValue)
        )
    }

    private fun setupTagInput() {
        editTextTags.afterTextChanged { text ->
            if (text.isNotBlank() && text.endsWith(" ")) {
                val tags = createTagsFromText(text)

                for (tag in tags) {
                    if (chipGroupTags.children.none { (it as? TagChip)?.getValue() == tag }) {
                        chipGroupTags.addView(createTagChip(tag))
                        editTextTags.clearText()
                    }
                }
            }
        }
        editTextTags.onKeyboardSubmit {
            when (val text = textAsString().trim()) {
                "" -> hideKeyboard()
                else -> {
                    val tags = createTagsFromText(text)

                    for (tag in tags) {
                        if (chipGroupTags.children.none { (it as? TagChip)?.getValue() == tag }) {
                            chipGroupTags.addView(createTagChip(tag))
                        }
                    }
                    editTextTags.clearText()
                }
            }
        }
    }

    private fun createTagsFromText(text: String): List<Tag> = text.trim().split(" ").map { Tag(it) }

    private fun createTagChip(value: Tag): View {
        return layoutInflater.inflate(R.layout.list_item_chip, chipGroupTags, false)
            .applyAs<View, TagChip> {
                setValue(value)
                setOnCloseIconClickListener { chipGroupTags.removeView(this) }
            }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.editPostContent) { content ->
            showPostDetails(content.post)
        }

        with(postAddViewModel) {
            viewLifecycleOwner.observe(loading) { layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE) }
            viewLifecycleOwner.observeEvent(saved) { mainActivity?.toast(getString(R.string.posts_saved_feedback)) }
            viewLifecycleOwner.observe(invalidUrlError, ::handleInvalidUrlError)
            viewLifecycleOwner.observe(invalidUrlTitleError, ::handleInvalidTitleError)
            viewLifecycleOwner.error(error) {
                handleError(it)
                showFab()
            }
        }
    }

    private fun showPostDetails(post: Post) {
        with(post) {
            editTextUrl.setText(url)
            editTextTitle.setText(title)
            editTextDescription.setText(description)
            checkboxPrivate.isChecked = private
            checkboxReadLater.isChecked = readLater

            chipGroupTags.removeAllViews()
            tags?.forEach { tag -> chipGroupTags.addView(createTagChip(tag)) }
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
