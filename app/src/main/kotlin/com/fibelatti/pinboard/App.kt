package com.fibelatti.pinboard

import android.app.Application
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.core.di.DaggerAppComponent

class App : Application() {

    val appComponent: AppComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        DaggerAppComponent.factory()
            .create(application = this)
    }
}
