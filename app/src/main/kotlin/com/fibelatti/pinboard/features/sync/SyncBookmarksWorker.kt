package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.user.data.UserDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

@HiltWorker
class SyncBookmarksWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userDataSource: UserDataSource,
    private val postsRepository: PostsRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {

        const val UNIQUE_WORK_NAME = "SyncBookmarksWork"
    }

    override suspend fun doWork(): Result {
        if (!userDataSource.userCredentials.first().hasAuthToken()) return Result.success()

        val success = postsRepository.getAllPosts(
            sortType = ByDateAddedNewestFirst,
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
