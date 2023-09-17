package com.fibelatti.pinboard.features.filters.data

import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavedFiltersDataSource @Inject constructor(
    private val savedFiltersDao: SavedFiltersDao,
    private val savedFiltersDtoMapper: SavedFilterDtoMapper,
) : SavedFiltersRepository {

    override fun getSavedFilters(): Flow<List<SavedFilter>> = savedFiltersDao.getSavedFilters()
        .map(savedFiltersDtoMapper::mapList)

    override suspend fun saveFilter(savedFilter: SavedFilter) {
        savedFiltersDao.saveFilter(
            savedFilterDto = savedFiltersDtoMapper.mapReverse(savedFilter),
        )
    }

    override suspend fun deleteFilter(savedFilter: SavedFilter) {
        savedFiltersDao.deleteSavedFilter(
            savedFilterDto = savedFiltersDtoMapper.mapReverse(savedFilter),
        )
    }
}
