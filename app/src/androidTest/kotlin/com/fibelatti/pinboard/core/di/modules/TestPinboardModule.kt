package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.PinboardMockServer
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.isDone
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import javax.inject.Singleton
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PinboardModule::class],
)
object TestPinboardModule {

    @Provides
    @Singleton
    @RestApi(RestApiProvider.PINBOARD)
    fun pinboardHttpClient(
        httpClientBuilder: HttpClientBuilder,
        userRepository: UserRepository,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient = httpClientBuilder.build(
        extraHttpClientConfig = {
            install(unauthorizedPluginProvider.plugin)

            val mockServerUrl = PinboardMockServer.instance.url("/")
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

            HttpResponseValidator {
                validateResponse { response ->
                    // Unfortunately nothing can be done if the server is acting up.
                    if (response.status == HttpStatusCode.InternalServerError) {
                        runCatching {
                            // Although, the action may have succeeded despite the 500 status code.
                            if (response.body<GenericResponseDto>().isDone) {
                                // In that case, no need to abort.
                                return@validateResponse
                            }
                        }

                        // A `ResponseException` is used when handling exceptions to notify users.
                        throw ResponseException(response, "")
                    }
                }
            }
        },
        extraOkHttpClientConfig = {
            // These are the server preferred Ciphers + all the ones included in COMPATIBLE_TLS
            val cipherSuites: List<CipherSuite> = listOf(
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            ) + ConnectionSpec.COMPATIBLE_TLS.cipherSuites.orEmpty()

            connectionSpecs(
                listOf(
                    ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                        .cipherSuites(*cipherSuites.toTypedArray())
                        .build(),
                    ConnectionSpec.CLEARTEXT,
                ),
            )
        },
    )
}
