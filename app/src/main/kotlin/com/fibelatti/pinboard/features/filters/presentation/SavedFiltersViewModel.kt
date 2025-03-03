package com.fibelatti.pinboard.features.filters.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavedFiltersViewModel @Inject constructor(
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel(scope, appStateRepository) {

    val state: StateFlow<List<SavedFilter>> = savedFiltersRepository.getSavedFilters()
        .stateIn(scope = scope, started = sharingStarted, initialValue = emptyList())

    fun deleteSavedFilter(savedFilter: SavedFilter) {
        scope.launch {
            savedFiltersRepository.deleteFilter(savedFilter = savedFilter)
        }
    }
}
