package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.PinboardMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PinboardModule::class],
)
object TestPinboardModule {

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun pinboardHttpClient(
        @RestApi(RestApiProvider.BASE) httpClient: HttpClient,
        userSharedPreferences: UserSharedPreferences,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient = httpClient.config {
        defaultRequest {
            url(PinboardMockServer.instance.url("/").toString()) {
                parameters.append(name = "format", value = "json")
                parameters.append(name = "auth_token", value = userSharedPreferences.authToken)
            }
            accept(ContentType.Application.Json)
        }

        install(unauthorizedPluginProvider.plugin)
    }
}
