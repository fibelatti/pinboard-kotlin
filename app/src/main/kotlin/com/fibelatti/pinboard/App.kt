package com.fibelatti.pinboard

import android.app.Application
import com.fibelatti.pinboard.core.di.AppComponent
import com.fibelatti.pinboard.core.di.AppComponentProvider
import com.fibelatti.pinboard.core.di.DaggerAppComponent

class App : Application(), AppComponentProvider {

    override val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory()
            .create(application = this)
    }
}
