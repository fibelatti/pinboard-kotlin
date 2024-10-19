package com.fibelatti.pinboard.features.sync

import androidx.work.ListenableWorker
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PendingSyncWorkerTest {

    private val postsRepository = mockk<PostsRepository>()

    private val worker = PendingSyncWorker(
        context = mockk(),
        workerParams = mockk(relaxed = true),
        postsRepository = postsRepository,
    )

    @Test
    fun `when getPendingSyncPosts fails then result is equal to retry`() = runTest {
        // GIVEN
        coEvery { postsRepository.getPendingSyncPosts() } returns Failure(Exception())

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `when getPendingSyncPosts returns empty then result is equal to success`() = runTest {
        // GIVEN
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(emptyList())

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when pendingSync is add then add is called`() = runTest {
        // GIVEN
        val post = createPost(pendingSync = PendingSync.ADD)
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(listOf(post))
        coEvery { postsRepository.add(post) } returns Success(post)

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when pendingSync is update then add is called`() = runTest {
        // GIVEN
        val post = createPost(pendingSync = PendingSync.UPDATE)
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(listOf(post))
        coEvery { postsRepository.add(post) } returns Success(post)

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when pendingSync is delete then delete is called`() = runTest {
        // GIVEN
        val post = createPost(pendingSync = PendingSync.DELETE)
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(listOf(post))
        coEvery { postsRepository.delete(post = post) } returns Success(Unit)

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `when some action fails then the result is equal to retry`() = runTest {
        // GIVEN
        val postAdd = createPost(pendingSync = PendingSync.ADD)
        val postDelete = createPost(pendingSync = PendingSync.DELETE)
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(listOf(postAdd, postDelete))
        coEvery { postsRepository.add(postAdd) } returns Failure(Exception())
        coEvery { postsRepository.delete(post = postDelete) } returns Success(Unit)

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `when all actions are successful then the result is equal to success`() = runTest {
        // GIVEN
        val postAdd = createPost(pendingSync = PendingSync.ADD)
        val postDelete = createPost(pendingSync = PendingSync.DELETE)
        coEvery { postsRepository.getPendingSyncPosts() } returns Success(listOf(postAdd, postDelete))
        coEvery { postsRepository.add(postAdd) } returns Success(postAdd)
        coEvery { postsRepository.delete(post = postDelete) } returns Success(Unit)

        // WHEN
        val result = worker.doWork()

        // THEN
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
