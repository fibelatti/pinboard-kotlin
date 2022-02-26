package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.network.ApiInterceptor
import com.fibelatti.pinboard.core.network.ApiRateLimitRunner
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.core.network.SkipBadElementsListAdapter
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Suppress("MagicNumber")
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun retrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    fun moshi(): Moshi = Moshi.Builder().add(SkipBadElementsListAdapter.Factory).build()

    @Provides
    fun okHttpClient(
        apiInterceptor: ApiInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        // These are the server preferred Ciphers + all the ones included in COMPATIBLE_TLS
        val cipherSuites: List<CipherSuite> = listOf(
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
        ) + ConnectionSpec.COMPATIBLE_TLS.cipherSuites.orEmpty()

        val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .cipherSuites(*cipherSuites.toTypedArray())
            .build()

        return OkHttpClient.Builder()
            .connectionSpecs(listOf(spec))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
            .addInterceptor(apiInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }
            .build()
    }

    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun rateLimitRunner(): RateLimitRunner = ApiRateLimitRunner(AppConfig.API_THROTTLE_TIME)
}
