package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.pinboard.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.prepareToReceiveMany
import com.fibelatti.pinboard.shouldHaveReceived
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

        val loadingObserver = postDetailViewModel.loading.prepareToReceiveMany()

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        postDetailViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        postDetailViewModel.deleteError.currentEventShouldBe(error)
        postDetailViewModel.deleted.shouldNeverReceiveValues()

        coVerify(exactly = 0) { mockAppStateRepository.runAction(PostDeleted) }
    }

    @Test
    fun `WHEN deletePost succeeds THEN appStateRepository should run PostDeleted`() {
        // GIVEN
        coEvery { mockDeletePost(mockPost.url) } returns Success(Unit)

        val loadingObserver = postDetailViewModel.loading.prepareToReceiveMany()

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        postDetailViewModel.loading.shouldHaveReceived(loadingObserver, true)
        postDetailViewModel.error.shouldNeverReceiveValues()
        postDetailViewModel.deleted.currentEventShouldBe(Unit)

        coVerify { mockAppStateRepository.runAction(PostDeleted) }
    }
}
