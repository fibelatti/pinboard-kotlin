package com.fibelatti.pinboard.core.persistence.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule

abstract class BaseRoomMigrationTest {

    companion object {

        const val TEST_DATABASE_NAME = "$DATABASE_NAME.test"
    }

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    protected fun getMigratedRoomDatabase(): AppDatabase = Room.databaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AppDatabase::class.java,
        TEST_DATABASE_NAME
    ).addMigrations(*getAllMigrations()).build().also(migrationTestHelper::closeWhenFinished)
}
