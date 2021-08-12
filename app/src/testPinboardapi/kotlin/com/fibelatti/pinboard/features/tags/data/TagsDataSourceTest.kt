package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagsDataSourceTest {

    private val mockApi = mockk<TagsApi>()
    private val mockPostsDao = mockk<PostsDao>()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns true
    }

    private val dataSource = TagsDataSource(
        mockApi,
        mockPostsDao,
        mockConnectivityInfoProvider,
    )

    @Nested
    inner class GetAllTagsTests {

        @Test
        fun `GIVEN getTags returns an error WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.getTags() } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN getTags returns a map with an invalid value WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.getTags() } returns mapOf("tag" to "a")

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(NumberFormatException::class.java)
        }

        @Test
        fun `GIVEN getTags returns an empty map WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.getTags() } returns emptyMap()

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(emptyList<Tag>())
        }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.getTags() } returns mapOf("tag" to "1")

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            assertThat(result.getOrNull()).isEqualTo(listOf(Tag("tag", 1)))
        }
    }

    @Nested
    inner class GetAllTagsNoConnectionTests {

        @BeforeEach
        fun setup() {
            every { mockConnectivityInfoProvider.isConnected() } returns false
        }

        @Test
        fun `GIVEN getAllPostTags returns an error WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockPostsDao.getAllPostTags() } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN getAllPostTags returns an empty list WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockPostsDao.getAllPostTags() } returns emptyList()

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(emptyList<Tag>())
        }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockPostsDao.getAllPostTags() } returns listOf(
                "tag1 tag2",
                "tag1",
                "tag2",
                "tag3"
            )

            // WHEN
            val result = runBlocking { dataSource.getAllTags() }

            assertThat(result.getOrNull()).isEqualTo(listOf(
                Tag("tag1", 2),
                Tag("tag2", 2),
                Tag("tag3", 1),
            ))
        }
    }
}
