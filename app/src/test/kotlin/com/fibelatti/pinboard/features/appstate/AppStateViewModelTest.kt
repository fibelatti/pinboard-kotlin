package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AppStateViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)

    private lateinit var appStateViewModel: AppStateViewModel

    @Test
    fun `WHEN getContent is called THEN repository content should be returned`() {
        // GIVEN
        val mockContent = mockk<Content>()

        every { mockAppStateRepository.getContent() } returns flowOf(mockContent)

        appStateViewModel = AppStateViewModel(mockAppStateRepository)

        // THEN
        runBlocking {
            assertThat(appStateViewModel.content.first()).isEqualTo(mockContent)
        }
    }

    @Test
    fun `WHEN reset is called THEN repository should reset`() {
        appStateViewModel = AppStateViewModel(mockAppStateRepository)

        // WHEN
        appStateViewModel.reset()

        // THEN
        verify { mockAppStateRepository.reset() }
    }

    @Test
    fun `WHEN runAction is called THEN repository should runAction`() {
        // GIVEN
        val mockAction = mockk<Action>()

        appStateViewModel = AppStateViewModel(mockAppStateRepository)

        // WHEN
        appStateViewModel.runAction(mockAction)

        // THEN
        coVerify { mockAppStateRepository.runAction(mockAction) }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SpecificContentTypeTests {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post list content should be emitted to postListContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is PostListContent) {
                    assertThat(appStateViewModel.postListContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.postListContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post detail content should be emitted to postDetailContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is PostDetailContent) {
                    assertThat(appStateViewModel.postDetailContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.postDetailContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only add post content should be emitted to addPostContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is AddPostContent) {
                    assertThat(appStateViewModel.addPostContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.addPostContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only edit post content should be emitted to editPostContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is EditPostContent) {
                    assertThat(appStateViewModel.editPostContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.editPostContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only search content should be emitted to searchContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is SearchContent) {
                    assertThat(appStateViewModel.searchContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.searchContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only tag list content should be emitted to tagListContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is TagListContent) {
                    assertThat(appStateViewModel.tagListContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.tagListContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only note list content should be emitted to noteListContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is NoteListContent) {
                    assertThat(appStateViewModel.noteListContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.noteListContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only note detail content should be emitted to noteDetailContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is NoteDetailContent) {
                    assertThat(appStateViewModel.noteDetailContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.noteDetailContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only popular posts content should be emitted to popularPostsContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is PopularPostsContent) {
                    assertThat(appStateViewModel.popularPostsContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.popularPostsContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only popular post details content should be emitted to popularPostDetailContent`(
            content: Content,
        ) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is PopularPostDetailContent) {
                    assertThat(appStateViewModel.popularPostDetailContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.popularPostDetailContent.isEmpty()).isTrue()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only user preferences content should be emitted to userPreferencesContent`(content: Content) {
            // GIVEN
            every { mockAppStateRepository.getContent() } returns flowOf(content)

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            runBlocking {
                if (content is UserPreferencesContent) {
                    assertThat(appStateViewModel.userPreferencesContent.first()).isEqualTo(content)
                } else {
                    assertThat(appStateViewModel.userPreferencesContent.isEmpty()).isTrue()
                }
            }
        }

        fun testCases(): List<Content> = mutableListOf<Content>().apply {
            Content::class.allSealedSubclasses
                .map { it.objectInstance ?: mockk() }
                .forEach { add(it) }
        }
    }
}
