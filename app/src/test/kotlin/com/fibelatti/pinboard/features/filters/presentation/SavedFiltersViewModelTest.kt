package com.fibelatti.pinboard.features.filters.presentation

import app.cash.turbine.test
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SavedFiltersViewModelTest : BaseViewModelTest() {

    private val savedFiltersRepository: SavedFiltersRepository = mockk {
        every { getSavedFilters() } returns emptyFlow()
    }

    private val savedFiltersViewModel by lazy {
        SavedFiltersViewModel(
            scope = TestScope(dispatcher),
            sharingStarted = SharingStarted.Lazily,
            appStateRepository = mockk(),
            savedFiltersRepository = savedFiltersRepository,
        )
    }

    @Test
    fun `state emits the repository values`() = runTest {
        val firstValue = listOf(mockk<SavedFilter>())
        val secondValue = listOf(mockk<SavedFilter>())

        every { savedFiltersRepository.getSavedFilters() } returns flow {
            emit(firstValue)
            delay(1_000L)
            emit(secondValue)
        }

        savedFiltersViewModel.state.test {
            assertThat(awaitItem()).isEqualTo(firstValue)
            assertThat(awaitItem()).isEqualTo(secondValue)
        }
    }

    @Test
    fun `deleteSavedFilter deletes the filter in the repository`() = runTest {
        val filter = mockk<SavedFilter>()

        coJustRun { savedFiltersRepository.deleteFilter(filter) }

        savedFiltersViewModel.deleteSavedFilter(filter)

        coVerify { savedFiltersRepository.deleteFilter(filter) }
    }
}
