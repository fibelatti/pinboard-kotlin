package com.fibelatti.pinboard

import android.app.Application
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.fibelatti.pinboard.features.InjectingWorkerFactory
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var periodicSyncManager: PeriodicSyncManager
    @Inject
    lateinit var injectingWorkerFactory: InjectingWorkerFactory

    override fun onCreate() {
        super.onCreate()

        periodicSyncManager.enqueueWork()
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(
            DelegatingWorkerFactory().apply {
                addFactory(injectingWorkerFactory)
            }
        )
        .build()
}
