package com.fibelatti.pinboard.features.tags.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class TagQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val tag: Tag

    data class Rename(
        override val tag: Tag,
    ) : TagQuickActions(
        title = R.string.quick_actions_rename,
        icon = R.drawable.ic_edit,
    )

    companion object {

        fun allOptions(
            tag: Tag,
        ): List<TagQuickActions> = listOf(
            Rename(tag),
        )
    }
}
