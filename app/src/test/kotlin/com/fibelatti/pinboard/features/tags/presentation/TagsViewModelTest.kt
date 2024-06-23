package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.features.tags.domain.model.TagSorting
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_TAGS
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
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
        every { mockTagsRepository.getAllTags() } returns flowOf(Success(MOCK_TAGS))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.MENU)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetTags(MOCK_TAGS)) }
    }

    @Test
    fun `GIVEN source is SEARCH WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        every { mockTagsRepository.getAllTags() } returns flowOf(Success(MOCK_TAGS))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.SEARCH)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetSearchTags(MOCK_TAGS)) }
    }

    @Test
    fun `WHEN sortTags is called AND sorting is AtoZ THEN a new state is emitted`() = runTest {
        tagsViewModel.sortTags(
            tags = listOf(tagA, tagB, tagC),
            sorting = TagSorting.AtoZ,
            searchQuery = "",
        )

        assertThat(tagsViewModel.state.first()).isEqualTo(
            TagsViewModel.State(
                allTags = listOf(tagA, tagB, tagC),
                currentSorting = TagSorting.AtoZ,
                currentQuery = "",
                isLoading = false,
            ),
        )
    }

    @Test
    fun `WHEN sortTags is called AND sorting is MoreFirst THEN a new state is emitted`() = runTest {
        tagsViewModel.sortTags(
            tags = listOf(tagA, tagB, tagC),
            sorting = TagSorting.MoreFirst,
            searchQuery = "",
        )

        assertThat(tagsViewModel.state.first()).isEqualTo(
            TagsViewModel.State(
                allTags = listOf(tagB, tagA, tagC),
                currentSorting = TagSorting.MoreFirst,
                currentQuery = "",
                isLoading = false,
            ),
        )
    }

    @Test
    fun `WHEN sortTags is called AND sorting is LessFirst THEN a new state is emitted`() = runTest {
        tagsViewModel.sortTags(
            tags = listOf(tagA, tagB, tagC),
            sorting = TagSorting.LessFirst,
            searchQuery = "",
        )

        assertThat(tagsViewModel.state.first()).isEqualTo(
            TagsViewModel.State(
                allTags = listOf(tagC, tagA, tagB),
                currentSorting = TagSorting.LessFirst,
                currentQuery = "",
                isLoading = false,
            ),
        )
    }

    @Test
    fun `WHEN searchTags is called THEN a new state is emitted`() = runTest {
        val currentState = tagsViewModel.state.first()

        tagsViewModel.searchTags("new-query")

        assertThat(tagsViewModel.state.first()).isEqualTo(
            currentState.copy(currentQuery = "new-query"),
        )
    }

    @Test
    fun `WHEN renameTag fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockTagsRepository.renameTag(any(), any()) } returns Failure(error)

        // WHEN
        tagsViewModel.renameTag(tag = Tag(name = "old-name"), newName = "new-name")

        // THEN
        assertThat(tagsViewModel.state.first().isLoading).isFalse()
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
        assertThat(tagsViewModel.state.first().isLoading).isFalse()
        assertThat(tagsViewModel.error.isEmpty()).isTrue()
        coVerify { mockAppStateRepository.runAction(SetTags(tags = tags, shouldReloadPosts = true)) }
    }

    @Test
    fun `GIVEN the currentQuery is empty WHEN filteredTags is called THEN all tags are returned`() = runTest {
        val state = TagsViewModel.State(
            allTags = listOf(tagA, tagB, tagC),
            currentQuery = "",
        )

        assertThat(state.filteredTags).isEqualTo(listOf(tagA, tagB, tagC))
    }

    @Test
    fun `GIVEN the currentQuery is not empty WHEN filteredTags is called THEN matching tags are returned`() = runTest {
        val state = TagsViewModel.State(
            allTags = listOf(tagA, tagB, tagC),
            currentQuery = "A",
        )

        assertThat(state.filteredTags).isEqualTo(listOf(tagA))
    }

    private companion object {

        private val tagA = Tag(name = "A", posts = 2)
        private val tagB = Tag(name = "B", posts = 3)
        private val tagC = Tag(name = "C", posts = 1)
    }
}
