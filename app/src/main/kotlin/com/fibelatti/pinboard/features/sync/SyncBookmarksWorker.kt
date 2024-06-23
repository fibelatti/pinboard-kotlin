package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.bookmarking.core.Config
import com.fibelatti.bookmarking.features.appstate.NewestFirst
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.core.functional.Success
import kotlinx.coroutines.flow.toList

class SyncBookmarksWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {

        const val UNIQUE_WORK_NAME = "SyncBookmarksWork"
    }

    override suspend fun doWork(): Result {
        if (!userRepository.hasAuthToken()) return Result.success()

        val success = postsRepository.getAllPosts(
            sortType = NewestFirst,
            searchTerm = "",
            tags = null,
            untaggedOnly = false,
            postVisibility = PostVisibility.None,
            readLaterOnly = false,
            countLimit = -1,
            pageLimit = Config.LOCAL_PAGE_SIZE,
            pageOffset = 0,
            forceRefresh = false,
        ).toList().all { it is Success }

        return if (success) Result.success() else Result.retry()
    }
}
