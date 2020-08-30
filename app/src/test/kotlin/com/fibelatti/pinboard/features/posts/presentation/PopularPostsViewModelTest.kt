package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.prepareToReceiveMany
import com.fibelatti.pinboard.features.shouldHaveReceived
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never

internal class PopularPostsViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mock<AppStateRepository>()
    private val mockUserRepository = mock<UserRepository>()
    private val mockGetPopularPosts = mock<GetPopularPosts>()
    private val mockAddPost = mock<AddPost>()

    private val popularPostsViewModel = PopularPostsViewModel(
        mockAppStateRepository,
        mockUserRepository,
        mockGetPopularPosts,
        mockAddPost
    )

    @Test
    fun `WHEN getPosts fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend { mockGetPopularPosts.invoke() }
            .willReturn(Failure(error))

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        popularPostsViewModel.error.currentValueShouldBe(error)
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `WHEN getPosts succeeds THEN AppStateRepository should run SetPopularPosts`() {
        // GIVEN
        val mockPosts = mock<List<Post>>()
        givenSuspend { mockGetPopularPosts() }
            .willReturn(Success(mockPosts))

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetPopularPosts(mockPosts)) }
        popularPostsViewModel.error.shouldNeverReceiveValues()
    }

    @Test
    fun `WHEN saveLink is called AND getEditAfterSharing is BeforeSaving THEN PostSaved action is run AND AddPost is not called`() {
        // GIVEN
        val post = createPost()
        given(mockUserRepository.getEditAfterSharing()).willReturn(EditAfterSharing.BeforeSaving)

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(PostSaved(post)) }
        popularPostsViewModel.loading.shouldNeverReceiveValues()
        verifySuspend(mockAddPost, never()) { invoke(safeAny()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post fails THEN error should receive a value`() {
        // GIVEN
        val post = createPost()
        val error = Exception()
        givenSuspend { mockAddPost(safeAny()) }.willReturn(Failure(error))
        given(mockUserRepository.getEditAfterSharing()).willReturn(mock())

        val loadingObserver = popularPostsViewModel.loading.prepareToReceiveMany()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        popularPostsViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        popularPostsViewModel.error.currentValueShouldBe(error)
        popularPostsViewModel.saved.shouldNeverReceiveValues()

        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny<PostSaved>()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post is successful AND edit after sharing is SkipEdit THEN saved should receive a value`() {
        // GIVEN
        val post = createPost()
        val randomBoolean = randomBoolean()
        val params = AddPost.Params(
            url = post.url,
            title = post.title,
            description = post.description,
            tags = post.tags,
            private = randomBoolean,
            readLater = randomBoolean,
        )
        given(mockUserRepository.getEditAfterSharing()).willReturn(EditAfterSharing.SkipEdit)
        given(mockUserRepository.getDefaultPrivate()).willReturn(randomBoolean)
        given(mockUserRepository.getDefaultReadLater()).willReturn(randomBoolean)
        givenSuspend { mockAddPost(params) }.willReturn(Success(post))

        val loadingObserver = popularPostsViewModel.loading.prepareToReceiveMany()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        popularPostsViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        popularPostsViewModel.saved.currentEventShouldBe(Unit)
        popularPostsViewModel.error.shouldNeverReceiveValues()

        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny<PostSaved>()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post is successful AND edit after sharing is AfterSaving THEN saved should receive a value and PostSave should be run`() {
        // GIVEN
        val post = createPost()
        val randomBoolean = randomBoolean()
        val params = AddPost.Params(
            url = post.url,
            title = post.title,
            description = post.description,
            tags = post.tags,
            private = randomBoolean,
            readLater = randomBoolean
        )
        given(mockUserRepository.getEditAfterSharing()).willReturn(EditAfterSharing.AfterSaving)
        given(mockUserRepository.getDefaultPrivate()).willReturn(randomBoolean)
        given(mockUserRepository.getDefaultReadLater()).willReturn(randomBoolean)
        givenSuspend { mockAddPost(params) }.willReturn(Success(post))

        val loadingObserver = popularPostsViewModel.loading.prepareToReceiveMany()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        popularPostsViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        popularPostsViewModel.saved.currentEventShouldBe(Unit)
        verifySuspend(mockAppStateRepository) { runAction(PostSaved(post)) }
        popularPostsViewModel.error.shouldNeverReceiveValues()
    }
}
