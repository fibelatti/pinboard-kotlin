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
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.onKeyboardSubmit
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class PostAddFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostAddFragment::class.java.simpleName
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
        mainActivity?.updateViews { bottomAppBar: BottomAppBar, _: FloatingActionButton ->
            bottomAppBar.hideKeyboard()
            bottomAppBar.visible()
        }
    }

    private fun setupLayout() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.posts_add_title)
            setNavigateUp(R.drawable.ic_close) { navigateBack() }
        }

        mainActivity?.updateViews { bottomAppBar: BottomAppBar, fab: FloatingActionButton ->
            bottomAppBar.gone()
            fab.run {
                blink {
                    setImageResource(R.drawable.ic_done)
                    setOnClickListener {
                        postAddViewModel.saveLink(
                            editTextUrl.textAsString(),
                            editTextTitle.textAsString(),
                            checkboxPrivate.isChecked,
                            checkboxReadLater.isChecked,
                            chipGroupTags.children.filterIsInstance<TagChip>().mapNotNull { it.getValue() }
                        )
                    }
                }
            }
        }

        setupTagInput()
    }

    private fun setupTagInput() {
        editTextTags.afterTextChanged { text ->
            val tag = createTagFromText(text, handleWhiteSpace = true)

            if (tag != null && chipGroupTags.children.none { (it as? TagChip)?.getValue() == tag }) {
                chipGroupTags.addView(createTagChip(tag))
                editTextTags.setText("")
            }
        }
        editTextTags.onKeyboardSubmit {
            val tag = createTagFromText(editTextTags.textAsString(), handleWhiteSpace = false)

            if (tag != null) {
                if (chipGroupTags.children.none { (it as? TagChip)?.getValue() == tag }) {
                    chipGroupTags.addView(createTagChip(tag))
                }
                editTextTags.clearText()
            }
        }
    }

    private fun createTagFromText(text: String, handleWhiteSpace: Boolean): Tag? {
        val tagText = text.run {
            if (handleWhiteSpace) {
                takeIf { it.endsWith(" ") }?.substringBefore(" ", "")
            } else {
                this
            }
        }?.takeIf { it.isNotBlank() }?.trim()

        return tagText?.let { Tag(it) }
    }

    private fun createTagChip(value: Tag): View {
        return layoutInflater.inflate(R.layout.list_item_chip, chipGroupTags, false)
            .applyAs<View, TagChip> {
                setValue(value)
                setOnCloseIconClickListener { chipGroupTags.removeView(this) }
            }
    }

    private fun setupViewModels() {
        with(postAddViewModel) {
            observeEvent(loading) { layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE) }
            observe(post, ::showPostDetails)
            observe(invalidUrlError, ::handleInvalidUrlError)
            observe(invalidUrlTitleError, ::handleInvalidTitleError)
            observeEvent(saved) {
                mainActivity?.toast(getString(R.string.posts_saved_feedback))
                navigateBack()
            }
            error(error, ::handleError)
        }
    }

    private fun showPostDetails(post: Post) {
        with(post) {
            editTextUrl.setText(url)
            editTextTitle.setText(title)
            checkboxPrivate.isChecked = private
            checkboxReadLater.isChecked = readLater

            chipGroupTags.removeAllViews()
            tags.forEach { tag ->
                chipGroupTags.addView(createTagChip(tag))
            }
        }
    }

    private fun handleInvalidUrlError(message: String) {
        textInputLayoutUrl.run {
            message.takeIf(String::isNotEmpty)?.let { showError(it) } ?: clearError()
        }
    }

    private fun handleInvalidTitleError(message: String) {
        textInputLayoutTitle.run {
            message.takeIf(String::isNotEmpty)?.let { showError(it) } ?: clearError()
        }
    }
}
