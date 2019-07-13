package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.di.ViewModelFactory
import com.fibelatti.core.extension.getSystemService
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.di.MultiBindingFragmentFactory
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.text.Collator
import java.util.Locale
import javax.inject.Singleton

@Module
abstract class CoreModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun gson(): Gson = Gson()

        @Provides
        @JvmStatic
        fun localeDefault(): Locale = Locale.getDefault()

        @Provides
        @JvmStatic
        fun usCollator(): Collator = Collator.getInstance(Locale.US)

        @Provides
        @JvmStatic
        fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getUserPreferences()

        @Provides
        @JvmStatic
        fun connectivityManager(context: Context): ConnectivityManager? =
            context.getSystemService<ConnectivityManager>()
    }

    @Binds
    abstract fun context(app: App): Context

    @Binds
    abstract fun resourceProvider(appResourceProvider: AppResourceProvider): ResourceProvider

    @Binds
    @Singleton
    abstract fun viewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    abstract fun fragmentFactory(factory: MultiBindingFragmentFactory): FragmentFactory
}
