package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.persistence.CurrentInstallSharedPreferences
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    val injector get() = (application as App).appComponent

    //    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var currentInstallSharedPreferences: CurrentInstallSharedPreferences

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        setupTheme()
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        error.printStackTrace()
    }

    private fun setupTheme() {
        if (currentInstallSharedPreferences.getTheme() == AppConfig.AppTheme.CLASSIC) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
