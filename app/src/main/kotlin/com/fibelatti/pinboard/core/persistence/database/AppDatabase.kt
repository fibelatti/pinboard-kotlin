package com.fibelatti.pinboard.core.persistence.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoFts

const val DATABASE_NAME = "com.fibelatti.pinboard.db"

const val DATABASE_VERSION_1 = 1

@Database(
    entities = [
        PostDto::class,
        PostDtoFts::class
    ],
    version = DATABASE_VERSION_1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostsDao
}
