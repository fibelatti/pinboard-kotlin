package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.network.ApiRateLimitRunner
import com.fibelatti.pinboard.core.network.HeadersInterceptor
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
abstract class NetworkModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        @Singleton
        fun retrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
            Retrofit.Builder()
                .baseUrl(AppConfig.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        @Provides
        @JvmStatic
        fun okHttpClient(
            okHttpClientBuilder: OkHttpClient.Builder
        ): OkHttpClient = okHttpClientBuilder.build()

        @Provides
        @JvmStatic
        fun okHttpClientBuilder(
            headersInterceptor: HeadersInterceptor,
            unauthorizedInterceptor: UnauthorizedInterceptor,
            loggingInterceptor: HttpLoggingInterceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(headersInterceptor)
                .addInterceptor(unauthorizedInterceptor)
                .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }

        @Provides
        @JvmStatic
        fun httpLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor()
                .apply { if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY }
    }

    @Binds
    abstract fun authInterceptor(headersInterceptor: HeadersInterceptor): Interceptor

    @Binds
    abstract fun rateLimitRunner(apiRateLimitRunner: ApiRateLimitRunner): RateLimitRunner
}
