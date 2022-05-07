package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import kotlinx.coroutines.flow.toList

class SyncBookmarksWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val postsRepository: PostsRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "SyncBookmarksWork"
    }

    override suspend fun doWork(): Result {
        val success = postsRepository.getAllPosts(
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
        ).toList().all { it is Success }

        return if (success) Result.success() else Result.retry()
    }
}
