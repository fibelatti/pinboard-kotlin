package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@HiltWorker
class PendingSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val postsRepository: PostsRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {

        const val UNIQUE_WORK_NAME = "PendingSyncWorker"
    }

    override suspend fun doWork(): Result {
        val pendingSyncPosts = postsRepository.getPendingSyncPosts().getOrNull() ?: return Result.retry()

        if (pendingSyncPosts.isEmpty()) return Result.success()

        val success = coroutineScope {
            pendingSyncPosts.map { post ->
                async {
                    when (post.pendingSync) {
                        PendingSync.ADD, PendingSync.UPDATE -> postsRepository.add(post = post)
                        PendingSync.DELETE -> postsRepository.delete(post = post)
                        null -> Success(Unit)
                    }
                }
            }.awaitAll()
        }.all { it is Success }

        return if (success) Result.success() else Result.retry()
    }
}
