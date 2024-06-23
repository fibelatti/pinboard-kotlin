package com.fibelatti.bookmarking.features.filters.data

import com.fibelatti.bookmarking.collectIn
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SavedFiltersDataSourceTest {

    private val savedFiltersDao: SavedFiltersDao = mockk()
    private val savedFiltersDtoMapper: SavedFilterDtoMapper = mockk()

    private val dataSource = SavedFiltersDataSource(
        savedFiltersDao = savedFiltersDao,
        savedFiltersDtoMapper = savedFiltersDtoMapper,
    )

    @Test
    fun `getSavedFilters maps and emit values from the dao`() = runUnconfinedTest {
        val dto = listOf(mockk<SavedFilterDto>())
        val output = listOf(mockk<SavedFilter>())
        val source = flowOf(dto)

        every { savedFiltersDtoMapper.mapList(dto) } returns output
        every { savedFiltersDao.getSavedFilters() } returns source

        val result = dataSource.getSavedFilters().collectIn(this)

        assertThat(result).containsExactly(output)
    }

    @Test
    fun `saveFilter maps and saves to the dao`() = runTest {
        val input = mockk<SavedFilter>()
        val dto = mockk<SavedFilterDto>()

        every { savedFiltersDtoMapper.mapReverse(input) } returns dto
        coJustRun { savedFiltersDao.saveFilter(dto) }

        dataSource.saveFilter(input)

        coVerify { savedFiltersDao.saveFilter(dto) }
    }

    @Test
    fun `deleteFilter maps and deletes from the dao`() = runTest {
        val input = mockk<SavedFilter>()
        val dto = mockk<SavedFilterDto>()

        every { savedFiltersDtoMapper.mapReverse(input) } returns dto
        coJustRun { savedFiltersDao.deleteSavedFilter(dto) }

        dataSource.deleteFilter(input)

        coVerify { savedFiltersDao.deleteSavedFilter(dto) }
    }
}
