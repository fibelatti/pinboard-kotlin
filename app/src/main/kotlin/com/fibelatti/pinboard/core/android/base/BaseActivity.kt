package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.webkit.WebView
import androidx.annotation.CallSuper
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentFactory
import com.fibelatti.core.di.ViewModelFactory
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.LightTheme
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity {

    private val injector
        get() = (application as App).appComponent

    @Inject
    lateinit var fragmentFactory: FragmentFactory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var userSharedPreferences: UserSharedPreferences

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
        setupTheme()
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        toast(getString(R.string.generic_msg_error))
        if (BuildConfig.DEBUG) error.printStackTrace()
    }

    private fun setupTheme() {
        workaroundWebViewNightModeIssue()
        if (userSharedPreferences.getAppearance() == LightTheme.value) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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
