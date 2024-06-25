package com.fibelatti.bookmarking

import androidx.room.Room
import com.fibelatti.bookmarking.core.persistence.database.AppDatabase
import com.fibelatti.bookmarking.features.filters.data.SavedFiltersDao
import com.fibelatti.bookmarking.linkding.data.BookmarksDao
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

public val testDatabaseModule: Module = module {
    single<AppDatabase> {
        Room.inMemoryDatabaseBuilder(androidApplication(), AppDatabase::class.java).build()
    }
    single<PostsDao> { get<AppDatabase>().postDao() }
    single<BookmarksDao> { get<AppDatabase>().linkdingBookmarksDao() }
    single<SavedFiltersDao> { get<AppDatabase>().savedFiltersDao() }
}
