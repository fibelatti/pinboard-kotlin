package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.PinboardMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.ApiInterceptor
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.fibelatti.pinboard.features.notes.data.NotesApi
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.tags.data.TagsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PinboardModule::class],
)
object TestPinboardModule {

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun okHttpClient(
        okHttpClient: OkHttpClient,
        apiInterceptor: ApiInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = okHttpClient.newBuilder()
        .addInterceptor(apiInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun retrofit(
        @RestApi(RestApiProvider.PINBOARD) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(PinboardMockServer.instance.url("/"))
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
