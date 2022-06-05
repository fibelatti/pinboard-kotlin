package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.fibelatti.core.extension.clearText
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.pinboard.databinding.LayoutEditTagsBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag

class AddTagsLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutEditTagsBinding.inflate(LayoutInflater.from(context), this, true)

    @Suppress("MagicNumber")
    fun setup(
        afterTagInput: (currentInput: String, currentTags: List<Tag>) -> Unit = { _, _ -> },
        onTagAdded: (tag: String, currentTags: List<Tag>) -> Unit = { _, _ -> },
        onTagRemoved: (currentInput: String, currentTags: List<Tag>) -> Unit = { _, _ -> },
    ) {
        with(binding) {
            editTextTags.doAfterTextChanged {
                val text = it.toString()
                val currentlyFocused = editTextTags.isFocused

                when {
                    text == ". " -> {
                        // Handle keyboards that add a space after punctuation, . is used for private tags
                        editTextTags.setText(".")
                        editTextTags.setSelection(1)
                    }
                    text.isNotBlank() && text.endsWith(" ") -> {
                        chipGroupTags.addValue(text, index = 0)
                        editTextTags.clearText()
                        onTagAdded(text, chipGroupTags.getAllTags())
                    }
                    text.isNotBlank() -> afterTagInput(text, chipGroupTags.getAllTags())
                    else -> chipGroupSuggestedTags.removeAllViews()
                }

                if (currentlyFocused) editTextTags.postDelayed({ editTextTags.requestFocus() }, 100L)
            }
            editTextTags.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_SEND) {
                when (val text = textAsString().trim()) {
                    "" -> hideKeyboard()
                    else -> {
                        chipGroupTags.addValue(text, index = 0)
                        editTextTags.clearText()
                        onTagAdded(text, chipGroupTags.getAllTags())
                    }
                }
            }
            buttonTagsAdd.setOnClickListener {
                editTextTags.textAsString().takeIf(kotlin.String::isNotBlank)?.let {
                    chipGroupTags.addValue(it, index = 0)
                    editTextTags.clearText()
                    onTagAdded(it, chipGroupTags.getAllTags())
                }
            }
            chipGroupTags.onTagChipRemoved = {
                editTextTags.textAsString().takeIf(String::isNotBlank)?.let {
                    onTagRemoved(it, chipGroupTags.getAllTags())
                }
            }

            chipGroupSuggestedTags.onTagChipClicked = {
                chipGroupTags.addTag(it, index = 0)
                editTextTags.clearText()
                onTagAdded(it.name, chipGroupTags.getAllTags())
            }
        }
    }

    fun showSuggestedTags(tags: List<Tag>, showRemoveIcon: Boolean = true) {
        binding.chipGroupSuggestedTags.removeAllViews()
        tags.forEach { binding.chipGroupSuggestedTags.addTag(it, showRemoveIcon = showRemoveIcon) }
    }

    fun showSuggestedValuesAsTags(tags: List<String>, showRemoveIcon: Boolean = true) {
        binding.chipGroupSuggestedTags.removeAllViews()
        tags.forEach { binding.chipGroupSuggestedTags.addValue(it, showRemoveIcon = showRemoveIcon) }
    }

    fun showTags(tags: List<Tag>, showRemoveIcon: Boolean = true) {
        binding.chipGroupTags.removeAllViews()
        tags.forEach { binding.chipGroupTags.addTag(it, showRemoveIcon = showRemoveIcon) }
    }

    fun showValuesAsTags(tags: List<String>, showRemoveIcon: Boolean = true) {
        binding.chipGroupTags.removeAllViews()
        tags.forEach { binding.chipGroupTags.addValue(it, showRemoveIcon = showRemoveIcon) }
    }

    fun getTags(): List<Tag> = binding.chipGroupTags.getAllTags()
}
