package com.fibelatti.pinboard

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.fibelatti.core.android.platform.SimpleActivityLifecycleCallbacks
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.di.allModules
import com.fibelatti.pinboard.features.sync.PendingSyncManager
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

open class App : Application() {

    private val periodicSyncManager: PeriodicSyncManager by inject()
    private val pendingSyncManager: PendingSyncManager by inject()
    private val userRepository: UserRepository by inject()

    override fun onCreate() {
        super.onCreate()

        setupDependencyGraph()

        setupTheme()

        val dynamicColorsOptions = DynamicColorsOptions.Builder()
            .setThemeOverlay(R.style.AppTheme_Overlay)
            .setPrecondition { _, _ -> userRepository.applyDynamicColors }
            .build()
        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorsOptions)

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

        periodicSyncManager.enqueueWork()
        pendingSyncManager.enqueueWorkOnNetworkAvailable()
    }

    protected open fun setupDependencyGraph() {
        startKoin {
            androidLogger()
            androidContext(this@App)
            fragmentFactory()
            workManagerFactory()

            modules(allModules())
        }
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
}
