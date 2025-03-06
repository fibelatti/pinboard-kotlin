package com.fibelatti.pinboard.core.android.base

import app.cash.turbine.test
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class BaseViewModelInstanceTests : BaseViewModelTest() {

    private val initialState = createAppState()

    private val appStateFlow = MutableStateFlow(initialState)
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
        coJustRun { runDelayedAction(any(), any()) }
    }

    private val viewModel = object : BaseViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
    ) {

        inline fun <reified T : Content> filtered(): Flow<T> = filteredContent<T>()
    }

    @Test
    fun `WHEN appState is called THEN repository content should be returned`() = runTest {
        assertThat(viewModel.appState.first()).isEqualTo(initialState)
    }

    @Test
    fun `WHEN runAction is called THEN repository should runAction`() = runTest {
        // GIVEN
        val mockAction = mockk<Action>()

        // WHEN
        viewModel.runAction(mockAction)

        // THEN
        coVerify { mockAppStateRepository.runAction(mockAction) }
    }

    @Test
    fun `WHEN runDelayedAction is called THEN repository should runDelayedAction`() = runTest {
        // GIVEN
        val mockAction = mockk<Action>()
        val delay = 300L

        // WHEN
        viewModel.runDelayedAction(mockAction, delay)

        // THEN
        coVerify { mockAppStateRepository.runDelayedAction(mockAction, delay) }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SpecificContentTypeTests {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post list content should be emitted to postListContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<PostListContent>().test {
                if (content is PostListContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post detail content should be emitted to postDetailContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<PostDetailContent>().test {
                if (content is PostDetailContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only add post content should be emitted to addPostContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<AddPostContent>().test {
                if (content is AddPostContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only edit post content should be emitted to editPostContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<EditPostContent>().test {
                if (content is EditPostContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only search content should be emitted to searchContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<SearchContent>().test {
                if (content is SearchContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only tag list content should be emitted to tagListContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<TagListContent>().test {
                if (content is TagListContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only note list content should be emitted to noteListContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<NoteListContent>().test {
                if (content is NoteListContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only note detail content should be emitted to noteDetailContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<NoteDetailContent>().test {
                if (content is NoteDetailContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only popular posts content should be emitted to popularPostsContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<PopularPostsContent>().test {
                if (content is PopularPostsContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only popular post details content should be emitted to popularPostDetailContent`(
            content: Content,
        ) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<PopularPostDetailContent>().test {
                if (content is PopularPostDetailContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only user preferences content should be emitted to userPreferencesContent`(content: Content) = runTest {
            appStateFlow.value = createAppState(content = content)

            viewModel.filtered<UserPreferencesContent>().test {
                if (content is UserPreferencesContent) {
                    assertThat(expectMostRecentItem()).isEqualTo(content)
                } else {
                    expectNoEvents()
                }
            }
        }

        fun testCases(): List<Content> = Content::class.allSealedSubclasses.map { it.objectInstance ?: mockkClass(it) }
    }
}
