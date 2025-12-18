package com.fibelatti.pinboard.core.persistence.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fibelatti.pinboard.features.filters.data.SavedFilterDto
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDao
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocalFts
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoFts
import com.fibelatti.pinboard.features.user.data.UserDataSource
import javax.inject.Inject

const val DATABASE_NAME = "com.fibelatti.pinboard.db"
const val DATABASE_VERSION_1 = 1 // Release 1.0.0
const val DATABASE_VERSION_2 = 2 // Release 1.7.0
private const val DATABASE_VERSION_3 = 3 // Release 1.16.4
private const val DATABASE_VERSION_4 = 4 // Release 1.18.0
private const val DATABASE_VERSION_5 = 5 // Release 2.2.0
private const val DATABASE_VERSION_6 = 6 // Release 3.0.0 (Linkding support)
private const val DATABASE_VERSION_7 = 7 // Release 3.2.0
private const val DATABASE_VERSION_8 = 8 // Release 3.8.3
private const val DATABASE_VERSION_9 = 9 // Release 3.12.0 (Advanced search)

@Database(
    entities = [
        PostDto::class, PostDtoFts::class,
        BookmarkLocal::class, BookmarkLocalFts::class,
        SavedFilterDto::class,
    ],
    version = DATABASE_VERSION_9,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = DATABASE_VERSION_3, to = DATABASE_VERSION_4),
        AutoMigration(from = DATABASE_VERSION_4, to = DATABASE_VERSION_5),
        AutoMigration(from = DATABASE_VERSION_5, to = DATABASE_VERSION_6),
        AutoMigration(from = DATABASE_VERSION_6, to = DATABASE_VERSION_7),
        AutoMigration(from = DATABASE_VERSION_7, to = DATABASE_VERSION_8),
        AutoMigration(from = DATABASE_VERSION_8, to = DATABASE_VERSION_9),
    ],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostsDao
    abstract fun linkdingBookmarksDao(): BookmarksDao
    abstract fun savedFiltersDao(): SavedFiltersDao
}

class DatabaseResetCallback @Inject constructor(
    private val userDataSource: UserDataSource,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        userDataSource.lastUpdate = ""
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        userDataSource.lastUpdate = ""
    }
}
