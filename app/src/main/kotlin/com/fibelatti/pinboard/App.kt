package com.fibelatti.pinboard

import android.app.Application
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.core.di.AppComponentProvider
import com.fibelatti.pinboard.core.di.DaggerAppComponent
import com.fibelatti.pinboard.features.InjectingWorkerFactory

class App : Application(), AppComponentProvider, Configuration.Provider {

    override val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory()
            .create(application = this)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.periodicSyncManager().enqueue(
            periodicSync = appComponent.userRepository().periodicSync
        )
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
