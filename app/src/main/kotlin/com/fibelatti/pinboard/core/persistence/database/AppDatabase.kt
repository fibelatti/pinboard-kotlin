package com.fibelatti.pinboard.core.persistence.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fibelatti.bookmarking.features.filters.data.SavedFilterDto
import com.fibelatti.bookmarking.features.filters.data.SavedFiltersDao
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.bookmarking.linkding.data.BookmarkLocal
import com.fibelatti.bookmarking.linkding.data.BookmarkLocalFts
import com.fibelatti.bookmarking.linkding.data.BookmarksDao
import com.fibelatti.bookmarking.pinboard.data.PostDto
import com.fibelatti.bookmarking.pinboard.data.PostDtoFts
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import org.koin.core.annotation.Factory

const val DATABASE_NAME = "com.fibelatti.pinboard.db"
const val DATABASE_VERSION_1 = 1 // Release 1.0.0
const val DATABASE_VERSION_2 = 2 // Release 1.7.0
const val DATABASE_VERSION_3 = 3 // Release 1.16.4
const val DATABASE_VERSION_4 = 4 // Release 1.18.0
const val DATABASE_VERSION_5 = 5 // Release 2.2.0
const val DATABASE_VERSION_6 = 6 // Release 2.x (Linkding support)

@Database(
    entities = [
        PostDto::class, PostDtoFts::class,
        BookmarkLocal::class, BookmarkLocalFts::class,
        SavedFilterDto::class,
    ],
    version = DATABASE_VERSION_6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = DATABASE_VERSION_3, to = DATABASE_VERSION_4),
        AutoMigration(from = DATABASE_VERSION_4, to = DATABASE_VERSION_5),
        AutoMigration(from = DATABASE_VERSION_5, to = DATABASE_VERSION_6),
    ],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostsDao
    abstract fun linkdingBookmarksDao(): BookmarksDao
    abstract fun savedFiltersDao(): SavedFiltersDao
}

@Factory
class DatabaseResetCallback(
    private val userRepository: UserRepository,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        userRepository.lastUpdate = ""
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        userRepository.lastUpdate = ""
    }
}
