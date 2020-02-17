package com.fibelatti.pinboard.core.persistence.database

import androidx.room.testing.MigrationTestHelper
import org.junit.Test

class AllRoomMigrationsTest : BaseRoomMigrationTest() {

    /**
     * Ideally for this test one would like to use [MigrationTestHelper.runMigrationsAndValidate]
     * passing the latest version and all migrations but currently Room fails to verify migrations
     * that include the creation of a virtual table, even though the create table statement is copied
     * directly from the generated schema. The migration will be successful, the table will be created
     * but the type and affinity of the fields won't match the schema and the test would fail.
     */
    @Test
    fun createDatabaseAndRunMigrations() {
        // Create earliest version of the database.
        migrationTestHelper.createDatabase(TEST_DATABASE_NAME, DATABASE_VERSION_1).close()

        // Create latest version of the database and attempt to open
        getMigratedRoomDatabase().openHelper.writableDatabase
    }
}
