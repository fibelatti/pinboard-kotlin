package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class TagsViewModelTest : BaseViewModelTest() {

    private val mockTagsRepository = mockk<TagsRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>()

    private val tagsViewModel = TagsViewModel(
        mockTagsRepository,
        mockAppStateRepository,
    )

    @Test
    fun `WHEN getAllTags fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockTagsRepository.getAllTags() } returns flowOf(Failure(error))

        // WHEN
        tagsViewModel.getAll(mockk())

        // THEN
        assertThat(tagsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN source is MENU WHEN getAllTags succeeds THEN AppStateRepository should run SetTags`() {
        // GIVEN
        coEvery { mockTagsRepository.getAllTags() } returns flowOf(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.MENU)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetTags(mockTags)) }
    }

    @Test
    fun `GIVEN source is SEARCH WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        coEvery { mockTagsRepository.getAllTags() } returns flowOf(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.SEARCH)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetSearchTags(mockTags)) }
    }
}
