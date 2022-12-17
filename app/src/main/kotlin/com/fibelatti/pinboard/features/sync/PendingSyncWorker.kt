package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PendingSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
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
                        PendingSync.ADD, PendingSync.UPDATE -> {
                            postsRepository.add(
                                url = post.url,
                                title = post.title,
                                description = post.description,
                                private = post.private,
                                readLater = post.readLater,
                                tags = post.tags,
                                replace = true,
                                hash = post.hash,
                            )
                        }
                        PendingSync.DELETE -> postsRepository.delete(post.url)
                        null -> Success(Unit)
                    }
                }
            }.awaitAll()
        }.all { it is Success }

        return if (success) Result.success() else Result.retry()
    }
}
