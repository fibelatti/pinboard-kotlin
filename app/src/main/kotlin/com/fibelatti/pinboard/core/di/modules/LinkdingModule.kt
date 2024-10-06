package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import okhttp3.ConnectionSpec

@Module
@InstallIn(SingletonComponent::class)
object LinkdingModule {

    @Provides
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingHttpClient(
        @RestApi(RestApiProvider.BASE) httpClient: HttpClient,
        userSharedPreferences: UserSharedPreferences,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient = httpClient.config {
        engine {
            (this as OkHttpConfig).config {
                connectionSpecs(
                    listOf(
                        ConnectionSpec.COMPATIBLE_TLS,
                        ConnectionSpec.CLEARTEXT,
                    ),
                )
            }
        }

        defaultRequest {
            url(userSharedPreferences.linkdingInstanceUrl)
            header("Authorization", "Token ${userSharedPreferences.authToken}")
            accept(ContentType.Application.Json)
        }

        install(unauthorizedPluginProvider.plugin)
    }
}
