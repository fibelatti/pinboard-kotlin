package com.fibelatti.pinboard.features.share

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.ParseUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.RichUrl
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class ShareReceiverViewModelTest : BaseViewModelTest() {

    private val mockExtractUrl = mockk<ExtractUrl>()
    private val mockParseUrl = mockk<ParseUrl>()
    private val mockAddPost = mockk<AddPost>()
    private val mockUserRepository = mockk<UserRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>()
    private val mockResourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.posts_saved_feedback) } returns "R.string.posts_saved_feedback"
    }

    private val error = Exception()

    private val shareReceiverViewModel = ShareReceiverViewModel(
        mockExtractUrl,
        mockParseUrl,
        mockAddPost,
        mockUserRepository,
        mockAppStateRepository,
        mockResourceProvider
    )

    @Test
    fun `WHEN ExtractUrl fails THEN failed should receive a value`() {
        // GIVEN
        coEvery { mockExtractUrl(mockUrlValid) } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        runBlocking {
            assertThat(shareReceiverViewModel.failed.first()).isEqualTo(error)
        }
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `WHEN ParseUrl fails THEN failed should receive a value`() {
        // GIVEN
        coEvery { mockExtractUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockParseUrl(mockUrlValid) } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        runBlocking {
            assertThat(shareReceiverViewModel.failed.first()).isEqualTo(error)
        }
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `GIVEN getEditAfterSharing is BeforeSaving THEN edit should receive an empty value AND EditPostFromShare should run`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        coEvery { mockExtractUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockParseUrl(mockUrlValid) } returns Success(RichUrl(mockUrlValid, mockUrlValid))
        every { mockUserRepository.getDefaultPrivate() } returns defaultPrivate
        every { mockUserRepository.getDefaultReadLater() } returns defaultReadLater
        every { mockUserRepository.getEditAfterSharing() } returns EditAfterSharing.BeforeSaving

        val expectedPost = Post(
            url = mockUrlValid,
            title = mockUrlValid,
            description = "",
            private = defaultPrivate,
            readLater = defaultReadLater
        )

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify { mockUserRepository.getDefaultPrivate() }
        verify { mockUserRepository.getDefaultReadLater() }
        runBlocking {
            assertThat(shareReceiverViewModel.edit.first()).isEqualTo("")
        }
        coVerify { mockAppStateRepository.runAction(EditPostFromShare(expectedPost)) }
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `WHEN AddPost fails THEN failed should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        coEvery { mockExtractUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockParseUrl(mockUrlValid) } returns Success(RichUrl(mockUrlValid, mockUrlValid))
        every { mockUserRepository.getDefaultPrivate() } returns defaultPrivate
        every { mockUserRepository.getDefaultReadLater() } returns defaultReadLater
        every { mockUserRepository.getEditAfterSharing() } returns mockk()
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    replace = false
                )
            )
        } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify { mockUserRepository.getDefaultPrivate() }
        verify { mockUserRepository.getDefaultReadLater() }
        runBlocking {
            assertThat(shareReceiverViewModel.failed.first()).isEqualTo(error)
        }
    }

    @Test
    fun `GIVEN getEditAfterSharing is SkipEdit WHEN saveUrl succeeds THEN saved should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        coEvery { mockExtractUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockParseUrl(mockUrlValid) } returns Success(RichUrl(mockUrlValid, mockUrlValid))
        every { mockUserRepository.getDefaultPrivate() } returns defaultPrivate
        every { mockUserRepository.getDefaultReadLater() } returns defaultReadLater
        every { mockUserRepository.getEditAfterSharing() } returns EditAfterSharing.SkipEdit

        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    replace = false
                )
            )
        } returns Success(createPost())

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify { mockUserRepository.getDefaultPrivate() }
        verify { mockUserRepository.getDefaultReadLater() }
        runBlocking {
            assertThat(shareReceiverViewModel.saved.first()).isEqualTo("R.string.posts_saved_feedback")
        }
    }

    @Test
    fun `GIVEN getEditAfterSharing is AfterSaving WHEN saveUrl succeeds THEN edit should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        coEvery { mockExtractUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockParseUrl(mockUrlValid) } returns Success(RichUrl(mockUrlValid, mockUrlValid))
        every { mockUserRepository.getDefaultPrivate() } returns defaultPrivate
        every { mockUserRepository.getDefaultReadLater() } returns defaultReadLater
        every { mockUserRepository.getEditAfterSharing() } returns EditAfterSharing.AfterSaving

        val post = createPost()
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    replace = false
                )
            )
        } returns Success(post)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify { mockUserRepository.getDefaultPrivate() }
        verify { mockUserRepository.getDefaultReadLater() }
        runBlocking {
            assertThat(shareReceiverViewModel.edit.first()).isEqualTo("R.string.posts_saved_feedback")
        }
        coVerify { mockAppStateRepository.runAction(EditPostFromShare(post)) }
    }
}
