package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.pinboard.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.prepareToReceiveMany
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.shouldHaveReceived
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class PopularPostsViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>()
    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockGetPopularPosts = mockk<GetPopularPosts>()
    private val mockAddPost = mockk<AddPost>()

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
        coEvery { mockGetPopularPosts.invoke() } returns Failure(error)

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        popularPostsViewModel.error.currentValueShouldBe(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getPosts succeeds THEN AppStateRepository should run SetPopularPosts`() {
        // GIVEN
        val mockPosts = mockk<List<Post>>()
        coEvery { mockGetPopularPosts() } returns Success(mockPosts)

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        coVerify { mockAppStateRepository.runAction(SetPopularPosts(mockPosts)) }
        popularPostsViewModel.error.shouldNeverReceiveValues()
    }

    @Test
    fun `WHEN saveLink is called AND getEditAfterSharing is BeforeSaving THEN PostSaved action is run AND AddPost is not called`() {
        // GIVEN
        val post = createPost()
        every { mockUserRepository.getEditAfterSharing() } returns EditAfterSharing.BeforeSaving

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        coVerify { mockAppStateRepository.runAction(PostSaved(post)) }
        popularPostsViewModel.loading.shouldNeverReceiveValues()
        coVerify(exactly = 0) { mockAddPost.invoke(any()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post fails THEN error should receive a value`() {
        // GIVEN
        val post = createPost()
        val error = Exception()
        coEvery { mockAddPost(any()) } returns Failure(error)
        every { mockUserRepository.getEditAfterSharing() } returns mockk()

        val loadingObserver = popularPostsViewModel.loading.prepareToReceiveMany()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        popularPostsViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        popularPostsViewModel.error.currentValueShouldBe(error)
        popularPostsViewModel.saved.shouldNeverReceiveValues()

        coVerify(exactly = 0) { mockAppStateRepository.runAction(any<PostSaved>()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post is successful THEN saved should receive a value and PostSave should be run`() {
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
        every { mockUserRepository.getDefaultPrivate() } returns randomBoolean
        every { mockUserRepository.getDefaultReadLater() } returns randomBoolean
        coEvery { mockAddPost(params) } returns Success(post)

        val loadingObserver = popularPostsViewModel.loading.prepareToReceiveMany()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        popularPostsViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        popularPostsViewModel.saved.currentEventShouldBe(Unit)
        coVerify { mockAppStateRepository.runAction(PostSaved(post)) }
        popularPostsViewModel.error.shouldNeverReceiveValues()
    }
}
