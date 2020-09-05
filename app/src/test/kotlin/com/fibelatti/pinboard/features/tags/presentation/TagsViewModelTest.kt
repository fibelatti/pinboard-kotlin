package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class TagsViewModelTest : BaseViewModelTest() {

    private val mockGetAllTags = mockk<GetAllTags>()
    private val mockAppStateRepository = mockk<AppStateRepository>()

    private val tagsViewModel = TagsViewModel(
        mockGetAllTags,
        mockAppStateRepository
    )

    @Test
    fun `WHEN getAllTags fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        coEvery { mockGetAllTags() } returns Failure(error)

        // WHEN
        tagsViewModel.getAll(mockk())

        // THEN
        tagsViewModel.error.currentValueShouldBe(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN source is MENU WHEN getAllTags succeeds THEN AppStateRepository should run SetTags`() {
        // GIVEN
        coEvery { mockGetAllTags() } returns Success(mockTags)

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.MENU)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetTags(mockTags)) }
    }

    @Test
    fun `GIVEN source is SEARCH WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        coEvery { mockGetAllTags() } returns Success(mockTags)

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.SEARCH)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetSearchTags(mockTags)) }
    }
}
