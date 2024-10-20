package com.fibelatti.pinboard.core.di.modules

import android.net.TrafficStats
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
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun json(): Json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    @Provides
    @RestApi(RestApiProvider.BASE)
    fun baseHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectionPool(
                    ConnectionPool(
                        maxIdleConnections = 0,
                        keepAliveDuration = 5,
                        timeUnit = TimeUnit.MINUTES,
                    ),
                )

                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)

                followRedirects(true)
                followSslRedirects(true)

                addInterceptor { chain ->
                    TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
                    chain.proceed(chain.request())
                }
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
