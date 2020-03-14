package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.children
import com.fibelatti.core.extension.inflate
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.chip.ChipGroup

class TagChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.chipGroupStyle
) : ChipGroup(context, attrs, defStyleAttr) {

    var onTagChipAdded: (() -> Unit)? = null
    var onTagChipRemoved: (() -> Unit)? = null
    var onTagChipClicked: ((Tag) -> Unit)? = null

    fun getAllTags(): List<Tag> = children.filterIsInstance<TagChip>().mapNotNull(TagChip::getValue)

    fun addTag(tag: Tag, index: Int = -1, showRemoveIcon: Boolean = true) {
        addIfNotAlreadyAdded(tag, index, showRemoveIcon)
    }

    fun addValue(value: String, index: Int = -1, showRemoveIcon: Boolean = true) {
        val tags = createTagsFromText(value)

        for (tag in tags) {
            addIfNotAlreadyAdded(tag, index, showRemoveIcon)
        }
    }

    private fun createTagsFromText(text: String): List<Tag> = text.trim().split(" ").map(::Tag)

    private fun addIfNotAlreadyAdded(tag: Tag, index: Int = -1, showRemoveIcon: Boolean) {
        if (children.none { (it as? TagChip)?.getValue() == tag }) {
            addView(createTagChip(tag, showRemoveIcon), index)
            onTagChipAdded?.invoke()
        }
    }

    private fun createTagChip(value: Tag, showRemoveIcon: Boolean): View {
        return inflate(R.layout.list_item_chip).applyAs<View, TagChip> {
            setValue(value)

            setOnClickListener {
                onTagChipClicked?.invoke(value)
            }

            isCloseIconVisible = showRemoveIcon

            if (showRemoveIcon) {
                setOnCloseIconClickListener {
                    removeView(this)
                    onTagChipRemoved?.invoke()
                }
            }
        }
    }
}
