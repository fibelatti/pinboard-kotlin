package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.features.appstate.AppStateDataSource
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDao
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDataSource
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.notes.data.NotesApi
import com.fibelatti.pinboard.features.notes.data.NotesDataSource
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.PostsDataSourceProxy
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
import kotlinx.coroutines.flow.SharingStarted
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    companion object {

        @Provides
        @Scope(AppDispatchers.IO)
        fun ioScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        @Provides
        @Scope(AppDispatchers.DEFAULT)
        fun defaultScope(): CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        @Provides
        fun sharingStarted(): SharingStarted = SharingStarted.Eagerly

        @Provides
        fun postsApi(retrofit: Retrofit): PostsApi = retrofit.create()

        @Provides
        fun postsDao(database: AppDatabase): PostsDao = database.postDao()

        @Provides
        fun tagsApi(retrofit: Retrofit): TagsApi = retrofit.create()

        @Provides
        fun notesApi(retrofit: Retrofit): NotesApi = retrofit.create()

        @Provides
        fun savedFiltersDao(database: AppDatabase): SavedFiltersDao = database.savedFiltersDao()
    }

    @Binds
    abstract fun appStateRepository(impl: AppStateDataSource): AppStateRepository

    @Binds
    abstract fun userRepository(impl: UserDataSource): UserRepository

    @Binds
    abstract fun postsRepository(impl: PostsDataSourceProxy): PostsRepository

    @Binds
    abstract fun tagsRepository(impl: TagsDataSource): TagsRepository

    @Binds
    abstract fun notesRepository(impl: NotesDataSource): NotesRepository

    @Binds
    abstract fun savedFiltersRepository(impl: SavedFiltersDataSource): SavedFiltersRepository
}
