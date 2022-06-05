package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.pinboard.core.di.IoScope
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.features.appstate.AppStateDataSource
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.notes.data.NotesApi
import com.fibelatti.pinboard.features.notes.data.NotesDataSource
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.PostsDataSource
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.data.TagsApi
import com.fibelatti.pinboard.features.tags.data.TagsDataSource
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    companion object {

        @Provides
        @IoScope
        fun ioScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        @Provides
        fun singleRunner(): SingleRunner = SingleRunner()

        @Provides
        fun Retrofit.postsApi(): PostsApi = create()

        @Provides
        fun AppDatabase.postsDao(): PostsDao = postDao()

        @Provides
        fun Retrofit.tagsApi(): TagsApi = create()

        @Provides
        fun Retrofit.notesApi(): NotesApi = create()
    }

    @Binds
    abstract fun AppStateDataSource.appStateRepository(): AppStateRepository

    @Binds
    abstract fun UserDataSource.userRepository(): UserRepository

    @Binds
    abstract fun PostsDataSource.postsRepository(): PostsRepository

    @Binds
    abstract fun TagsDataSource.tagsRepository(): TagsRepository

    @Binds
    abstract fun NotesDataSource.notesRepository(): NotesRepository
}
