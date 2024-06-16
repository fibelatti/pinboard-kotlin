package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import androidx.room.Room
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDao
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun appDatabase(
        application: Application,
    ): AppDatabase = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java).build()

    @Provides
    fun postsDao(database: AppDatabase): PostsDao = database.postDao()

    @Provides
    fun bookmarksDao(appDatabase: AppDatabase): BookmarksDao = appDatabase.linkdingBookmarksDao()

    @Provides
    fun savedFiltersDao(database: AppDatabase): SavedFiltersDao = database.savedFiltersDao()
}
