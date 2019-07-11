package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.children
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

    fun getAllTags(): List<Tag> = children.filterIsInstance<TagChip>().mapNotNull(TagChip::getValue)

    fun addTag(tag: Tag, index: Int = -1) {
        addIfNotAlreadyAdded(tag, index)
    }

    fun addValue(value: String, index: Int = -1) {
        val tags = createTagsFromText(value)

        for (tag in tags) {
            addIfNotAlreadyAdded(tag, index)
        }
    }

    private fun addIfNotAlreadyAdded(tag: Tag, index: Int = -1) {
        if (children.none { (it as? TagChip)?.getValue() == tag }) {
            addView(createTagChip(tag), index)
            onTagChipAdded?.invoke()
        }
    }

    private fun createTagsFromText(text: String): List<Tag> = text.trim().split(" ").map { Tag(it) }

    private fun createTagChip(value: Tag): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item_chip, this, false)
            .applyAs<View, TagChip> {
                setValue(value)
                setOnCloseIconClickListener {
                    removeView(this)
                    onTagChipRemoved?.invoke()
                }
            }
    }
}
