package com.fibelatti.pinboard.features

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.features.sync.SyncBookmarksWorker

class InjectingWorkerFactory(
    private val appComponent: AppComponent,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SyncBookmarksWorker::class.java.name -> SyncBookmarksWorker(
            appContext,
            workerParameters,
            appComponent.postsRepository(),
        )
        else -> null
    }
}
