package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class TagsViewModelTest : BaseViewModelTest() {

    private val mockTagsRepository = mockk<TagsRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository> {
        coJustRun { runAction(any()) }
    }

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
        every { mockTagsRepository.getAllTags() } returns flowOf(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.MENU)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetTags(mockTags)) }
    }

    @Test
    fun `GIVEN source is SEARCH WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        every { mockTagsRepository.getAllTags() } returns flowOf(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.SEARCH)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetSearchTags(mockTags)) }
    }

    @Test
    fun `WHEN renameTag fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockTagsRepository.renameTag(any(), any()) } returns Failure(error)

        // WHEN
        tagsViewModel.renameTag(tag = Tag(name = "old-name"), newName = "new-name")

        // THEN
        assertThat(tagsViewModel.loading.first()).isFalse()
        assertThat(tagsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN renameTag succeeds THEN AppStateRepository should run SetTags`() = runTest {
        // GIVEN
        val tags = listOf(Tag(name = "new-name"))
        coEvery { mockTagsRepository.renameTag(any(), any()) } returns Success(tags)

        // WHEN
        tagsViewModel.renameTag(tag = Tag(name = "old-name"), newName = "new-name")

        // THEN
        assertThat(tagsViewModel.loading.first()).isFalse()
        assertThat(tagsViewModel.error.isEmpty()).isTrue()
        coVerify { mockAppStateRepository.runAction(SetTags(tags = tags, shouldReloadPosts = true)) }
    }
}
