package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
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
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager? = context.getSystemService()

    @Provides
    @Singleton
    fun resourceProvider(@ApplicationContext context: Context): ResourceProvider = AppResourceProvider(context)
}
