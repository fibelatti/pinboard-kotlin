package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ArchivePost
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.features.posts.domain.usecase.UnarchivePost
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PostDetailViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockDeletePost = mockk<DeletePost>()
    private val mockAddPost = mockk<AddPost>()
    private val mockArchivePost = mockk<ArchivePost>()
    private val mockUnarchivePost = mockk<UnarchivePost>()

    private val mockPost = createPost()

    private val postDetailViewModel = PostDetailViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        deletePost = mockDeletePost,
        addPost = mockAddPost,
        archivePost = mockArchivePost,
        unarchivePost = mockUnarchivePost,
    )

    @Test
    fun `WHEN deletePost fails THEN deleteError should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockDeletePost(mockPost) } returns Result.failure(error)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.failure(error),
                updated = Result.success(false),
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN deletePost succeeds THEN appStateRepository should run PostDeleted`() = runTest {
        // GIVEN
        coEvery { mockDeletePost(mockPost) } returns Result.success(Unit)

        // WHEN
        postDetailViewModel.deletePost(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(true),
                updated = Result.success(false),
            ),
        )
        coVerify { mockAppStateRepository.runDelayedAction(PostDeleted) }
    }

    @Test
    fun `WHEN toggleReadLater fails THEN updateError should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockAddPost(any()) } returns Result.failure(error)

        // WHEN
        postDetailViewModel.toggleReadLater(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(false),
                updated = Result.failure(error),
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN toggleReadLater succeeds THEN appStateRepository should run PostSaved`() = runTest {
        // GIVEN
        val randomBoolean = randomBoolean()
        val post = createPost(readLater = randomBoolean)
        val expectedParams = post.copy(
            readLater = !randomBoolean,
        )

        coEvery { mockAddPost(expectedParams) } returns Result.success(mockPost)

        // WHEN
        postDetailViewModel.toggleReadLater(post)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(false),
                updated = Result.success(true),
            ),
        )
        coVerify {
            mockAddPost(expectedParams)
            mockAppStateRepository.runDelayedAction(PostSaved(mockPost))
        }
    }

    @Test
    fun `WHEN toggleArchived fails THEN updateError should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockArchivePost(mockPost) } returns Result.failure(error)

        // WHEN
        postDetailViewModel.toggleArchived(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(false),
                updated = Result.failure(error),
            ),
        )
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN toggleArchived succeeds for a not archived post THEN it should archive and run PostSaved`() = runTest {
        // GIVEN
        val archivedPost = mockPost.copy(isArchived = true)
        coEvery { mockArchivePost(mockPost) } returns Result.success(archivedPost)

        // WHEN
        postDetailViewModel.toggleArchived(mockPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(false),
                updated = Result.success(true),
            ),
        )
        coVerify {
            mockArchivePost(mockPost)
            mockAppStateRepository.runDelayedAction(PostSaved(archivedPost))
        }
    }

    @Test
    fun `WHEN toggleArchived succeeds for an archived post THEN it should unarchive and run PostSaved`() = runTest {
        // GIVEN
        val archivedPost = mockPost.copy(isArchived = true)
        val unarchivedPost = mockPost.copy(isArchived = false)
        coEvery { mockUnarchivePost(archivedPost) } returns Result.success(unarchivedPost)

        // WHEN
        postDetailViewModel.toggleArchived(archivedPost)

        // THEN
        assertThat(postDetailViewModel.screenState.first()).isEqualTo(
            PostDetailViewModel.ScreenState(
                isLoading = false,
                deleted = Result.success(false),
                updated = Result.success(true),
            ),
        )
        coVerify {
            mockUnarchivePost(archivedPost)
            mockAppStateRepository.runDelayedAction(PostSaved(unarchivedPost))
        }
    }
}
