package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.webkit.WebView
import androidx.annotation.CallSuper
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentFactory
import com.fibelatti.core.archcomponents.ViewModelFactory
import com.fibelatti.core.extension.toast
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.user.data.UserDataSource
import javax.inject.Inject

abstract class BaseActivity @ContentView constructor(
    @LayoutRes contentLayoutId: Int
) : AppCompatActivity(contentLayoutId) {

    private val injector
        get() = (application as App).appComponent

    @Inject
    lateinit var fragmentFactory: FragmentFactory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var userDataSource: UserDataSource

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
        setupTheme()
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        toast(getString(R.string.generic_msg_error))
        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }
    }

    private fun setupTheme() {
        workaroundWebViewNightModeIssue()
        when (userDataSource.getAppearance()) {
            Appearance.DarkTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Appearance.LightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
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
