package com.fibelatti.pinboard.features.filters.presentation

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.ui.foundation.toStableList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SavedFiltersFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val savedFiltersViewModel: SavedFiltersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            SavedFiltersScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                savedFiltersViewModel = savedFiltersViewModel,
                onBackPressed = { navigateBack() },
                onError = ::handleError,
                onSavedFilterLongClicked = ::showQuickActions,
            )
        }
    }

    private fun showQuickActions(savedFilter: SavedFilter) {
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = SavedFiltersQuickActions.allOptions(savedFilter).toStableList(),
            optionName = { getString(it.title) },
            optionIcon = SavedFiltersQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is SavedFiltersQuickActions.Delete -> {
                        savedFiltersViewModel.deleteSavedFilter(option.savedFilter)
                        requireView().showBanner(getString(R.string.saved_filters_deleted_feedback))
                    }
                }
            },
        )
    }

    companion object {

        @JvmStatic
        val TAG: String = "SavedFiltersFragment"
    }
}

private sealed class SavedFiltersQuickActions(
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
