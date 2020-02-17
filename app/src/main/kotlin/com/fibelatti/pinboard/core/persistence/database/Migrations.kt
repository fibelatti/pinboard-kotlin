package com.fibelatti.pinboard.core.persistence.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun getAllMigrations(): Array<Migration> = arrayOf(
    MIGRATION_1_2
)

val MIGRATION_1_2 = object : Migration(DATABASE_VERSION_1, DATABASE_VERSION_2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop version 1 table
        database.execSQL("DROP TABLE IF EXISTS `PostsFts`")

        // Create version 2 table
        database.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `PostsFts` USING FTS4(
            `href` TEXT NOT NULL, `description` TEXT NOT NULL, 
            `extended` TEXT NOT NULL, `tags` TEXT NOT NULL, 
            tokenize=unicode61 `tokenchars=._-=#@&`, content=`Posts`)
        """.trimIndent())

        // Populate new table
        database.execSQL("INSERT INTO PostsFts(PostsFts) VALUES ('rebuild')")
    }
}
