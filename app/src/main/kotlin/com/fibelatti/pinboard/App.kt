package com.fibelatti.pinboard

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.core.di.DaggerAppComponent

class App : Application() {
    val appComponent: AppComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent.inject(this)
    }
}
