package com.fibelatti.pinboard.core.di

import android.app.Application
import com.fibelatti.pinboard.core.di.modules.AndroidModule
import com.fibelatti.pinboard.core.di.modules.CoreModule
import com.fibelatti.pinboard.core.di.modules.DatabaseModule
import com.fibelatti.pinboard.core.di.modules.NetworkModule
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        NetworkModule::class,
        DatabaseModule::class,
        CoreModule::class,
        AndroidModule::class
    ]
)
@Singleton
interface AppComponent {

    fun activityComponentFactory(): ActivityComponent.Factory
    fun userRepository(): UserRepository

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance application: Application): AppComponent
    }
}

interface AppComponentProvider {

    val appComponent: AppComponent
}
