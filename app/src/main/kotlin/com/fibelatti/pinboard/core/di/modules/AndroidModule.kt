package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.fibelatti.core.android.platform.AppResourceProvider
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.platform.ConnectivityInfoProvider
import com.fibelatti.core.platform.UserAgentProvider
import com.fibelatti.pinboard.core.android.AndroidConnectivityInfoProvider
import com.fibelatti.pinboard.core.android.AndroidUserAgentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {

    @Provides
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager? = context.getSystemService()

    @Provides
    @Singleton
    fun resourceProvider(@ApplicationContext context: Context): ResourceProvider = AppResourceProvider(context)

    @Provides
    @Singleton
    fun userAgentProvider(impl: AndroidUserAgentProvider): UserAgentProvider = impl

    @Provides
    @Singleton
    fun connectivityInfoProvider(impl: AndroidConnectivityInfoProvider): ConnectivityInfoProvider = impl
}
