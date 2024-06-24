package com.fibelatti.pinboard.core.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.fibelatti.bookmarking.di.GeneratedBookmarkingModule
import com.fibelatti.bookmarking.di.bookmarkingModules
import com.fibelatti.core.android.platform.AppResourceProvider
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.features.ContainerFragment
import com.fibelatti.pinboard.features.appstate.ActionHandler
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
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
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.dsl.fragmentOf
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.module

val coreModule = module {
    factory { CoroutineScope(context = Dispatchers.Default + SupervisorJob()) }
    factory { SharingStarted.Eagerly }
}

val androidAppModule = module {
    single<ConnectivityManager?> { androidContext().getSystemService() }
    singleOf(::AppResourceProvider) { bind<ResourceProvider>() }
    single<Settings> { SharedPreferencesSettings.Factory(androidContext()).create(name = "user_preferences") }

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
            userRepository = get(),
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

fun appModules() = listOf(
    coreModule,
    androidAppModule,
    GeneratedModule().module,
)

fun allModules() = appModules() + bookmarkingModules() + GeneratedBookmarkingModule().module

@Module
@ComponentScan("com.fibelatti.pinboard")
class GeneratedModule
