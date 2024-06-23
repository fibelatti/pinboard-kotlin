package com.fibelatti.bookmarking.features.filters.domain

import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import kotlinx.coroutines.flow.Flow

public interface SavedFiltersRepository {

    public fun getSavedFilters(): Flow<List<SavedFilter>>

    public suspend fun saveFilter(savedFilter: SavedFilter)

    public suspend fun deleteFilter(savedFilter: SavedFilter)
}
