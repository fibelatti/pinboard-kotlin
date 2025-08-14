package com.fibelatti.pinboard

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.fibelatti.core.android.platform.SimpleActivityLifecycleCallbacks
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.sync.PendingSyncManager
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber

@HiltAndroidApp
class App : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var periodicSyncManager: PeriodicSyncManager

    @Inject
    lateinit var pendingSyncManager: PendingSyncManager

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        setupDebugMode()
        setupThemeAndColors()
        setupDisableScreenshots()
        setupWorkers()
    }

    override fun newImageLoader(context: Context): ImageLoader = imageLoader.get()

    @OptIn(ExperimentalComposeRuntimeApi::class)
    private fun setupDebugMode() {
        if (!BuildConfig.DEBUG) return

        Timber.plant(Timber.DebugTree())

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )

        Composer.setDiagnosticStackTraceEnabled(enabled = true)
    }

    private fun setupThemeAndColors() {
        val mode = when (userRepository.appearance) {
            Appearance.DarkTheme -> AppCompatDelegate.MODE_NIGHT_YES
            Appearance.LightTheme -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(mode)

        val dynamicColorsOptions = DynamicColorsOptions.Builder()
            .setThemeOverlay(R.style.AppTheme_Overlay)
            .setPrecondition { _, _ -> userRepository.applyDynamicColors }
            .build()

        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorsOptions)
    }

    private fun setupDisableScreenshots() {
        registerActivityLifecycleCallbacks(
            object : SimpleActivityLifecycleCallbacks() {
                override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                    if (userRepository.disableScreenshots) {
                        p0.window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE,
                        )
                    }
                }
            },
        )
    }

    private fun setupWorkers() {
        periodicSyncManager.enqueueWork()
        pendingSyncManager.setupListeners()
    }
}
