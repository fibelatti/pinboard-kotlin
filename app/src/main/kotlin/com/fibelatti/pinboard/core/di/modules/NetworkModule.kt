package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
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
    @RestApi(RestApiProvider.BASE)
    fun httpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
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

                connectionSpecs(connectionSpecs)
                connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))

                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)

                followRedirects(true)
                followSslRedirects(true)
            }
        }

        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.ANDROID
            }
        }
    }
}
