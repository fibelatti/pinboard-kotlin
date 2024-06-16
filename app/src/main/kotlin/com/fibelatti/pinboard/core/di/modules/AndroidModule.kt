package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.fibelatti.core.android.extension.getSharedPreferences
import com.fibelatti.core.android.platform.AppResourceProvider
import com.fibelatti.core.android.platform.ResourceProvider
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
    fun localeDefault(): Locale = Locale.getDefault()

    @Provides
    fun usCollator(): Collator = Collator.getInstance(Locale.US)

    @Provides
    fun sharedPreferences(
        application: Application,
    ): SharedPreferences = application.getSharedPreferences("user_preferences")

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
