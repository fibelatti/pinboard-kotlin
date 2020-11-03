package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class PostDetailViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>()
    private val mockDeletePost = mockk<DeletePost>()

    private val mockPost = createPost()

    private val postDetailViewModel = PostDetailViewModel(
        mockAppStateRepository,
        mockDeletePost
    )

    @Test
    fun `WHEN deletePost fails THEN deleteError should receive a value`() {
        // GIVEN
        val error = Exception()
        coEvery { mockDeletePost(mockPost.url) } returns Failure(error)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        runBlocking {
            assertThat(postDetailViewModel.loading.first()).isEqualTo(false)
            assertThat(postDetailViewModel.deleteError.first()).isEqualTo(error)
            assertThat(postDetailViewModel.deleted.isEmpty()).isTrue()
        }
        coVerify(exactly = 0) { mockAppStateRepository.runAction(PostDeleted) }
    }

    @Test
    fun `WHEN deletePost succeeds THEN appStateRepository should run PostDeleted`() {
        // GIVEN
        coEvery { mockDeletePost(mockPost.url) } returns Success(Unit)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        runBlocking {
            assertThat(postDetailViewModel.loading.first()).isEqualTo(true)
            assertThat(postDetailViewModel.error.isEmpty()).isTrue()
            assertThat(postDetailViewModel.deleted.first()).isEqualTo(Unit)
        }

        coVerify { mockAppStateRepository.runAction(PostDeleted) }
    }
}
