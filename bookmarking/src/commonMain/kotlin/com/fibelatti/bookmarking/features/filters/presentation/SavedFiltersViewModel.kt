package com.fibelatti.bookmarking.features.filters.presentation

import com.fibelatti.bookmarking.core.base.BaseViewModel
import com.fibelatti.bookmarking.features.filters.domain.SavedFiltersRepository
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
public class SavedFiltersViewModel(
    private val savedFiltersRepository: SavedFiltersRepository,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : BaseViewModel() {

    public val state: StateFlow<List<SavedFilter>> = savedFiltersRepository.getSavedFilters()
        .stateIn(
            scope = scope,
            started = sharingStarted,
            initialValue = emptyList(),
        )

    public fun deleteSavedFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.deleteFilter(savedFilter = savedFilter)
        }
    }
}
