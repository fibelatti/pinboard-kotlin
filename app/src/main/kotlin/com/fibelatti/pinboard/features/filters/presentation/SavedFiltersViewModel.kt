package com.fibelatti.pinboard.features.filters.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SavedFiltersViewModel(
    private val savedFiltersRepository: SavedFiltersRepository,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : BaseViewModel() {

    val state: StateFlow<List<SavedFilter>> = savedFiltersRepository.getSavedFilters()
        .stateIn(
            scope = scope,
            started = sharingStarted,
            initialValue = emptyList(),
        )

    fun deleteSavedFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.deleteFilter(savedFilter = savedFilter)
        }
    }
}
