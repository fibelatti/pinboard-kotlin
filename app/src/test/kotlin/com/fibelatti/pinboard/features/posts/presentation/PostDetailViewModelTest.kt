package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Test

internal class PostDetailViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockDeletePost = mockk<DeletePost>()
    private val mockAddPost = mockk<AddPost>()

    private val mockPost = createPost()

    private val postDetailViewModel = PostDetailViewModel(
        mockAppStateRepository,
        mockDeletePost,
        mockAddPost,
    )

    @Test
    fun `WHEN deletePost fails THEN deleteError should receive a value`() = runUnconfinedTest {
        // GIVEN
        val error = Exception()
        coEvery { mockDeletePost(mockPost.url) } returns Failure(error)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Failure(error),
                updated = Success(false),
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN deletePost succeeds THEN appStateRepository should run PostDeleted`() = runUnconfinedTest {
        // GIVEN
        coEvery { mockDeletePost(mockPost.url) } returns Success(Unit)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Success(true),
                updated = Success(false),
            ),
        )
        coVerify { mockAppStateRepository.runAction(PostDeleted) }
    }

    @Test
    fun `WHEN toggleReadLater fails THEN updateError should receive a value`() = runUnconfinedTest {
        // GIVEN
        val error = Exception()
        coEvery { mockAddPost(any()) } returns Failure(error)

        // WHEN
        postDetailViewModel.toggleReadLater(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Success(false),
                updated = Failure(error),
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN toggleReadLater succeeds THEN appStateRepository should run PostSaved`() = runUnconfinedTest {
        // GIVEN
        val randomBoolean = randomBoolean()
        val post = createPost(readLater = randomBoolean)
        val expectedParams = post.copy(
            readLater = !randomBoolean,
        )

        coEvery { mockAddPost(expectedParams) } returns Success(mockPost)

        // WHEN
        postDetailViewModel.toggleReadLater(post)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Success(false),
                updated = Success(true),
            ),
        )
        coVerify {
            mockAddPost(expectedParams)
            mockAppStateRepository.runAction(PostSaved(mockPost))
        }
    }
}
