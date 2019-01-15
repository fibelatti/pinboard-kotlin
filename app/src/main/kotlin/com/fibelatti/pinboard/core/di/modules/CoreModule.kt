package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.provider.CoroutineLauncher
import com.fibelatti.core.provider.CoroutineLauncherDelegate
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.di.modules.viewmodel.ViewModelFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.text.Collator
import java.util.Locale

@Module(
    includes = [
        CoreModule.Binder::class
    ]
)
object CoreModule {
    @Module
    interface Binder {
        @Binds
        fun bindContext(app: App): Context

        @Binds
        fun bindResourceProvider(appResourceProvider: AppResourceProvider): ResourceProvider

        @Binds
        fun bindCoroutineLauncher(coroutineLauncherDelegate: CoroutineLauncherDelegate): CoroutineLauncher

        @Binds
        fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
    }

    @Provides
    @JvmStatic
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @JvmStatic
    fun provideLocaleDefault(): Locale = Locale.getDefault()

    @Provides
    @JvmStatic
    fun provideUSCollator(): Collator = Collator.getInstance(Locale.US)
}
