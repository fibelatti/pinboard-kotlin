package com.fibelatti.pinboard.features.filters.domain

import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import kotlinx.coroutines.flow.Flow

interface SavedFiltersRepository {

    fun getSavedFilters(): Flow<List<SavedFilter>>

    suspend fun saveFilter(savedFilter: SavedFilter)

    suspend fun deleteFilter(savedFilter: SavedFilter)
}
