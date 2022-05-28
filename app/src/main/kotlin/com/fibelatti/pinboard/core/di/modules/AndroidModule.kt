package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.android.ResourceProvider
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
import javax.inject.Singleton

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

    @Provides
    @Singleton
    fun resourceProvider(
        @ApplicationContext context: Context,
    ): ResourceProvider = AppResourceProvider(context)
}
