package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.OfflineCopySaved
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.offline.domain.SaveOfflineCopy
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ArchivePost
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.features.posts.domain.usecase.UnarchivePost
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PostDetailViewModelTest : BaseViewModelTest() {

    private val appStateFlow: MutableStateFlow<AppState> = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true) {
        every { appState } returns appStateFlow
    }
    private val mockDeletePost = mockk<DeletePost>()
    private val mockAddPost = mockk<AddPost>()
    private val mockArchivePost = mockk<ArchivePost>()
    private val mockUnarchivePost = mockk<UnarchivePost>()
    private val mockSaveOfflineCopy = mockk<SaveOfflineCopy>()
    private val mockOfflineCopyRepository = mockk<OfflineCopyRepository>(relaxed = true)

    private val mockPost = createPost()

    private val postDetailViewModel = PostDetailViewModel(
        dispatcher = dispatcher,
        appStateRepository = mockAppStateRepository,
        deletePost = mockDeletePost,
        addPost = mockAddPost,
        archivePost = mockArchivePost,
        unarchivePost = mockUnarchivePost,
        saveOfflineCopy = mockSaveOfflineCopy,
        offlineCopyRepository = mockOfflineCopyRepository,
    )

    @Nested
    inner class OfflineCopyTests {

        @Test
        fun `WHEN the content becomes PostDetailContent THEN its offline copy is exposed`() = runTest {
            // GIVEN the copy is resolved by the action handler and travels on the content
            val offlineCopy = mockk<OfflineCopy>()
            val file = File("offline.html")
            every { mockOfflineCopyRepository.fileFor(offlineCopy) } returns file

            // WHEN
            appStateFlow.value = createAppState(content = createPostDetailContent(offlineCopy = offlineCopy))

            // THEN
            assertThat(postDetailViewModel.screenState.first()).isEqualTo(
                PostDetailViewModel.ScreenState(
                    offlineCopy = offlineCopy,
                    offlineCopyFile = file,
                ),
            )
        }

        @Test
        fun `WHEN the bookmark has no offline copy THEN the state stays empty`() = runTest {
            // WHEN
            appStateFlow.value = createAppState(content = createPostDetailContent(offlineCopy = null))

            // THEN
            assertThat(postDetailViewModel.screenState.first()).isEqualTo(PostDetailViewModel.ScreenState())
            verify(exactly = 0) { mockOfflineCopyRepository.fileFor(any()) }
        }

        @Test
        fun `WHEN saveOfflineCopy succeeds THEN the copy is published through the app state`() = runTest {
            // GIVEN
            val offlineCopy = mockk<OfflineCopy>()
            coEvery { mockSaveOfflineCopy(any()) } returns Result.success(offlineCopy)

            // WHEN
            postDetailViewModel.saveOfflineCopy(mockPost)

            // THEN the copy is not written into the screen state directly: it goes through the app
            // state so the content carries it back, keeping a single source of truth.
            assertThat(postDetailViewModel.screenState.first()).isEqualTo(
                PostDetailViewModel.ScreenState(
                    isSavingOfflineCopy = false,
                    offlineCopySaved = Result.success(true),
                ),
            )
            coVerify {
                mockSaveOfflineCopy(SaveOfflineCopy.Params(post = mockPost, appMode = AppMode.PINBOARD))
                mockAppStateRepository.runAction(OfflineCopySaved(offlineCopy))
            }
        }

        @Test
        fun `WHEN saveOfflineCopy fails THEN the failure is exposed AND nothing is published`() = runTest {
            // GIVEN
            val error = Exception()
            coEvery { mockSaveOfflineCopy(any()) } returns Result.failure(error)

            // WHEN
            postDetailViewModel.saveOfflineCopy(mockPost)

            // THEN publishing on failure would put a copy into the app state that was never saved.
            assertThat(postDetailViewModel.screenState.first()).isEqualTo(
                PostDetailViewModel.ScreenState(
                    isSavingOfflineCopy = false,
                    offlineCopySaved = Result.failure(error),
                ),
            )
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any<OfflineCopySaved>()) }
        }

        @Test
        fun `WHEN saveOfflineCopy is called again while one is running THEN it is ignored`() = runTest {
            // GIVEN a capture that has not finished yet — it is slow enough for a second tap to land
            val ongoing = CompletableDeferred<Result<OfflineCopy>>()
            coEvery { mockSaveOfflineCopy(any()) } coAnswers { ongoing.await() }

            // WHEN
            postDetailViewModel.saveOfflineCopy(mockPost)
            postDetailViewModel.saveOfflineCopy(mockPost)

            // THEN two captures of the same bookmark would race over the same file and row
            assertThat(postDetailViewModel.screenState.first().isSavingOfflineCopy).isTrue()
            coVerify(exactly = 1) { mockSaveOfflineCopy(any()) }

            ongoing.complete(Result.success(mockk()))
        }

        @Test
        fun `WHEN a capture has finished THEN another one can start`() = runTest {
            // GIVEN
            coEvery { mockSaveOfflineCopy(any()) } returns Result.success(mockk())

            // WHEN
            postDetailViewModel.saveOfflineCopy(mockPost)
            postDetailViewModel.saveOfflineCopy(mockPost)

            // THEN the guard must not latch, or saving would work exactly once per screen
            coVerify(exactly = 2) { mockSaveOfflineCopy(any()) }
        }

        @Test
        fun `WHEN the same bookmark is republished mid capture THEN it still reports saving`() = runTest {
            // GIVEN a capture in flight
            val ongoing = CompletableDeferred<Result<OfflineCopy>>()
            coEvery { mockSaveOfflineCopy(any()) } coAnswers { ongoing.await() }
            postDetailViewModel.saveOfflineCopy(mockPost)

            // WHEN the content is republished, as saving a copy itself causes
            appStateFlow.value = createAppState(content = createPostDetailContent(offlineCopy = null))

            // THEN the bookmark on screen is the one being captured, so the progress belongs to it
            assertThat(postDetailViewModel.screenState.first().isSavingOfflineCopy).isTrue()

            ongoing.complete(Result.success(mockk()))
        }

        @Test
        fun `WHEN another bookmark is opened mid capture THEN it does not report saving`() = runTest {
            // GIVEN a capture of one bookmark in flight
            val ongoing = CompletableDeferred<Result<OfflineCopy>>()
            coEvery { mockSaveOfflineCopy(any()) } coAnswers { ongoing.await() }
            postDetailViewModel.saveOfflineCopy(mockPost)

            // WHEN the user moves to a different bookmark
            val otherPost = createPost(id = "another-bookmark")
            appStateFlow.value = createAppState(
                content = createPostDetailContent(offlineCopy = null, post = otherPost),
            )

            // THEN progress belongs to the bookmark being captured, not to whatever is on screen
            assertThat(postDetailViewModel.screenState.first().isSavingOfflineCopy).isFalse()

            ongoing.complete(Result.success(mockk()))
        }

        @Test
        fun `WHEN a capture is in flight THEN another bookmark can still start one`() = runTest {
            // GIVEN the quick actions let the user queue up several bookmarks in a row, and the
            // guard only exists to stop one bookmark racing against itself
            val ongoing = CompletableDeferred<Result<OfflineCopy>>()
            coEvery { mockSaveOfflineCopy(any()) } coAnswers { ongoing.await() }
            val otherPost = createPost(id = "another-bookmark")

            // WHEN
            postDetailViewModel.saveOfflineCopy(mockPost)
            postDetailViewModel.saveOfflineCopy(otherPost)

            // THEN
            coVerify(exactly = 1) {
                mockSaveOfflineCopy(SaveOfflineCopy.Params(post = mockPost, appMode = AppMode.PINBOARD))
                mockSaveOfflineCopy(SaveOfflineCopy.Params(post = otherPost, appMode = AppMode.PINBOARD))
            }

            ongoing.complete(Result.success(mockk()))
        }

        @Test
        fun `WHEN one of several captures finishes THEN the others are still reported as running`() = runTest {
            // GIVEN two captures in flight, the second of which is the bookmark on screen
            val first = CompletableDeferred<Result<OfflineCopy>>()
            val otherPost = createPost(id = "another-bookmark")
            coEvery {
                mockSaveOfflineCopy(SaveOfflineCopy.Params(post = mockPost, appMode = AppMode.PINBOARD))
            } coAnswers { first.await() }
            coEvery {
                mockSaveOfflineCopy(SaveOfflineCopy.Params(post = otherPost, appMode = AppMode.PINBOARD))
            } coAnswers { CompletableDeferred<Result<OfflineCopy>>().await() }

            appStateFlow.value = createAppState(content = createPostDetailContent(offlineCopy = null, post = otherPost))

            postDetailViewModel.saveOfflineCopy(mockPost)
            postDetailViewModel.saveOfflineCopy(otherPost)

            // WHEN the capture that is not on screen completes
            first.complete(Result.success(mockk()))

            // THEN clearing the flag outright would drop the progress of the one still running
            assertThat(postDetailViewModel.screenState.first().isSavingOfflineCopy).isTrue()
        }

        @Test
        fun `WHEN toggleViewingOfflineCopy is called THEN it alternates`() = runTest {
            assertThat(postDetailViewModel.screenState.first().viewingOfflineCopy).isFalse()

            postDetailViewModel.toggleViewingOfflineCopy()
            assertThat(postDetailViewModel.screenState.first().viewingOfflineCopy).isTrue()

            postDetailViewModel.toggleViewingOfflineCopy()
            assertThat(postDetailViewModel.screenState.first().viewingOfflineCopy).isFalse()
        }

        @Test
        fun `WHEN deletePost succeeds THEN the offline copy is deleted too`() = runTest {
            // GIVEN
            coEvery { mockDeletePost(mockPost) } returns Result.success(Unit)

            // WHEN
            postDetailViewModel.deletePost(mockPost)

            // THEN the bookmark is gone, so its saved copy has nothing left to belong to.
            coVerify {
                mockOfflineCopyRepository.delete(appMode = AppMode.PINBOARD, bookmarkId = mockPost.id)
            }
        }

        @Test
        fun `WHEN deletePost fails THEN the offline copy is kept`() = runTest {
            // GIVEN
            coEvery { mockDeletePost(mockPost) } returns Result.failure(Exception())

            // WHEN
            postDetailViewModel.deletePost(mockPost)

            // THEN the bookmark still exists, so deleting its copy would lose it for nothing.
            coVerify(exactly = 0) { mockOfflineCopyRepository.delete(any(), any()) }
        }

        @Test
        fun `WHEN userNotified is called THEN the offline copy result is cleared`() = runTest {
            // GIVEN
            coEvery { mockSaveOfflineCopy(any()) } returns Result.failure(Exception())
            postDetailViewModel.saveOfflineCopy(mockPost)

            // WHEN
            postDetailViewModel.userNotified()

            // THEN the banner must not fire again on the next state change.
            assertThat(postDetailViewModel.screenState.first().offlineCopySaved).isEqualTo(Result.success(false))
        }

        private fun createPostDetailContent(
            offlineCopy: OfflineCopy?,
            post: Post = mockPost,
        ): PostDetailContent = PostDetailContent(
            post = post,
            previousContent = mockk(),
            offlineCopy = offlineCopy,
        )
    }

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
