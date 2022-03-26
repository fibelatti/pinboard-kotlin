package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.extension.getSystemService
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.text.Collator
import java.util.Locale

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {

    @Provides
    @MainVariant
    fun mainVariant(): Boolean = BuildConfig.FLAVOR == "pinboardapi"

    @Provides
    fun localeDefault(): Locale = Locale.getDefault()

    @Provides
    fun usCollator(): Collator = Collator.getInstance(Locale.US)

    @Provides
    fun sharedPreferences(
        application: Application,
    ): SharedPreferences = application.getUserPreferences()

    @Provides
    fun connectivityManager(
        application: Application,
    ): ConnectivityManager? = application.getSystemService()

    /**
     * TODO: Get rid of this dependency.
     *
     * The app currently supports a single locale, but otherwise there would be problems with
     * configuration changes. Injecting the Activity context would solve this but it could lead
     * to memory leaks since this is injected in ViewModels which could outlive the given [context].
     */
    @Provides
    fun resourceProvider(
        @ApplicationContext context: Context,
    ): ResourceProvider = AppResourceProvider(context)
}
