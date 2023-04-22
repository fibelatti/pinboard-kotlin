package com.fibelatti.pinboard.features.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PeriodicSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
) {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    fun enqueueWork(shouldReplace: Boolean = false) {
        if (userRepository.periodicSync is PeriodicSync.Off) {
            workManager.cancelUniqueWork(SyncBookmarksWorker.UNIQUE_WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val work = PeriodicWorkRequestBuilder<SyncBookmarksWorker>(userRepository.periodicSync.hours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            SyncBookmarksWorker.UNIQUE_WORK_NAME,
            if (shouldReplace) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            work,
        )
    }
}
