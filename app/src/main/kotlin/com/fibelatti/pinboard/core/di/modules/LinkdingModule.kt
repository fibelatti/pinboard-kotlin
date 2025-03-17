package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
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
        userRepository: UserRepository,
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
            val credentials = userRepository.userCredentials.value

            url(requireNotNull(credentials.linkdingInstanceUrl))
            credentials.linkdingAuthToken?.let { token ->
                header("Authorization", "Token $token")
            }
            accept(ContentType.Application.Json)
        }

        expectSuccess = true

        install(unauthorizedPluginProvider.plugin)
    }
}
