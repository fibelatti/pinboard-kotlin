package com.fibelatti.bookmarking.features.filters.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// TODO: Make internal once the migration is completed
@Dao
public interface SavedFiltersDao {

    @Query("select * from ${SavedFilterDto.TABLE_NAME}")
    public fun getSavedFilters(): Flow<List<SavedFilterDto>>

    @Upsert
    public suspend fun saveFilter(savedFilterDto: SavedFilterDto)

    @Delete
    public suspend fun deleteSavedFilter(savedFilterDto: SavedFilterDto)
}
