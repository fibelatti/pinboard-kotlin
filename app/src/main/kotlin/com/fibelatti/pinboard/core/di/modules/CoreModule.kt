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

@Module
abstract class CoreModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun moshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        @Provides
        @JvmStatic
        fun localeDefault(): Locale = Locale.getDefault()

        @Provides
        @JvmStatic
        fun usCollator(): Collator = Collator.getInstance(Locale.US)
    }

    @Binds
    abstract fun context(app: App): Context

    @Binds
    abstract fun resourceProvider(appResourceProvider: AppResourceProvider): ResourceProvider

    @Binds
    abstract fun coroutineLaunche(coroutineLauncherDelegate: CoroutineLauncherDelegate): CoroutineLauncher

    @Binds
    abstract fun viewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
