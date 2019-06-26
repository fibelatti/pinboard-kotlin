package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.BaseViewModelTest
import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.features.prepareToReceiveMany
import com.fibelatti.pinboard.features.shouldHaveReceived
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never

internal class PostDetailViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mock<AppStateRepository>()
    private val mockDeletePost = mock<DeletePost>()

    private val mockPost = createPost()

    private val postDetailViewModel = PostDetailViewModel(
        mockAppStateRepository,
        mockDeletePost
    )

    @Test
    fun `WHEN deletePost fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend { mockDeletePost(mockPost.url) }
            .willReturn(Failure(error))

        val loadingObserver = postDetailViewModel.loading.prepareToReceiveMany()

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        postDetailViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        postDetailViewModel.error.currentValueShouldBe(error)
        postDetailViewModel.deleted.shouldNeverReceiveValues()

        verifySuspend(mockAppStateRepository, never()) { runAction(PostDeleted) }
    }

    @Test
    fun `WHEN deletePost succeeds THEN appStateRepository should run PostDeleted`() {
        // GIVEN
        givenSuspend { mockDeletePost(mockPost.url) }
            .willReturn(Success(Unit))

        val loadingObserver = postDetailViewModel.loading.prepareToReceiveMany()

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        postDetailViewModel.loading.shouldHaveReceived(loadingObserver, true)
        postDetailViewModel.error.shouldNeverReceiveValues()
        postDetailViewModel.deleted.currentEventShouldBe(Unit)

        verifySuspend(mockAppStateRepository) { runAction(PostDeleted) }
    }
}
