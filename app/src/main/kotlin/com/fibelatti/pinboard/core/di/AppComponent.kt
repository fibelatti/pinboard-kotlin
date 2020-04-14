package com.fibelatti.pinboard.core.di

import android.app.Application
import com.fibelatti.pinboard.core.di.modules.AuthModule
import com.fibelatti.pinboard.core.di.modules.CoreModule
import com.fibelatti.pinboard.core.di.modules.DatabaseModule
import com.fibelatti.pinboard.core.di.modules.FeatureModule
import com.fibelatti.pinboard.core.di.modules.NetworkModule
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

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}
