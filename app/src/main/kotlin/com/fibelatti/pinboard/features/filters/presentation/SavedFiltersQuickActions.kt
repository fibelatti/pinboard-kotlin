package com.fibelatti.pinboard.features.filters.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter

sealed class SavedFiltersQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val savedFilter: SavedFilter

    data class Delete(
        override val savedFilter: SavedFilter,
    ) : SavedFiltersQuickActions(
        title = R.string.quick_actions_delete_filter,
        icon = R.drawable.ic_delete,
    )

    companion object {

        fun allOptions(
            savedFilter: SavedFilter,
        ): List<SavedFiltersQuickActions> = listOf(
            Delete(savedFilter),
        )
    }
}
