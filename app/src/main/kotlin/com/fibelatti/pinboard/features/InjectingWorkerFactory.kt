package com.fibelatti.pinboard.features

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.sync.PendingSyncWorker
import com.fibelatti.pinboard.features.sync.SyncBookmarksWorker
import javax.inject.Inject

class InjectingWorkerFactory @Inject constructor(
    private val postsRepository: PostsRepository,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SyncBookmarksWorker::class.java.name -> SyncBookmarksWorker(
            appContext,
            workerParameters,
            postsRepository,
        )
        PendingSyncWorker::class.java.name -> PendingSyncWorker(
            appContext,
            workerParameters,
            postsRepository,
        )
        else -> null
    }
}
