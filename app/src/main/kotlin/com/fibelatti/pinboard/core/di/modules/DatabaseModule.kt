package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import androidx.room.Room
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.core.persistence.database.DATABASE_NAME
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_1
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_2
import com.fibelatti.pinboard.core.persistence.database.DatabaseResetCallback
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDao
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun appDatabase(
        application: Application,
        databaseResetCallback: DatabaseResetCallback,
    ): AppDatabase = Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigrationFrom(dropAllTables = true, DATABASE_VERSION_1, DATABASE_VERSION_2)
        .addCallback(databaseResetCallback)
        .build()

    @Provides
    fun postsDao(database: AppDatabase): PostsDao = database.postDao()

    @Provides
    fun bookmarksDao(appDatabase: AppDatabase): BookmarksDao = appDatabase.linkdingBookmarksDao()

    @Provides
    fun savedFiltersDao(database: AppDatabase): SavedFiltersDao = database.savedFiltersDao()
}
