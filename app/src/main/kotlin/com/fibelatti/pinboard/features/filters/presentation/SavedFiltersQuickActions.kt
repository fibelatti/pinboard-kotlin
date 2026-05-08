package com.fibelatti.pinboard.features.filters.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Delete
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter

sealed class SavedFiltersQuickActions(
    @StringRes val title: Int,
    val icon: ImageVector,
) {

    abstract val savedFilter: SavedFilter

    data class Delete(
        override val savedFilter: SavedFilter,
    ) : SavedFiltersQuickActions(
        title = R.string.quick_actions_delete_filter,
        icon = AppIcons.Delete,
    )

    companion object {

        fun allOptions(
            savedFilter: SavedFilter,
        ): List<SavedFiltersQuickActions> = listOf(
            Delete(savedFilter),
        )
    }
}
