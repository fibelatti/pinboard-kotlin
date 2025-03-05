package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetResultSize
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SearchPostViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockSavedFiltersRepository = mockk<SavedFiltersRepository>()

    private val viewModel = SearchPostViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        postsRepository = mockPostsRepository,
        savedFiltersRepository = mockSavedFiltersRepository,
    )

    @Test
    fun `WHEN SearchContent is emitted AND searchParameters is active THEN getQueryResultSize is called`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.getQueryResultSize(searchTerm = "term", tags = emptyList()) } returns 13

        // WHEN
        appStateFlow.value = createAppState(
            content = SearchContent(
                searchParameters = SearchParameters(term = "term", tags = emptyList()),
                previousContent = createPostListContent(),
            ),
        )

        // THEN
        coVerify { mockAppStateRepository.runAction(SetResultSize(resultSize = 13)) }

        // WHEN
        appStateFlow.value = createAppState(
            content = SearchContent(
                searchParameters = SearchParameters(),
                previousContent = createPostListContent(),
            ),
        )

        // THEN
        coVerify { mockAppStateRepository.runAction(SetResultSize(resultSize = 0)) }
    }

    @Test
    fun `when saveFilter is called then it calls the repository`() = runTest {
        val savedFilter = mockk<SavedFilter>()

        coJustRun { mockSavedFiltersRepository.saveFilter(savedFilter) }

        viewModel.saveFilter(savedFilter)

        coVerify { mockSavedFiltersRepository.saveFilter(savedFilter) }
    }
}
