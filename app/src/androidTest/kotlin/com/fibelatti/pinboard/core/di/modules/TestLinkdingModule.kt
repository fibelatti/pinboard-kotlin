package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.LinkdingMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.LinkdingSSLSocketFactory
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
import javax.inject.Singleton
import okhttp3.ConnectionSpec

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LinkdingModule::class],
)
object TestLinkdingModule {

    @Provides
    @Singleton
    @RestApi(RestApiProvider.LINKDING)
    fun linkdingHttpClient(
        httpClientBuilder: HttpClientBuilder,
        userRepository: UserRepository,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
        linkdingSSLSocketFactory: LinkdingSSLSocketFactory,
    ): HttpClient = httpClientBuilder.build(
        extraHttpClientConfig = {
            install(unauthorizedPluginProvider.plugin)

            expectSuccess = true

            val mockServerUrl = LinkdingMockServer.instance.url("/")
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
        },
        extraOkHttpClientConfig = {
            connectionSpecs(
                listOf(
                    ConnectionSpec.COMPATIBLE_TLS,
                    // Linkding instances can be self-hosted and use insecure connections
                    ConnectionSpec.CLEARTEXT,
                ),
            )

            // Always registering the factory to avoid having to recreate objects if the alias
            // changes. It uses the default behavior when there's no alias set by the user.
            sslSocketFactory(
                sslSocketFactory = linkdingSSLSocketFactory,
                trustManager = linkdingSSLSocketFactory.trustManager,
            )
        },
    )
}
