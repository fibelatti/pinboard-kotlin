package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import com.fibelatti.pinboard.databinding.ListItemChipBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.chip.ChipGroup

class TagChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipGroupStyle,
) : ChipGroup(context, attrs, defStyleAttr) {

    companion object {

        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"
        private const val DYNAMIC_TAGS_KEY = "DYNAMIC_TAGS_KEY"
    }

    var onTagChipAdded: ((Tag) -> Unit)? = null
    var onTagChipRemoved: ((Tag) -> Unit)? = null
    var onTagChipClicked: ((Tag) -> Unit)? = null

    fun getAllTags(): List<Tag> = children.toList().filterIsInstance<TagChip>().mapNotNull(TagChip::getValue)

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
            onTagChipAdded?.invoke(tag)
        }
    }

    private fun createTagChip(value: Tag, showRemoveIcon: Boolean): View {
        return ListItemChipBinding.inflate(LayoutInflater.from(context), this, false).root.apply {
            setValue(value)

            setOnClickListener {
                onTagChipClicked?.invoke(value)
            }

            isCloseIconVisible = showRemoveIcon

            if (showRemoveIcon) {
                setOnCloseIconClickListener {
                    removeView(this)
                    onTagChipRemoved?.invoke(value)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
        putParcelableArrayList(DYNAMIC_TAGS_KEY, ArrayList(getAllTags()))
    }

    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            return
        }

        super.onRestoreInstanceState(state.getParcelable(SUPER_STATE_KEY))

        val tags: List<Tag>? = state.getParcelableArrayList(DYNAMIC_TAGS_KEY)
        tags?.forEach { addTag(it) }
    }
}
