package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.PinboardMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol

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
        userRepository: UserRepository,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient {
        val mockServerUrl = PinboardMockServer.instance.url("/")

        return httpClient.config {
            defaultRequest {
                val credentials = userRepository.userCredentials.value

                url {
                    protocol = URLProtocol.HTTP
                    host = mockServerUrl.host
                    port = mockServerUrl.port

                    parameters.append(name = "format", value = "json")
                    credentials.pinboardAuthToken?.let { token ->
                        parameters.append(name = "auth_token", value = token)
                    }
                }
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}
