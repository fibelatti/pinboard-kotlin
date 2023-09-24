package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.android.ResourceProvider
import com.fibelatti.core.extension.getSharedPreferences
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.AppReviewMode
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.features.user.domain.UserRepository
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
    @Suppress("KotlinConstantConditions")
    fun mainVariant(): Boolean = BuildConfig.FLAVOR == "pinboardapi"

    @Provides
    @AppReviewMode
    fun appReviewMode(userRepository: UserRepository): Boolean = userRepository.appReviewMode

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
