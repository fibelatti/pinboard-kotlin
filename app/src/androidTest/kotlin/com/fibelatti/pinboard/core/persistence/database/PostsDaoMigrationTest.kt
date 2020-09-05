package com.fibelatti.pinboard.core.persistence.database

import com.fibelatti.pinboard.MockDataProvider.mockHash
import com.fibelatti.pinboard.MockDataProvider.mockShared
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockToRead
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PostsDaoMigrationTest : BaseRoomMigrationTest() {

    companion object {

        private const val mockTagWithHyphen = "android-studio"
        private const val mockAnotherTagWithHyphen = "another-tag"
    }

    private val insertQuery = """
        INSERT INTO $POST_TABLE_NAME VALUES (
            '$mockUrlValid',
            '$mockUrlTitle',
            '$mockUrlDescription',
            '$mockHash',
            '$mockTime',
            '$mockShared',
            '$mockToRead',
            '$mockTagWithHyphen',
            null
        )
    """.trimMargin().trimIndent()

    private val postFromQuery = PostDto(
        href = mockUrlValid,
        description = mockUrlTitle,
        extended = mockUrlDescription,
        hash = mockHash,
        time = mockTime,
        shared = mockShared,
        toread = mockToRead,
        tags = mockTagWithHyphen,
        imageUrl = null
    )

    @Test
    fun postsDaoDataShouldBeKeptAfterMigrations() {
        // GIVEN
        migrationTestHelper.createDatabase(TEST_DATABASE_NAME, DATABASE_VERSION_1).run {
            execSQL(insertQuery)
            close()
        }

        // WHEN
        val result = getMigratedRoomDatabase().postDao().getAllPosts()

        // THEN
        assertThat(result).isEqualTo(listOf(postFromQuery))
    }

    @Test
    fun queryWithHyphenShouldWorkAfterVersion2() {
        // GIVEN
        migrationTestHelper.createDatabase(TEST_DATABASE_NAME, DATABASE_VERSION_1).run {
            execSQL(insertQuery)
            close()
        }

        // WHEN
        val result = getMigratedRoomDatabase().postDao()
            .getAllPosts(tag1 = PostsDao.preFormatTag(mockTagWithHyphen))
            .firstOrNull()

        // THEN
        assertThat(result).isEqualTo(postFromQuery)
    }

    @Test
    fun ftsTableTriggersShouldContinueToWorkAfterMigrations() {
        // GIVEN
        migrationTestHelper.createDatabase(TEST_DATABASE_NAME, DATABASE_VERSION_1).run {
            execSQL(insertQuery)
            close()
        }

        val updatedPost = postFromQuery.copy(tags = mockAnotherTagWithHyphen)
        val postsDao = getMigratedRoomDatabase().postDao()

        postsDao.savePosts(listOf(updatedPost))

        // WHEN
        val result = postsDao.getAllPosts(tag1 = PostsDao.preFormatTag(mockAnotherTagWithHyphen))
            .firstOrNull()

        // THEN
        assertThat(result).isEqualTo(updatedPost)
    }
}
