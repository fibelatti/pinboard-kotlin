package com.fibelatti.pinboard.core.di

import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.room.Room
import com.fibelatti.core.android.extension.getSharedPreferences
import com.fibelatti.core.android.platform.AppResourceProvider
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.core.persistence.database.DATABASE_NAME
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_1
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_2
import com.fibelatti.pinboard.core.persistence.database.DatabaseResetCallback
import com.fibelatti.pinboard.features.ContainerFragment
import com.fibelatti.pinboard.features.appstate.ActionHandler
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDao
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersFragment
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.sync.PendingSyncWorker
import com.fibelatti.pinboard.features.sync.SyncBookmarksWorker
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.dsl.fragmentOf
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module
import java.text.Collator
import java.util.Locale
import java.util.concurrent.TimeUnit

val coreModule = module {
    factory { CoroutineScope(context = Dispatchers.Default + SupervisorJob()) }
    factory { SharingStarted.Eagerly }
}

val databaseModule = module {
    single<AppDatabase> {
        val databaseResetCallback: DatabaseResetCallback = get()

        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(
                dropAllTables = true,
                DATABASE_VERSION_1,
                DATABASE_VERSION_2,
            )
            .addCallback(databaseResetCallback)
            .build()
    }
    single<PostsDao> { get<AppDatabase>().postDao() }
    single<BookmarksDao> { get<AppDatabase>().linkdingBookmarksDao() }
    single<SavedFiltersDao> { get<AppDatabase>().savedFiltersDao() }
}

val networkModule = module {
    factory {
        Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }

    factory(named("base")) {
        HttpClient(OkHttp) {
            engine {
                config {
                    // These are the server preferred Ciphers + all the ones included in COMPATIBLE_TLS
                    val cipherSuites: List<CipherSuite> = listOf(
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                    ) + ConnectionSpec.COMPATIBLE_TLS.cipherSuites.orEmpty()

                    val connectionSpecs = buildList {
                        val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                            .cipherSuites(*cipherSuites.toTypedArray())
                            .build()

                        add(spec)

                        if (BuildConfig.DEBUG) {
                            add(ConnectionSpec.CLEARTEXT)
                        }
                    }

                    connectionSpecs(connectionSpecs)
                    connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))

                    connectTimeout(60, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)

                    followRedirects(true)
                    followSslRedirects(true)
                }
            }

            install(ContentNegotiation) {
                json(get(), contentType = ContentType.Any)
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = Logger.ANDROID
                }
            }
        }
    }
}

val pinboardModule = module {
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

val linkdingModule = module {
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

val androidPlatformModule = module {
    single { Locale.getDefault() }
    single { Collator.getInstance(Locale.US) }
    single<ConnectivityManager?> { androidContext().getSystemService() }
    singleOf(::AppResourceProvider) { bind<ResourceProvider>() }
}

val androidAppModule = module {
    single<SharedPreferences> { androidApplication().getSharedPreferences(name = "user_preferences") }

    factory { getAll<ActionHandler<*>>().toSet() }

    worker {
        PendingSyncWorker(
            context = get(),
            workerParams = get(),
            postsRepository = get(),
        )
    }
    worker {
        SyncBookmarksWorker(
            context = get(),
            workerParams = get(),
            userDataSource = get(),
            postsRepository = get(),
        )
    }

    fragmentOf(::ContainerFragment)
    fragmentOf(::AuthFragment)
    fragmentOf(::PostListFragment)
    fragmentOf(::PostDetailFragment)
    fragmentOf(::EditPostFragment)
    fragmentOf(::PostSearchFragment)
    fragmentOf(::TagsFragment)
    fragmentOf(::SavedFiltersFragment)
    fragmentOf(::NoteListFragment)
    fragmentOf(::NoteDetailsFragment)
    fragmentOf(::PopularPostsFragment)
    fragmentOf(::UserPreferencesFragment)
}

fun allModules() = listOf(
    coreModule,
    databaseModule,
    networkModule,
    pinboardModule,
    linkdingModule,
    androidPlatformModule,
    androidAppModule,
    KoinGeneratedModule().module,
)

@Module
@ComponentScan("com.fibelatti.pinboard")
class KoinGeneratedModule
