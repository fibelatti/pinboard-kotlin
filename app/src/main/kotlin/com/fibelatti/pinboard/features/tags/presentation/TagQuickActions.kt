package com.fibelatti.pinboard.features.tags.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Edit
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class TagQuickActions(
    @StringRes val title: Int,
    val icon: ImageVector,
) {

    abstract val tag: Tag

    data class Rename(
        override val tag: Tag,
    ) : TagQuickActions(
        title = R.string.quick_actions_rename,
        icon = AppIcons.Edit,
    )

    companion object {

        fun allOptions(
            tag: Tag,
        ): List<TagQuickActions> = listOf(
            Rename(tag),
        )
    }
}
