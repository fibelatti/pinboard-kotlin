package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PopularPostsViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository> {
        coEvery { getPost(id = any(), url = any()) } returns Failure(mockk())
    }
    private val mockGetPopularPosts = mockk<GetPopularPosts>()
    private val mockAddPost = mockk<AddPost>()

    private val popularPostsViewModel = PopularPostsViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        postsRepository = mockPostsRepository,
        getPopularPosts = mockGetPopularPosts,
        addPost = mockAddPost,
    )

    @Test
    fun `WHEN PopularPostsContent is emitted AND shouldLoad is true THEN getPosts is called - success`() = runTest {
        // GIVEN
        val mockPosts = mockk<Map<Post, Int>>()
        coEvery { mockGetPopularPosts() } returns Success(mockPosts)

        // WHEN
        appStateFlow.value = createAppState(
            content = PopularPostsContent(
                posts = emptyMap(),
                shouldLoad = true,
                previousContent = createPostListContent(),
            ),
        )

        // THEN
        assertThat(popularPostsViewModel.error.first()).isNull()
        coVerify { mockAppStateRepository.runAction(SetPopularPosts(mockPosts)) }
    }

    @Test
    fun `WHEN PopularPostsContent is emitted AND shouldLoad is true THEN getPosts is called - fast emissions`() =
        runTest {
            // GIVEN
            val mockPosts = mockk<Map<Post, Int>>()
            coEvery { mockGetPopularPosts() } coAnswers {
                delay(2_000)
                Success(mockPosts)
            } andThenAnswer {
                Success(mockPosts)
            }

            // WHEN
            appStateFlow.value = createAppState(
                content = PopularPostsContent(
                    posts = emptyMap(),
                    shouldLoad = true,
                    previousContent = mockk(),
                ),
            )

            advanceTimeBy(1_000)

            appStateFlow.value = createAppState(
                content = PopularPostsContent(
                    posts = emptyMap(),
                    shouldLoad = true,
                    previousContent = mockk(),
                ),
            )

            // THEN
            assertThat(popularPostsViewModel.error.first()).isNull()
            coVerify(exactly = 2) { mockGetPopularPosts() }
            coVerify { mockAppStateRepository.runAction(SetPopularPosts(mockPosts)) }
        }

    @Test
    fun `WHEN PopularPostsContent is emitted AND shouldLoad is true THEN getPosts is called - failure`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockGetPopularPosts() } returns Failure(error)

        // WHEN
        appStateFlow.value = createAppState(
            content = PopularPostsContent(
                posts = emptyMap(),
                shouldLoad = true,
                previousContent = createPostListContent(),
            ),
        )

        // THEN
        assertThat(popularPostsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
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
    fun `WHEN saveLink is called AND add post fails THEN error should receive a value`() = runTest {
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
        runTest {
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
                dateAdded = post.dateAdded,
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
            assertThat(popularPostsViewModel.error.first()).isNull()
            coVerify { mockAppStateRepository.runDelayedAction(PostSaved(post)) }
        }
}
