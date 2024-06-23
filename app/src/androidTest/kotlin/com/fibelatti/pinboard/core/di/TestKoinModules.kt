package com.fibelatti.pinboard.core.di

import androidx.room.Room
import com.fibelatti.bookmarking.core.network.UnauthorizedPluginProvider
import com.fibelatti.bookmarking.core.persistence.UserSharedPreferences
import com.fibelatti.bookmarking.features.filters.data.SavedFiltersDao
import com.fibelatti.bookmarking.linkding.data.BookmarksDao
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import com.fibelatti.pinboard.LinkdingMockServer
import com.fibelatti.pinboard.PinboardMockServer
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val testDatabaseModule = module {
    single<AppDatabase> {
        Room.inMemoryDatabaseBuilder(androidApplication(), AppDatabase::class.java).build()
    }
    single<PostsDao> { get<AppDatabase>().postDao() }
    single<BookmarksDao> { get<AppDatabase>().linkdingBookmarksDao() }
    single<SavedFiltersDao> { get<AppDatabase>().savedFiltersDao() }
}

val testPinboardModule = module {
    factory(named("pinboard")) {
        val httpClient: HttpClient = get(named("base"))
        val userSharedPreferences: UserSharedPreferences = get()
        val unauthorizedPluginProvider: UnauthorizedPluginProvider = get()
        val mockServerUrl = PinboardMockServer.instance.url("/")

        httpClient.config {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = mockServerUrl.host
                    port = mockServerUrl.port

                    parameters.append(name = "format", value = "json")
                    parameters.append(name = "auth_token", value = userSharedPreferences.authToken)
                }
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}

val testLinkdingModule = module {
    factory(named("linkding")) {
        val httpClient: HttpClient = get(named("base"))
        val userSharedPreferences: UserSharedPreferences = get()
        val unauthorizedPluginProvider: UnauthorizedPluginProvider = get()
        val mockServerUrl = LinkdingMockServer.instance.url("/")

        httpClient.config {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = mockServerUrl.host
                    port = mockServerUrl.port
                }
                header("Authorization", "Token ${userSharedPreferences.authToken}")
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}
