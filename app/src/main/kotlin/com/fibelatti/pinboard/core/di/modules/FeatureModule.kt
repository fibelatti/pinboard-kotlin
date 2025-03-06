package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.SharedPreferences
import com.fibelatti.core.android.extension.getSharedPreferences
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDataSource
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.notes.data.NotesDataSource
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.posts.data.PostsDataSourceProxy
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.data.TagManagerDataSource
import com.fibelatti.pinboard.features.tags.data.TagsDataSourceProxy
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FeatureModule {

    @Binds
    abstract fun postsRepository(impl: PostsDataSourceProxy): PostsRepository

    @Binds
    abstract fun tagsRepository(impl: TagsDataSourceProxy): TagsRepository

    @Binds
    abstract fun tagManagerRepository(impl: TagManagerDataSource): TagManagerRepository

    @Binds
    abstract fun savedFiltersRepository(impl: SavedFiltersDataSource): SavedFiltersRepository

    @Binds
    abstract fun notesRepository(impl: NotesDataSource): NotesRepository

    @Binds
    abstract fun userRepository(impl: UserDataSource): UserRepository

    companion object {

        @Provides
        fun sharedPreferences(
            application: Application,
        ): SharedPreferences = application.getSharedPreferences("user_preferences")
    }
}
