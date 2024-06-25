package com.fibelatti.bookmarking.features.filters.data

import com.fibelatti.bookmarking.tooling.BaseDbTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class SavedFiltersDaoTest : BaseDbTest() {

    private val dto1 = SavedFilterDto(
        term = "term",
        tags = "tag1,tag2",
    )
    private val dto2 = SavedFilterDto(
        term = "term2",
        tags = "tag1,tag2",
    )
    private val dto3 = SavedFilterDto(
        term = "term",
        tags = "tag1",
    )

    private val dao get() = appDatabase.savedFiltersDao()

    @Test
    fun whenGetSavedFiltersIsCalledThenAllFiltersAreReturned() = runTest {
        dao.saveFilter(dto1)

        val result = dao.getSavedFilters().first()

        assertThat(result).containsExactly(dto1)
    }

    @Test
    fun onlyUniqueFiltersAreSaved() = runTest {
        dao.saveFilter(dto1)
        dao.saveFilter(dto1)
        dao.saveFilter(dto2)
        dao.saveFilter(dto3)

        val result = dao.getSavedFilters().first()

        assertThat(result).containsExactly(dto1, dto2, dto3)
    }

    @Test
    fun onlyMatchingFiltersAreDeleted() = runTest {
        dao.saveFilter(dto1)
        dao.saveFilter(dto2)
        dao.saveFilter(dto3)

        dao.deleteSavedFilter(dto1)

        val result = dao.getSavedFilters().first()

        assertThat(result).containsExactly(dto2, dto3)
    }
}
