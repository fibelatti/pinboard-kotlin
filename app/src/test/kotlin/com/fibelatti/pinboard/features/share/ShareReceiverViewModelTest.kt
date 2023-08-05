package com.fibelatti.pinboard.features.share

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.ScreenState
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlPreview
import com.fibelatti.pinboard.features.posts.domain.usecase.UrlPreview
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ShareReceiverViewModelTest : BaseViewModelTest() {

    private val mockExtractUrl = mockk<ExtractUrl> {
        coEvery {
            this@mockk.invoke(params = mockUrlValid)
        } returns Success(
            ExtractUrl.ExtractedUrl(url = mockUrlValid),
        )
    }
    private val mockGetUrlPreview = mockk<GetUrlPreview> {
        coEvery {
            this@mockk.invoke(
                GetUrlPreview.Params(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                ),
            )
        } returns Success(
            UrlPreview(
                url = mockUrlValid,
                title = mockUrlValid,
            ),
        )
    }
    private val mockAddPost = mockk<AddPost>()
    private val mockPostsRepository = mockk<PostsRepository> {
        coEvery { getPost(any()) } returns Failure(InvalidRequestException())
    }
    private val mockUserRepository = mockk<UserRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxUnitFun = true)

    private val post = mockk<Post>()
    private val error = Exception()

    private val shareReceiverViewModel = ShareReceiverViewModel(
        extractUrl = mockExtractUrl,
        getUrlPreview = mockGetUrlPreview,
        addPost = mockAddPost,
        postsRepository = mockPostsRepository,
        userRepository = mockUserRepository,
        appStateRepository = mockAppStateRepository,
    )

    @Test
    fun `WHEN ExtractUrl fails THEN failed should receive a value`() = runTest {
        // GIVEN
        coEvery { mockExtractUrl(mockUrlValid) } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

        // THEN
        assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(ScreenState.Error(error))
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `WHEN GetUrlPreview fails THEN failed should receive a value`() = runTest {
        // GIVEN
        coEvery { mockGetUrlPreview(GetUrlPreview.Params(mockUrlValid, mockUrlTitle)) } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

        // THEN
        assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(ScreenState.Error(error))
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `GIVEN getEditAfterSharing is SkipEdit WHEN an existing post is found THEN save should receive a value`() =
        runTest {
            // GIVEN
            coEvery { mockPostsRepository.getPost(mockUrlValid) } returns Success(post)
            every { mockUserRepository.editAfterSharing } returns EditAfterSharing.SkipEdit

            // WHEN
            shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

            // THEN
            assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
                ScreenState.Loaded(
                    ShareReceiverViewModel.SharingResult.Saved(message = R.string.posts_existing_feedback),
                ),
            )
        }

    @Test
    fun `GIVEN getEditAfterSharing is BeforeSaving WHEN an existing post is found THEN save should receive a value`() =
        runTest {
            // GIVEN
            coEvery { mockPostsRepository.getPost(mockUrlValid) } returns Success(post)
            every { mockUserRepository.editAfterSharing } returns EditAfterSharing.BeforeSaving

            // WHEN
            shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

            // THEN
            assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
                ScreenState.Loaded(
                    ShareReceiverViewModel.SharingResult.Edit(message = R.string.posts_existing_feedback),
                ),
            )
            coVerify { mockAppStateRepository.runAction(EditPostFromShare(post)) }
        }

    @Test
    fun `GIVEN getEditAfterSharing is AfterSaving WHEN an existing post is found THEN save should receive a value`() =
        runTest {
            // GIVEN
            coEvery { mockPostsRepository.getPost(mockUrlValid) } returns Success(post)
            every { mockUserRepository.editAfterSharing } returns EditAfterSharing.AfterSaving

            // WHEN
            shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

            // THEN
            assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
                ScreenState.Loaded(
                    ShareReceiverViewModel.SharingResult.Edit(message = R.string.posts_existing_feedback),
                ),
            )
            coVerify { mockAppStateRepository.runAction(EditPostFromShare(post)) }
        }

    @Test
    fun `GIVEN getEditAfterSharing is BeforeSaving THEN edit should receive an empty value AND EditPostFromShare should run`() =
        runTest {
            // GIVEN
            val defaultPrivate = randomBoolean()
            val defaultReadLater = randomBoolean()
            val defaultTags = mockk<List<Tag>>()

            every { mockUserRepository.defaultPrivate } returns defaultPrivate
            every { mockUserRepository.defaultReadLater } returns defaultReadLater
            every { mockUserRepository.defaultTags } returns defaultTags
            every { mockUserRepository.editAfterSharing } returns EditAfterSharing.BeforeSaving

            val expectedPost = Post(
                url = mockUrlValid,
                title = mockUrlValid,
                description = "",
                private = defaultPrivate,
                readLater = defaultReadLater,
                tags = defaultTags,
            )

            // WHEN
            shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

            // THEN
            verify {
                mockUserRepository.defaultPrivate
                mockUserRepository.defaultReadLater
                mockUserRepository.defaultTags
            }
            assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
                ScreenState.Loaded(
                    ShareReceiverViewModel.SharingResult.Edit(message = null),
                ),
            )
            coVerify { mockAppStateRepository.runAction(EditPostFromShare(expectedPost)) }
            coVerify(exactly = 0) { mockAddPost.invoke(any()) }
        }

    @Test
    fun `WHEN AddPost fails THEN failed should receive a value`() = runTest {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()
        val defaultTags = mockk<List<Tag>>()

        every { mockUserRepository.defaultPrivate } returns defaultPrivate
        every { mockUserRepository.defaultReadLater } returns defaultReadLater
        every { mockUserRepository.defaultTags } returns defaultTags
        every { mockUserRepository.editAfterSharing } returns mockk()
        coEvery {
            mockAddPost(
                Post(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    description = "",
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    tags = defaultTags,
                ),
            )
        } returns Failure(error)

        // WHEN
        shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

        // THEN
        verify {
            mockUserRepository.defaultPrivate
            mockUserRepository.defaultReadLater
            mockUserRepository.defaultTags
        }
        assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(ScreenState.Error(error))
    }

    @Test
    fun `GIVEN getEditAfterSharing is SkipEdit WHEN saveUrl succeeds THEN saved should receive a value`() = runTest {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()
        val defaultTags = mockk<List<Tag>>()

        every { mockUserRepository.defaultPrivate } returns defaultPrivate
        every { mockUserRepository.defaultReadLater } returns defaultReadLater
        every { mockUserRepository.defaultTags } returns defaultTags
        every { mockUserRepository.editAfterSharing } returns EditAfterSharing.SkipEdit

        coEvery {
            mockAddPost(
                Post(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    description = "",
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    tags = defaultTags,
                ),
            )
        } returns Success(createPost())

        // WHEN
        shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

        // THEN
        verify {
            mockUserRepository.defaultPrivate
            mockUserRepository.defaultReadLater
            mockUserRepository.defaultTags
        }
        assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
            ScreenState.Loaded(
                ShareReceiverViewModel.SharingResult.Saved(message = R.string.posts_saved_feedback),
            ),
        )
    }

    @Test
    fun `GIVEN getEditAfterSharing is AfterSaving WHEN saveUrl succeeds THEN edit should receive a value`() = runTest {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()
        val defaultTags = mockk<List<Tag>>()

        every { mockUserRepository.defaultPrivate } returns defaultPrivate
        every { mockUserRepository.defaultReadLater } returns defaultReadLater
        every { mockUserRepository.defaultTags } returns defaultTags
        every { mockUserRepository.editAfterSharing } returns EditAfterSharing.AfterSaving

        val post = createPost()
        coEvery {
            mockAddPost(
                Post(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    description = "",
                    private = defaultPrivate,
                    readLater = defaultReadLater,
                    tags = defaultTags,
                ),
            )
        } returns Success(post)

        // WHEN
        shareReceiverViewModel.saveUrl(url = mockUrlValid, title = mockUrlTitle)

        // THEN
        verify {
            mockUserRepository.defaultPrivate
            mockUserRepository.defaultReadLater
            mockUserRepository.defaultTags
        }
        assertThat(shareReceiverViewModel.screenState.first()).isEqualTo(
            ScreenState.Loaded(
                ShareReceiverViewModel.SharingResult.Edit(message = R.string.posts_saved_feedback),
            ),
        )
        coVerify { mockAppStateRepository.runAction(EditPostFromShare(post)) }
    }
}
