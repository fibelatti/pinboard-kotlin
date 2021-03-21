package com.fibelatti.pinboard

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.DelegatingWorkerFactory
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.core.di.AppComponentProvider
import com.fibelatti.pinboard.core.di.DaggerAppComponent
import com.fibelatti.pinboard.features.InjectingWorkerFactory
import com.fibelatti.pinboard.features.SyncBookmarksWorker
import java.util.concurrent.TimeUnit

class App : Application(), AppComponentProvider, Configuration.Provider {

    override val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory()
            .create(application = this)
    }

    override fun onCreate() {
        super.onCreate()
        enqueueSyncBookmarksWork()
    }

    @Suppress("MagicNumber")
    private fun enqueueSyncBookmarksWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val work = PeriodicWorkRequestBuilder<SyncBookmarksWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("SyncBookmarksWork", ExistingPeriodicWorkPolicy.KEEP, work)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(
            DelegatingWorkerFactory().apply {
                addFactory(InjectingWorkerFactory(appComponent))
            }
        )
        .build()
}
