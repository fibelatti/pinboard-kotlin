package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SearchPostViewModelTest : BaseViewModelTest() {

    private val postsRepository = mockk<PostsRepository>()
    private val savedFiltersRepository = mockk<SavedFiltersRepository>()

    private val viewModel = SearchPostViewModel(
        postsRepository = postsRepository,
        savedFiltersRepository = savedFiltersRepository,
    )

    @Test
    fun `when searchParametersChanged is called then queryResultSize should emit`() = runTest {
        // GIVEN
        coEvery {
            postsRepository.getQueryResultSize(searchTerm = "term", tags = emptyList())
        } returns 13

        // WHEN
        viewModel.searchParametersChanged(SearchParameters(term = "term", tags = emptyList()))

        // THEN
        assertThat(viewModel.queryResultSize.first()).isEqualTo(13)
    }

    @Test
    fun `when saveFilter is called then it calls the repository`() = runTest {
        val savedFilter = mockk<SavedFilter>()

        coJustRun { savedFiltersRepository.saveFilter(savedFilter) }

        viewModel.saveFilter(savedFilter)

        coVerify { savedFiltersRepository.saveFilter(savedFilter) }
    }
}
