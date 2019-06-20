package com.fibelatti.pinboard.core.di

import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.di.modules.AuthModule
import com.fibelatti.pinboard.core.di.modules.CoreModule
import com.fibelatti.pinboard.core.di.modules.DatabaseModule
import com.fibelatti.pinboard.core.di.modules.NetworkModule
import com.fibelatti.pinboard.core.di.modules.FeatureModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        CoreModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        AuthModule::class,
        FeatureModule::class
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
}
