package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_DESCRIPTION
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_INVALID
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_TITLE
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_VALID
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.isEmpty
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class EditPostViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockAddPost = mockk<AddPost>()
    private val mockResourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.validation_error_invalid_url) } returns "R.string.validation_error_invalid_url"
        every { getString(R.string.validation_error_empty_url) } returns "R.string.validation_error_empty_url"
        every { getString(R.string.validation_error_empty_title) } returns "R.string.validation_error_empty_title"
    }

    private val editPostViewModel by lazy {
        EditPostViewModel(
            appStateRepository = mockAppStateRepository,
            postsRepository = mockPostsRepository,
            addPost = mockAddPost,
            resourceProvider = mockResourceProvider,
            scope = TestScope(UnconfinedTestDispatcher()),
            sharingStarted = SharingStarted.Eagerly,
        )
    }

    @Test
    fun `GIVEN state was not initialized WHEN initializePost is called THEN the state becomes initialized`() =
        runUnconfinedTest {
            val post = mockk<Post>()
            val result = editPostViewModel.postState.collectIn(this)

            editPostViewModel.initializePost(post)

            assertThat(result).containsExactly(post)
        }

    @Test
    fun `GIVEN state was initialized WHEN initializePost is called THEN the state does not change`() =
        runUnconfinedTest {
            val post = mockk<Post>()
            val otherPost = mockk<Post>()

            val result = editPostViewModel.postState.collectIn(this)

            editPostViewModel.initializePost(post)
            editPostViewModel.initializePost(otherPost)

            assertThat(result).containsExactly(post)
        }

    @Test
    fun `WHEN updatePost is called THEN the updated state is emitted`() = runUnconfinedTest {
        val post = mockk<Post>()
        val otherPost = mockk<Post>()
        val result = editPostViewModel.postState.collectIn(this)

        editPostViewModel.initializePost(post)
        editPostViewModel.updatePost { otherPost }

        assertThat(result).containsExactly(post, otherPost)
    }

    @Test
    fun `GIVEN getSuggestedTags will fail WHEN searchForTag is called THEN suggestedTags should never receive values`() =
        runTest {
            // GIVEN
            coEvery { mockPostsRepository.searchExistingPostTag(any(), any()) } returns Failure(Exception())

            // WHEN
            editPostViewModel.searchForTag(SAMPLE_TAG_VALUE_1, mockk())

            // THEN
            assertThat(editPostViewModel.screenState.first()).isEqualTo(
                EditPostViewModel.ScreenState(suggestedTags = emptyList()),
            )
        }

    @Test
    fun `GIVEN getSuggestedTags will succeed WHEN searchForTag is called THEN suggestedTags should receive its response`() =
        runTest {
            // GIVEN
            val result = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2)
            coEvery {
                mockPostsRepository.searchExistingPostTag(tag = SAMPLE_TAG_VALUE_1, currentTags = emptyList())
            } returns Success(result)

            // WHEN
            editPostViewModel.searchForTag(tag = SAMPLE_TAG_VALUE_1, currentTags = emptyList())

            // THEN
            assertThat(editPostViewModel.screenState.first()).isEqualTo(
                EditPostViewModel.ScreenState(suggestedTags = result),
            )
        }

    @Test
    fun `GIVEN url is blank WHEN saveLink is called THEN invalidUrlError will receive a value`() = runUnconfinedTest {
        // GIVEN
        editPostViewModel.initializePost(
            Post(
                url = "",
                title = SAMPLE_URL_TITLE,
                description = SAMPLE_URL_DESCRIPTION,
                private = true,
                readLater = true,
                tags = SAMPLE_TAGS,
            ),
        )

        // WHEN
        editPostViewModel.saveLink()

        // THEN
        assertThat(editPostViewModel.screenState.first()).isEqualTo(
            EditPostViewModel.ScreenState(
                invalidUrlError = "R.string.validation_error_empty_url",
                saved = false,
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN title is blank WHEN saveLink is called THEN invalidUrlTitleError will received a value`() =
        runUnconfinedTest {
            // GIVEN
            editPostViewModel.initializePost(
                Post(
                    url = SAMPLE_URL_VALID,
                    title = "",
                    description = SAMPLE_URL_DESCRIPTION,
                    private = true,
                    readLater = true,
                    tags = SAMPLE_TAGS,
                ),
            )

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.screenState.first()).isEqualTo(
                EditPostViewModel.ScreenState(
                    invalidTitleError = "R.string.validation_error_empty_title",
                    saved = false,
                ),
            )
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }

    @Test
    fun `GIVEN addPost returns InvalidUrlException WHEN saveLink is called THEN invalidUrlError will receive a value`() =
        runUnconfinedTest {
            // GIVEN
            coEvery {
                mockAddPost(
                    Post(
                        url = SAMPLE_URL_INVALID,
                        title = SAMPLE_URL_TITLE,
                        description = SAMPLE_URL_DESCRIPTION,
                        private = true,
                        readLater = true,
                        tags = SAMPLE_TAGS,
                        id = "",
                        dateAdded = "",
                    ),
                )
            } returns Failure(InvalidUrlException())

            editPostViewModel.initializePost(
                Post(
                    url = SAMPLE_URL_INVALID,
                    title = SAMPLE_URL_TITLE,
                    description = SAMPLE_URL_DESCRIPTION,
                    private = true,
                    readLater = true,
                    tags = SAMPLE_TAGS,
                ),
            )

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.screenState.first()).isEqualTo(
                EditPostViewModel.ScreenState(
                    isLoading = false,
                    invalidUrlError = "R.string.validation_error_invalid_url",
                    invalidTitleError = "",
                    saved = false,
                ),
            )
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }

    @Test
    fun `GIVEN addPost returns an error WHEN saveLink is called THEN error will receive a value`() = runUnconfinedTest {
        // GIVEN
        val error = Exception()
        coEvery {
            mockAddPost(
                Post(
                    url = SAMPLE_URL_VALID,
                    title = SAMPLE_URL_TITLE,
                    description = SAMPLE_URL_DESCRIPTION,
                    private = true,
                    readLater = true,
                    tags = SAMPLE_TAGS,
                    id = "",
                    dateAdded = "",
                ),
            )
        } returns Failure(error)

        editPostViewModel.initializePost(
            Post(
                url = SAMPLE_URL_VALID,
                title = SAMPLE_URL_TITLE,
                description = SAMPLE_URL_DESCRIPTION,
                private = true,
                readLater = true,
                tags = SAMPLE_TAGS,
            ),
        )

        // WHEN
        editPostViewModel.saveLink()

        // THEN
        assertThat(editPostViewModel.screenState.first()).isEqualTo(
            EditPostViewModel.ScreenState(
                isLoading = false,
                invalidUrlError = "",
                invalidTitleError = "",
                saved = false,
            ),
        )
        assertThat(editPostViewModel.error.isEmpty()).isFalse()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN addPost is successful WHEN saveLink is called THEN AppStateRepository should run PostSaved`() =
        runUnconfinedTest {
            // GIVEN
            val post = createPost()
            coEvery {
                mockAddPost(
                    Post(
                        url = SAMPLE_URL_VALID,
                        title = SAMPLE_URL_TITLE,
                        description = SAMPLE_URL_DESCRIPTION,
                        private = true,
                        readLater = true,
                        tags = SAMPLE_TAGS,
                        id = "",
                        dateAdded = "",
                    ),
                )
            } returns Success(post)

            editPostViewModel.initializePost(
                Post(
                    url = SAMPLE_URL_VALID,
                    title = SAMPLE_URL_TITLE,
                    description = SAMPLE_URL_DESCRIPTION,
                    private = true,
                    readLater = true,
                    tags = SAMPLE_TAGS,
                ),
            )

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.screenState.first()).isEqualTo(
                EditPostViewModel.ScreenState(
                    isLoading = true,
                    invalidUrlError = "",
                    invalidTitleError = "",
                    saved = true,
                ),
            )
            assertThat(editPostViewModel.error.isEmpty()).isTrue()
            coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post)) }
        }

    @Test
    fun `GIVEN there are no changes WHEN hasPendingChanges is called THEN false is returned`() = runTest {
        val post = mockk<Post>()

        editPostViewModel.initializePost(post)

        assertThat(editPostViewModel.hasPendingChanges()).isFalse()
    }

    @Test
    fun `GIVEN there are changes WHEN hasPendingChanges is called THEN true is returned`() = runTest {
        val post = mockk<Post>()
        val otherPost = mockk<Post>()

        editPostViewModel.initializePost(post)
        editPostViewModel.updatePost { otherPost }

        assertThat(editPostViewModel.hasPendingChanges()).isTrue()
    }
}
