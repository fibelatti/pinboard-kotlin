package com.fibelatti.pinboard.features.posts.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class EditPostViewModelTest : BaseViewModelTest() {

    private val post = createPost()

    private val appStateFlow = MutableStateFlow(
        createAppState(content = EditPostContent(post = post, previousContent = mockk())),
    )
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
    }

    private val tagManagerStateFlow = MutableStateFlow<TagManagerState?>(null)
    private val mockTagManagerRepository = mockk<TagManagerRepository> {
        every { tagManagerState } returns tagManagerStateFlow.filterNotNull()
    }

    private val mockAddPost = mockk<AddPost>()
    private val mockResourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.validation_error_invalid_url) } returns "R.string.validation_error_invalid_url"
        every { getString(R.string.validation_error_empty_url) } returns "R.string.validation_error_empty_url"
        every { getString(R.string.validation_error_empty_title) } returns "R.string.validation_error_empty_title"
    }

    private val editPostViewModel = EditPostViewModel(
        scope = TestScope(dispatcher),
        dispatchers = Dispatchers.Unconfined,
        sharingStarted = SharingStarted.Eagerly,
        appStateRepository = mockAppStateRepository,
        tagManagerRepository = mockTagManagerRepository,
        addPost = mockAddPost,
        resourceProvider = mockResourceProvider,
    )

    @Test
    fun `initial post state emissions reset the post state`() = runTest {
        editPostViewModel.postState.test {
            appStateFlow.value = createAppState(content = createPostListContent())
            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = true,
                    defaultTags = emptyList(),
                    previousContent = mockk(),
                ),
            )

            assertThat(receivedItems()).containsExactly(
                post,
                Post(
                    url = "",
                    title = "",
                    description = "",
                    private = true,
                    readLater = true,
                    tags = null,
                ),
            )
        }
    }

    @Test
    fun `tag manager emissions update the post state`() = runTest {
        editPostViewModel.postState.test {
            tagManagerStateFlow.value = TagManagerState(tags = listOf(Tag("some-tag")))

            assertThat(expectMostRecentItem()).isEqualTo(post.copy(tags = listOf(Tag("some-tag"))))
        }
    }

    @Test
    fun `WHEN updatePost is called THEN the updated state is emitted`() = runTest {
        val otherPost = mockk<Post>()

        editPostViewModel.postState.test {
            editPostViewModel.updatePost { otherPost }

            assertThat(receivedItems()).containsExactly(post, otherPost)
        }
    }

    @Test
    fun `GIVEN url is blank WHEN saveLink is called THEN invalidUrlError will receive a value`() = runTest {
        editPostViewModel.screenState.test {
            // GIVEN
            editPostViewModel.updatePost { post.copy(url = "") }

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(expectMostRecentItem()).isEqualTo(
                EditPostViewModel.ScreenState(
                    invalidUrlError = "R.string.validation_error_empty_url",
                    saved = false,
                ),
            )
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }
    }

    @Test
    fun `GIVEN title is blank WHEN saveLink is called THEN invalidUrlTitleError will received a value`() =
        runTest {
            editPostViewModel.screenState.test {
                // GIVEN
                editPostViewModel.updatePost { post.copy(title = "") }

                // WHEN
                editPostViewModel.saveLink()

                // THEN
                assertThat(expectMostRecentItem()).isEqualTo(
                    EditPostViewModel.ScreenState(
                        invalidTitleError = "R.string.validation_error_empty_title",
                        saved = false,
                    ),
                )
                coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
            }
        }

    @Test
    fun `GIVEN addPost returns InvalidUrlException WHEN saveLink is called THEN invalidUrlError will receive a value`() =
        runTest {
            editPostViewModel.screenState.test {
                // GIVEN
                coEvery { mockAddPost(post) } returns Failure(InvalidUrlException())

                // WHEN
                editPostViewModel.saveLink()

                // THEN
                assertThat(expectMostRecentItem()).isEqualTo(
                    EditPostViewModel.ScreenState(
                        isLoading = false,
                        invalidUrlError = "R.string.validation_error_invalid_url",
                        invalidTitleError = "",
                        saved = false,
                    ),
                )

                coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
            }
        }

    @Test
    fun `GIVEN addPost returns an error WHEN saveLink is called THEN error will receive a value`() = runTest {
        turbineScope {
            // GIVEN
            val exception = Exception()
            coEvery { mockAddPost(post) } returns Failure(exception)

            val screenState = editPostViewModel.screenState.testIn(backgroundScope)
            val error = editPostViewModel.error.testIn(backgroundScope)

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(screenState.expectMostRecentItem()).isEqualTo(
                EditPostViewModel.ScreenState(
                    isLoading = false,
                    invalidUrlError = "",
                    invalidTitleError = "",
                    saved = false,
                ),
            )
            assertThat(error.expectMostRecentItem()).isEqualTo(exception)
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }
    }

    @Test
    fun `GIVEN addPost is successful WHEN saveLink is called THEN AppStateRepository should run PostSaved`() =
        runTest {
            turbineScope {
                // GIVEN
                coEvery { mockAddPost(post) } returns Success(post)

                val screenState = editPostViewModel.screenState.testIn(backgroundScope)
                val error = editPostViewModel.error.testIn(backgroundScope)

                // WHEN
                editPostViewModel.saveLink()

                // THEN
                assertThat(screenState.expectMostRecentItem()).isEqualTo(
                    EditPostViewModel.ScreenState(
                        isLoading = true,
                        invalidUrlError = "",
                        invalidTitleError = "",
                        saved = true,
                    ),
                )
                assertThat(error.expectMostRecentItem()).isNull()
                coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post)) }
            }
        }

    @Test
    fun `GIVEN there are no changes WHEN hasPendingChanges is called THEN false is returned`() = runTest {
        assertThat(editPostViewModel.hasPendingChanges()).isFalse()
    }

    @Test
    fun `GIVEN there are changes WHEN hasPendingChanges is called THEN true is returned`() = runTest {
        val otherPost = mockk<Post>()

        editPostViewModel.updatePost { otherPost }

        assertThat(editPostViewModel.hasPendingChanges()).isTrue()
    }
}
