package com.fibelatti.pinboard

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fibelatti.core.android.platform.SimpleActivityLifecycleCallbacks
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.sync.PendingSyncManager
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var periodicSyncManager: PeriodicSyncManager

    @Inject
    lateinit var pendingSyncManager: PendingSyncManager

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var userRepository: UserRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        setupStrictMode()
        setupThemeAndColors()
        setupDisableScreenshots()
        setupWorkers()
    }

    private fun setupStrictMode() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
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
