package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import android.net.TrafficStats
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.memoryCacheMaxSizePercentWhileInBackground
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import coil3.request.crossfade
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.PinboardResponseFixerPlugin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient

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
    fun threadStatsTagInterceptor(): Interceptor = Interceptor { chain ->
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
        chain.proceed(chain.request())
    }

    @Provides
    @RestApi(RestApiProvider.BASE)
    fun baseHttpClient(
        json: Json,
        threadStatsTagInterceptor: Interceptor,
        @ApplicationContext context: Context,
    ): HttpClient = HttpClient(OkHttp) {
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

                addInterceptor(threadStatsTagInterceptor)
            }
        }

        install(PinboardResponseFixerPlugin)
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }

        install(HttpCache) {
            publicStorage(FileStorage(File("${context.cacheDir}/http-cache")))
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                level = LogLevel.INFO
                logger = Logger.ANDROID
            }
        }
    }

    @Provides
    @Singleton
    @OptIn(ExperimentalCoilApi::class)
    fun imageLoader(
        @ApplicationContext context: Context,
        threadStatsTagInterceptor: Interceptor,
    ): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient.Builder()
                            .addInterceptor(threadStatsTagInterceptor)
                            .build()
                    },
                ),
            )
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context = context, percent = 0.25)
                .build()
        }
        .memoryCacheMaxSizePercentWhileInBackground(percent = 0.25)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(relative = "image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .interceptorCoroutineContext(Dispatchers.IO)
        .allowHardware(enable = false)
        .crossfade(enable = true)
        .build()
}
