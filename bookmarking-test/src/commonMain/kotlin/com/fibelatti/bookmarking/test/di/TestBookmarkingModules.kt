package com.fibelatti.bookmarking.test.di

import androidx.room.Room
import com.fibelatti.bookmarking.core.network.UnauthorizedPluginProvider
import com.fibelatti.bookmarking.core.persistence.UserSharedPreferences
import com.fibelatti.bookmarking.core.persistence.database.AppDatabase
import com.fibelatti.bookmarking.di.libraryModule
import com.fibelatti.bookmarking.di.networkModule
import com.fibelatti.bookmarking.di.platformModule
import com.fibelatti.bookmarking.features.filters.data.SavedFiltersDao
import com.fibelatti.bookmarking.linkding.data.BookmarksDao
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import com.fibelatti.bookmarking.test.LinkdingMockServer
import com.fibelatti.bookmarking.test.PinboardMockServer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

public val testDatabaseModule: Module = module {
    single<AppDatabase> {
        Room.inMemoryDatabaseBuilder(androidApplication(), AppDatabase::class.java).build()
    }
    single<PostsDao> { get<AppDatabase>().postDao() }
    single<BookmarksDao> { get<AppDatabase>().linkdingBookmarksDao() }
    single<SavedFiltersDao> { get<AppDatabase>().savedFiltersDao() }
}

public val testPinboardModule: Module = module {
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

public val testLinkdingModule: Module = module {
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

public fun testBookmarkingModules(): List<Module> = listOf(
    platformModule(),
    testDatabaseModule,
    networkModule,
    testPinboardModule,
    testLinkdingModule,
    libraryModule,
)
