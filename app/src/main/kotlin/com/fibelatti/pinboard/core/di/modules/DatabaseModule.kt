package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import androidx.room.Room
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import com.fibelatti.pinboard.core.persistence.database.DATABASE_NAME
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_1
import com.fibelatti.pinboard.core.persistence.database.DATABASE_VERSION_2
import com.fibelatti.pinboard.core.persistence.database.DatabaseResetCallback
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
        .fallbackToDestructiveMigrationFrom(DATABASE_VERSION_1, DATABASE_VERSION_2)
        .addCallback(databaseResetCallback)
        .build()
}
