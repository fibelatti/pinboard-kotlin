package com.fibelatti.pinboard.features.filters.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFiltersDao {

    @Query("select * from ${SavedFilterDto.TABLE_NAME}")
    fun getSavedFilters(): Flow<List<SavedFilterDto>>

    @Upsert
    suspend fun saveFilter(savedFilterDto: SavedFilterDto)

    @Delete
    suspend fun deleteSavedFilter(savedFilterDto: SavedFilterDto)
}
