package com.fibelatti.pinboard

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.fibelatti.pinboard.core.persistence.database.AppDatabase
import org.junit.After
import org.junit.Before

abstract class BaseDbTest {

    protected lateinit var appDatabase: AppDatabase

    @Before
    fun initDb() {
        appDatabase = Room
            .inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                AppDatabase::class.java
            )
            .build()
    }

    @After
    fun closeDb() {
        appDatabase.close()
    }
}
