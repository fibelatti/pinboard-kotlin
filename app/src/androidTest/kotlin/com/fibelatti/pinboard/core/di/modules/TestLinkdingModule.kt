package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.LinkdingMockServer
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
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LinkdingModule::class],
)
object TestLinkdingModule {

    @Provides
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingHttpClient(
        @RestApi(RestApiProvider.BASE) httpClient: HttpClient,
        userRepository: UserRepository,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient {
        val mockServerUrl = LinkdingMockServer.instance.url("/")

        return httpClient.config {
            defaultRequest {
                val credentials = userRepository.userCredentials.value

                url {
                    protocol = URLProtocol.HTTP
                    host = mockServerUrl.host
                    port = mockServerUrl.port
                }
                credentials.linkdingAuthToken?.let { token ->
                    header("Authorization", "Token $token")
                }
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}
