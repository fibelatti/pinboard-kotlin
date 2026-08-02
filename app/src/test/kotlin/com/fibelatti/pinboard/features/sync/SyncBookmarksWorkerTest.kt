package com.fibelatti.pinboard.features.sync

import androidx.work.ListenableWorker
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SyncBookmarksWorkerTest {

    private val mockOfflineCopyRepository = mockk<OfflineCopyRepository>(relaxed = true)
    private val mockAppModeProvider = mockk<AppModeProvider> {
        every { appMode } returns MutableStateFlow(AppMode.PINBOARD)
    }

    private val userDataSource = mockk<UserDataSource> {
        every { userCredentials } returns MutableStateFlow(
            mockk {
                every { hasAuthToken() } returns true
            },
        )
    }

    private val postsRepository = mockk<PostsRepository>()

    private val worker = SyncBookmarksWorker(
        context = mockk(),
        workerParams = mockk(relaxed = true),
        userDataSource = userDataSource,
        postsRepository = postsRepository,
        offlineCopyRepository = mockOfflineCopyRepository,
        appModeProvider = mockAppModeProvider,
    )

    @Test
    fun `when getAllPosts returns only success then the result is equal to success`() = runTest {
        // GIVEN
        coEvery {
            postsRepository.getAllPosts(
                sortType = ByDateAddedNewestFirst,
                searchTerm = "",
                tags = null,
                matchAll = true,
                exactMatch = false,
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                archivedOnly = false,
                countLimit = -1,
                pageLimit = AppConfig.DEFAULT_PAGE_SIZE,
                pageOffset = 0,
                forceRefresh = false,
            )
        } returns flowOf(Result.success(mockk()), Result.success(mockk()))

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify {
            mockOfflineCopyRepository.deleteOrphaned(appMode = AppMode.PINBOARD)
        }
    }

    @Test
    fun `when getAllPosts returns a failure then the result is equal to retry`() = runTest {
        // GIVEN
        coEvery {
            postsRepository.getAllPosts(
                sortType = ByDateAddedNewestFirst,
                searchTerm = "",
                tags = null,
                matchAll = true,
                exactMatch = false,
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                archivedOnly = false,
                countLimit = -1,
                pageLimit = AppConfig.DEFAULT_PAGE_SIZE,
                pageOffset = 0,
                forceRefresh = false,
            )
        } returns flowOf(Result.success(mockk()), Result.failure(Exception()))

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
        verify {
            mockOfflineCopyRepository wasNot Called
        }
    }
}
