package com.fibelatti.bookmarking.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.fibelatti.bookmarking.core.network.UnauthorizedPluginProvider
import com.fibelatti.bookmarking.core.persistence.UserSharedPreferences
import com.fibelatti.bookmarking.core.persistence.database.AppDatabase
import com.fibelatti.bookmarking.core.persistence.database.DATABASE_VERSION_1
import com.fibelatti.bookmarking.core.persistence.database.DATABASE_VERSION_2
import com.fibelatti.bookmarking.core.persistence.database.DatabaseResetCallback
import com.fibelatti.bookmarking.features.appstate.ActionHandler
import com.fibelatti.bookmarking.features.filters.data.SavedFiltersDao
import com.fibelatti.bookmarking.features.posts.data.PostsDataSourceNoApi
import com.fibelatti.bookmarking.features.tags.data.TagsDataSource
import com.fibelatti.bookmarking.linkding.data.BookmarksDao
import com.fibelatti.bookmarking.linkding.data.PostsDataSourceLinkdingApi
import com.fibelatti.bookmarking.linkding.data.TagsDataSourceLinkdingApi
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import com.fibelatti.bookmarking.pinboard.data.PostsDataSourcePinboardApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val databaseModule: org.koin.core.module.Module = module {
    single<AppDatabase> {
        val databaseBuilder: RoomDatabase.Builder<AppDatabase> = get()
        val databaseResetCallback: DatabaseResetCallback = get()

        databaseBuilder
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .fallbackToDestructiveMigrationFrom(
                dropAllTables = true,
                DATABASE_VERSION_1,
                DATABASE_VERSION_2,
            )
            .addCallback(databaseResetCallback)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<PostsDao> { get<AppDatabase>().postDao() }
    single<BookmarksDao> { get<AppDatabase>().linkdingBookmarksDao() }
    single<SavedFiltersDao> { get<AppDatabase>().savedFiltersDao() }
}

public val networkModule: org.koin.core.module.Module = module {
    factory {
        Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }
}

private val pinboardModule: org.koin.core.module.Module = module {
    factory(named("pinboard")) {
        val httpClient: HttpClient = get(named("base"))
        val userSharedPreferences: UserSharedPreferences = get()
        val unauthorizedPluginProvider: UnauthorizedPluginProvider = get()

        httpClient.config {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.pinboard.in/v1"
                    parameters.append(name = "format", value = "json")
                    parameters.append(name = "auth_token", value = userSharedPreferences.authToken)
                }
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}

private val linkdingModule: org.koin.core.module.Module = module {
    factory(named("linkding")) {
        val httpClient: HttpClient = get(named("base"))
        val userSharedPreferences: UserSharedPreferences = get()
        val unauthorizedPluginProvider: UnauthorizedPluginProvider = get()

        httpClient.config {
            defaultRequest {
                url(userSharedPreferences.linkdingInstanceUrl)
                header("Authorization", "Token ${userSharedPreferences.authToken}")
                accept(ContentType.Application.Json)
            }

            install(unauthorizedPluginProvider.plugin)
        }
    }
}

public val libraryModule: org.koin.core.module.Module = module {
    factory { CoroutineScope(context = Dispatchers.Default + SupervisorJob()) }
    factory { SharingStarted.Eagerly }

    factory { getAll<ActionHandler<*>>().toSet() }

    singleOf(::PostsDataSourceNoApi)
    singleOf(::PostsDataSourcePinboardApi)
    singleOf(::PostsDataSourceLinkdingApi)
    singleOf(::TagsDataSource)
    singleOf(::TagsDataSourceLinkdingApi)
}

public fun bookmarkingModules(): List<org.koin.core.module.Module> = listOf(
    platformModule(),
    databaseModule,
    networkModule,
    pinboardModule,
    linkdingModule,
    libraryModule,
)

@Module
@ComponentScan("com.fibelatti.bookmarking")
public class GeneratedBookmarkingModule
