package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.LinkdingMockServer
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
import io.ktor.client.request.header
import io.ktor.http.ContentType

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
        userSharedPreferences: UserSharedPreferences,
        unauthorizedPluginProvider: UnauthorizedPluginProvider,
    ): HttpClient = httpClient.config {
        defaultRequest {
            url(LinkdingMockServer.instance.url("/").toString())
            header("Authorization", "Token ${userSharedPreferences.authToken}")
            accept(ContentType.Application.Json)
        }

        install(unauthorizedPluginProvider.plugin)
    }
}
