package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.android.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlInvalid
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
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
    private val mockGetSuggestedTags = mockk<GetSuggestedTags>()
    private val mockAddPost = mockk<AddPost>()
    private val mockResourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.validation_error_invalid_url) } returns "R.string.validation_error_invalid_url"
        every { getString(R.string.validation_error_empty_url) } returns "R.string.validation_error_empty_url"
        every { getString(R.string.validation_error_empty_title) } returns "R.string.validation_error_empty_title"
    }

    private val editPostViewModel = EditPostViewModel(
        appStateRepository = mockAppStateRepository,
        getSuggestedTags = mockGetSuggestedTags,
        addPost = mockAddPost,
        resourceProvider = mockResourceProvider,
        scope = TestScope(UnconfinedTestDispatcher()),
        sharingStarted = SharingStarted.Eagerly,
    )

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
            coEvery { mockGetSuggestedTags(any()) } returns Failure(Exception())

            // WHEN
            editPostViewModel.searchForTag(mockTagString1, mockk())

            // THEN
            assertThat(editPostViewModel.suggestedTags.isEmpty()).isTrue()
        }

    @Test
    fun `GIVEN getSuggestedTags will succeed WHEN searchForTag is called THEN suggestedTags should receive its response`() =
        runTest {
            // GIVEN
            val result = listOf(mockTagString1, mockTagString2)
            coEvery { mockGetSuggestedTags(any()) } returns Success(result)

            // WHEN
            editPostViewModel.searchForTag(mockTagString1, mockk())

            // THEN
            assertThat(editPostViewModel.suggestedTags.first()).isEqualTo(result)
        }

    @Test
    fun `GIVEN url is blank WHEN saveLink is called THEN invalidUrlError will receive a value`() = runUnconfinedTest {
        val saved = editPostViewModel.saved.collectIn(this)

        // GIVEN
        editPostViewModel.initializePost(
            Post(
                url = "",
                title = mockUrlTitle,
                description = mockUrlDescription,
                private = true,
                readLater = true,
                tags = mockTags
            )
        )

        // WHEN
        editPostViewModel.saveLink()

        // THEN
        assertThat(editPostViewModel.invalidUrlError.first()).isEqualTo("R.string.validation_error_empty_url")
        assertThat(saved).isEmpty()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN title is blank WHEN saveLink is called THEN invalidUrlTitleError will received a value`() =
        runUnconfinedTest {
            val saved = editPostViewModel.saved.collectIn(this)

            // GIVEN
            editPostViewModel.initializePost(
                Post(
                    url = mockUrlValid,
                    title = "",
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.invalidUrlTitleError.first()).isEqualTo("R.string.validation_error_empty_title")
            assertThat(saved).isEmpty()
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }

    @Test
    fun `GIVEN addPost returns InvalidUrlException WHEN saveLink is called THEN invalidUrlError will receive a value`() =
        runUnconfinedTest {
            // GIVEN
            coEvery {
                mockAddPost(
                    AddPost.Params(
                        url = mockUrlInvalid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = true,
                        readLater = true,
                        tags = mockTags
                    )
                )
            } returns Failure(InvalidUrlException())

            editPostViewModel.initializePost(
                Post(
                    url = mockUrlInvalid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )

            val saved = editPostViewModel.saved.collectIn(this)

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.loading.first()).isEqualTo(false)
            assertThat(editPostViewModel.invalidUrlError.first()).isEqualTo("R.string.validation_error_invalid_url")
            assertThat(editPostViewModel.invalidUrlTitleError.first()).isEqualTo("")
            assertThat(saved).isEmpty()
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        }

    @Test
    fun `GIVEN addPost returns an error WHEN saveLink is called THEN error will receive a value`() = runUnconfinedTest {
        // GIVEN
        val error = Exception()
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )
        } returns Failure(error)

        editPostViewModel.initializePost(
            Post(
                url = mockUrlValid,
                title = mockUrlTitle,
                description = mockUrlDescription,
                private = true,
                readLater = true,
                tags = mockTags
            )
        )

        val saved = editPostViewModel.saved.collectIn(this)

        // WHEN
        editPostViewModel.saveLink()

        // THEN
        assertThat(editPostViewModel.loading.first()).isEqualTo(false)
        assertThat(editPostViewModel.invalidUrlError.first()).isEqualTo("")
        assertThat(editPostViewModel.invalidUrlTitleError.first()).isEqualTo("")
        assertThat(editPostViewModel.error.first()).isEqualTo(error)
        assertThat(saved).isEmpty()

        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN addPost is successful WHEN saveLink is called THEN AppStateRepository should run PostSaved`() =
        runUnconfinedTest {
            // GIVEN
            val post = createPost()
            coEvery {
                mockAddPost(
                    AddPost.Params(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = true,
                        readLater = true,
                        tags = mockTags
                    )
                )
            } returns Success(post)

            editPostViewModel.initializePost(
                Post(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )

            val saved = editPostViewModel.saved.collectIn(this)

            // WHEN
            editPostViewModel.saveLink()

            // THEN
            assertThat(editPostViewModel.loading.first()).isEqualTo(true)
            assertThat(editPostViewModel.invalidUrlError.first()).isEqualTo("")
            assertThat(editPostViewModel.invalidUrlTitleError.first()).isEqualTo("")
            assertThat(editPostViewModel.error.isEmpty()).isTrue()
            assertThat(saved).isEqualTo(listOf(Unit))

            coVerify { mockAppStateRepository.runAction(PostSaved(post)) }
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
