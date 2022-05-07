package com.fibelatti.pinboard.features.sync

import androidx.work.ListenableWorker
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SyncBookmarksWorkerTest {

    private val postsRepository = mockk<PostsRepository>()

    private val worker = SyncBookmarksWorker(
        context = mockk(),
        workerParams = mockk(relaxed = true),
        postsRepository = postsRepository,
    )

    @Test
    fun `when getAllPosts returns only success then the result is equal to success`() = runTest {
        // GIVEN
        coEvery {
            postsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = "",
                tags = null,
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = AppConfig.DEFAULT_PAGE_SIZE,
                pageOffset = 0,
                forceRefresh = false,
            )
        } returns flowOf(Success(mockk()), Success(mockk()))

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when getAllPosts returns a failure then the result is equal to retry`() = runTest {
        // GIVEN
        coEvery {
            postsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = "",
                tags = null,
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = AppConfig.DEFAULT_PAGE_SIZE,
                pageOffset = 0,
                forceRefresh = false,
            )
        } returns flowOf(Success(mockk()), Failure(Exception()))

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }
}
