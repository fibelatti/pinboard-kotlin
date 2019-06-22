package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.util.AttributeSet
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.chip.Chip

class TagChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.chipStyle
) : Chip(context, attrs, defStyleAttr) {

    private var value: Tag? = null

    fun setValue(value: Tag) {
        this.value = value
        this.text = value.name
    }

    fun getValue(): Tag? = value
}
