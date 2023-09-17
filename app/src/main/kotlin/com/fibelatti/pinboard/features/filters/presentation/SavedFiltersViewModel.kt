package com.fibelatti.pinboard.features.filters.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedFiltersViewModel @Inject constructor(
    private val savedFiltersRepository: SavedFiltersRepository,
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
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
