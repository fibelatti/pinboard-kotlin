package com.fibelatti.pinboard.core.di

import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.di.modules.CoreModule
import com.fibelatti.pinboard.core.di.modules.PreferencesModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        CoreModule::class,
        PreferencesModule::class
    ]
)
@Singleton
interface AppComponent : Injector {

    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun application(application: App): Builder
    }

    fun inject(application: App)
}
