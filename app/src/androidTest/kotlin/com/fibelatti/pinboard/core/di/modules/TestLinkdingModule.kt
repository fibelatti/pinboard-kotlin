package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.LinkdingMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.LinkdingApiInterceptor
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.fibelatti.pinboard.features.linkding.data.LinkdingApi
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
    replaces = [LinkdingModule::class],
)
object TestLinkdingModule {

    @Provides
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingOkHttpClient(
        okHttpClient: OkHttpClient,
        linkdingApiInterceptor: LinkdingApiInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = okHttpClient.newBuilder()
        .addInterceptor(linkdingApiInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingRetrofit(
        @RestApi(RestApiProvider.LINKDING) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(LinkdingMockServer.instance.url("/"))
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    fun linkdingApi(@RestApi(RestApiProvider.LINKDING) retrofit: Retrofit): LinkdingApi = retrofit.create()
}
