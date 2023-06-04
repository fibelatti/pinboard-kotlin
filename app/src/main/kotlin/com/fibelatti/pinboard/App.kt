package com.fibelatti.pinboard

import android.app.Application
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.InjectingWorkerFactory
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
    lateinit var injectingWorkerFactory: InjectingWorkerFactory

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()

        setupTheme()

        val dynamicColorsOptions = DynamicColorsOptions.Builder()
            .setThemeOverlay(R.style.AppTheme_Overlay)
            .setPrecondition { _, _ -> userRepository.applyDynamicColors }
            .build()
        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorsOptions)

        periodicSyncManager.enqueueWork()
        pendingSyncManager.enqueueWorkOnNetworkAvailable()
    }

    private fun setupTheme() {
        workaroundWebViewNightModeIssue()
        val mode = when (userRepository.appearance) {
            Appearance.DarkTheme -> AppCompatDelegate.MODE_NIGHT_YES
            Appearance.LightTheme -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * It turns out there is a strange bug where only the first time a WebView is created, it resets
     * the UI mode. Instantiating a dummy one before calling [AppCompatDelegate.setDefaultNightMode]
     * should be enough so WebViews can be used in the app without any issues.
     */
    private fun workaroundWebViewNightModeIssue() {
        try {
            WebView(this)
        } catch (ignored: Exception) {
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(injectingWorkerFactory)
        .build()
}
