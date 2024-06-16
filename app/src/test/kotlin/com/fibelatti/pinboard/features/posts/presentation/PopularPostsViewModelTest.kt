package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.isEmpty
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PopularPostsViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository> {
        coEvery { getPost(id = any(), url = any()) } returns Failure(mockk())
    }
    private val mockGetPopularPosts = mockk<GetPopularPosts>()
    private val mockAddPost = mockk<AddPost>()

    private val popularPostsViewModel = PopularPostsViewModel(
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        postsRepository = mockPostsRepository,
        getPopularPosts = mockGetPopularPosts,
        addPost = mockAddPost,
    )

    @Test
    fun `WHEN getPosts fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockGetPopularPosts.invoke() } returns Failure(error)

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        assertThat(popularPostsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getPosts succeeds THEN AppStateRepository should run SetPopularPosts`() = runTest {
        // GIVEN
        val mockPosts = mockk<List<Post>>()
        coEvery { mockGetPopularPosts() } returns Success(mockPosts)

        // WHEN
        popularPostsViewModel.getPosts()

        // THEN
        coVerify { mockAppStateRepository.runAction(SetPopularPosts(mockPosts)) }
        assertThat(popularPostsViewModel.error.isEmpty()).isTrue()
    }

    @Test
    fun `WHEN saveLink is called AND existing post is not null THEN PostSaved action is run AND AddPost is not called`() =
        runTest {
            // GIVEN
            val post = createPost()
            coEvery { mockPostsRepository.getPost(id = post.id, url = post.url) } returns Success(post)

            // WHEN
            popularPostsViewModel.saveLink(post)

            // THEN
            coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post)) }
            assertThat(popularPostsViewModel.screenState.first()).isEqualTo(
                PopularPostsViewModel.ScreenState(
                    isLoading = false,
                    savedMessage = R.string.posts_existing_feedback,
                ),
            )
            coVerify(exactly = 0) {
                mockUserRepository.editAfterSharing
                mockAddPost.invoke(any())
            }
        }

    @Test
    fun `WHEN saveLink is called AND getEditAfterSharing is BeforeSaving THEN PostSaved action is run AND AddPost is not called`() =
        runTest {
            // GIVEN
            val post = createPost()
            every { mockUserRepository.editAfterSharing } returns EditAfterSharing.BeforeSaving

            // WHEN
            popularPostsViewModel.saveLink(post)

            // THEN
            coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post.copy(tags = emptyList()))) }
            assertThat(popularPostsViewModel.screenState.first()).isEqualTo(
                PopularPostsViewModel.ScreenState(
                    isLoading = false,
                    savedMessage = null,
                ),
            )
            coVerify(exactly = 0) { mockAddPost.invoke(any()) }
        }

    @Test
    fun `WHEN saveLink is called AND add post fails THEN error should receive a value`() = runUnconfinedTest {
        // GIVEN
        val post = createPost()
        val error = Exception()
        coEvery { mockAddPost(any()) } returns Failure(error)
        every { mockUserRepository.editAfterSharing } returns mockk()

        // WHEN
        popularPostsViewModel.saveLink(post)

        // THEN
        assertThat(popularPostsViewModel.screenState.first()).isEqualTo(
            PopularPostsViewModel.ScreenState(
                isLoading = false,
                savedMessage = null,
            ),
        )
        assertThat(popularPostsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any<PostSaved>()) }
    }

    @Test
    fun `WHEN saveLink is called AND add post is successful THEN saved should receive a value and PostSave should be run`() =
        runUnconfinedTest {
            // GIVEN
            val post = createPost(
                tags = null,
            )
            val randomBoolean = randomBoolean()
            val mockTags = mockk<List<Tag>>()
            val params = Post(
                url = post.url,
                title = post.title,
                description = post.description,
                tags = mockTags,
                private = randomBoolean,
                readLater = randomBoolean,
                id = post.id,
                time = post.time,
            )
            every { mockUserRepository.defaultPrivate } returns randomBoolean
            every { mockUserRepository.defaultReadLater } returns randomBoolean
            every { mockUserRepository.defaultTags } returns mockTags
            coEvery { mockAddPost(params) } returns Success(post)

            // WHEN
            popularPostsViewModel.saveLink(post)

            // THEN
            assertThat(popularPostsViewModel.screenState.first()).isEqualTo(
                PopularPostsViewModel.ScreenState(
                    isLoading = false,
                    savedMessage = R.string.posts_saved_feedback,
                ),
            )
            assertThat(popularPostsViewModel.error.isEmpty()).isTrue()
            coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post)) }
        }
}
