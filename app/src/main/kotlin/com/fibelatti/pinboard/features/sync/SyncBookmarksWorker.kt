package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.bookmarking.core.Config
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.user.data.UserDataSource
import kotlinx.coroutines.flow.toList

class SyncBookmarksWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val userDataSource: UserDataSource,
    private val postsRepository: PostsRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {

        const val UNIQUE_WORK_NAME = "SyncBookmarksWork"
    }

    override suspend fun doWork(): Result {
        if (!userDataSource.hasAuthToken()) return Result.success()

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
