package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.UrlParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun json(): Json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    @Provides
    fun baseOkHttpClient(): OkHttpClient {
        // These are the server preferred Ciphers + all the ones included in COMPATIBLE_TLS
        val cipherSuites: List<CipherSuite> = listOf(
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        ) + ConnectionSpec.COMPATIBLE_TLS.cipherSuites.orEmpty()

        val connectionSpecs = buildList {
            val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .cipherSuites(*cipherSuites.toTypedArray())
                .build()

            add(spec)

            if (BuildConfig.DEBUG) {
                add(ConnectionSpec.CLEARTEXT)
            }
        }

        return OkHttpClient.Builder()
            .connectionSpecs(connectionSpecs)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
            .build()
    }

    @Provides
    @UrlParser
    fun urlParserOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }
        .build()

    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .apply { level = HttpLoggingInterceptor.Level.BODY }
}
