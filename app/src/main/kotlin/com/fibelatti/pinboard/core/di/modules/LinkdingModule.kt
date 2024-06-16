package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.LinkdingApiInterceptor
import com.fibelatti.pinboard.core.network.UnauthorizedInterceptor
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.linkding.data.LinkdingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object LinkdingModule {

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
        .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) }
        .build()

    @Provides
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingRetrofit(
        userSharedPreferences: UserSharedPreferences,
        @RestApi(RestApiProvider.LINKDING) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(userSharedPreferences.linkdingInstanceUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    fun linkdingApi(@RestApi(RestApiProvider.LINKDING) retrofit: Retrofit): LinkdingApi = retrofit.create()
}
