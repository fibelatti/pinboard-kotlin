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
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec

@Module
@InstallIn(SingletonComponent::class)
object PinboardModule {

    @Provides
    @RestApi(RestApiProvider.PINBOARD)
    fun pinboardHttpClient(
        @RestApi(RestApiProvider.BASE) httpClient: HttpClient,
        userSharedPreferences: UserSharedPreferences,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient = httpClient.config {
        engine {
            (this as OkHttpConfig).config {
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
                    ),
                )
            }
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.pinboard.in"
                parameters.append(name = "format", value = "json")
                parameters.append(name = "auth_token", value = userSharedPreferences.authToken)
            }
            accept(ContentType.Application.Json)
        }

        install(unauthorizedPluginProvider.plugin)
    }
}
