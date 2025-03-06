package com.fibelatti.pinboard.features.filters.data

import app.cash.turbine.test
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.receivedItems
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
    fun `getSavedFilters maps and emit values from the dao`() = runTest {
        val dto = listOf(mockk<SavedFilterDto>())
        val output = listOf(mockk<SavedFilter>())
        val source = flowOf(dto)

        every { savedFiltersDtoMapper.mapList(dto) } returns output
        every { savedFiltersDao.getSavedFilters() } returns source

        dataSource.getSavedFilters().test {
            assertThat(receivedItems()).containsExactly(output)
        }
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
