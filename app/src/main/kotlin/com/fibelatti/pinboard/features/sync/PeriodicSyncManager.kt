package com.fibelatti.pinboard.features.sync

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PeriodicSyncManager @Inject constructor(
    application: Application,
) {

    private val workManager: WorkManager = WorkManager.getInstance(application)

    fun enqueue(periodicSync: PeriodicSync, shouldReplace: Boolean = false) {
        if (periodicSync is PeriodicSync.Off) {
            workManager.cancelUniqueWork(SyncBookmarksWorker.UNIQUE_WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val work = PeriodicWorkRequestBuilder<SyncBookmarksWorker>(periodicSync.hours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            SyncBookmarksWorker.UNIQUE_WORK_NAME,
            if (shouldReplace) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }
}
