package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.ApiInterceptor
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.fibelatti.pinboard.features.notes.data.NotesApi
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.tags.data.TagsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object PinboardModule {

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun okHttpClient(
        okHttpClient: OkHttpClient,
        apiInterceptor: ApiInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        // These are the server preferred Ciphers + all the ones included in COMPATIBLE_TLS
        val cipherSuites: List<CipherSuite> = listOf(
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        ) + ConnectionSpec.COMPATIBLE_TLS.cipherSuites.orEmpty()

        val connectionSpecs = listOf(
            ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .cipherSuites(*cipherSuites.toTypedArray())
                .build()
        )

        return okHttpClient.newBuilder()
            .connectionSpecs(connectionSpecs)
            .addInterceptor(apiInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }
            .build()
    }

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun pinboardRetrofit(
        @RestApi(RestApiProvider.PINBOARD) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    fun postsApi(@RestApi(RestApiProvider.PINBOARD) retrofit: Retrofit): PostsApi = retrofit.create()

    @Provides
    fun tagsApi(@RestApi(RestApiProvider.PINBOARD) retrofit: Retrofit): TagsApi = retrofit.create()

    @Provides
    fun notesApi(@RestApi(RestApiProvider.PINBOARD) retrofit: Retrofit): NotesApi = retrofit.create()
}
