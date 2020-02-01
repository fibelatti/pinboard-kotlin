package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.network.ApiRateLimitRunner
import com.fibelatti.pinboard.core.network.HeadersInterceptor
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object NetworkModule {

    private const val DEFAULT_NETWORK_TIMEOUT_SECONDS = 60L

    @Provides
    @Singleton
    fun retrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    fun okHttpClient(
        headersInterceptor: HeadersInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(DEFAULT_NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(headersInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }
            .build()

    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .apply { if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun rateLimitRunner(): RateLimitRunner = ApiRateLimitRunner(AppConfig.API_THROTTLE_TIME)
}
