package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagsDataSourcePinboardApiTest {

    private val mockApi = mockk<TagsApi>()
    private val mockPostsDao = mockk<PostsDao> {
        coEvery { getAllPostTags() } returns emptyList()
    }
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns true
    }

    private val dataSource = TagsDataSourcePinboardApi(
        tagsApi = mockApi,
        postsDao = mockPostsDao,
        connectivityInfoProvider = mockConnectivityInfoProvider,
    )

    @Nested
    inner class GetAllTagsTests {

        @Test
        fun `GIVEN getTags returns an error WHEN getAllTags is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getTags() } throws Exception()

            // WHEN
            val result = dataSource.getAllTags().toList()

            // THEN
            assertThat(result).hasSize(2)
            assertThat(result[0].getOrNull()).isEmpty()
            assertThat(result[1].getOrNull()).isEmpty()
        }

        @Test
        fun `GIVEN getTags returns an empty map WHEN getAllTags is called THEN Success is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getTags() } returns emptyMap()

            // WHEN
            val result = dataSource.getAllTags().toList()

            // THEN
            assertThat(result).hasSize(2)
            assertThat(result[0].getOrNull()).isEmpty()
            assertThat(result[1].getOrNull()).isEmpty()
        }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getTags() } returns mapOf("tag" to 1)

            // WHEN
            val result = dataSource.getAllTags().toList()

            // THEN
            assertThat(result).hasSize(2)
            assertThat(result[0].getOrNull()).isEmpty()
            assertThat(result[1].getOrNull()).isEqualTo(listOf(Tag("tag", 1)))
        }
    }

    @Nested
    inner class GetAllTagsNoConnectionTests {

        @BeforeEach
        fun setup() {
            every { mockConnectivityInfoProvider.isConnected() } returns false
        }

        @Test
        fun `GIVEN getAllPostTags returns an error WHEN getAllTags is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockPostsDao.getAllPostTags() } throws Exception()

            // WHEN
            val result = dataSource.getAllTags().toList()

            // THEN
            assertThat(result).hasSize(1)
            assertThat(result.first().getOrNull()).isEmpty()
        }

        @Test
        fun `GIVEN getAllPostTags returns an empty list WHEN getAllTags is called THEN Success is returned`() =
            runTest {
                // GIVEN
                coEvery { mockPostsDao.getAllPostTags() } returns emptyList()

                // WHEN
                val result = dataSource.getAllTags().toList()

                // THEN
                assertThat(result).hasSize(1)
                assertThat(result.first().getOrNull()).isEmpty()
            }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() = runTest {
            // GIVEN
            coEvery { mockPostsDao.getAllPostTags() } returns listOf(
                "tag1 tag2",
                "tag1",
                "tag2",
                "tag3",
            )

            // WHEN
            val result = dataSource.getAllTags().toList()

            // THEN
            assertThat(result).hasSize(1)
            assertThat(result.first().getOrNull()).isEqualTo(
                listOf(
                    Tag("tag1", 2),
                    Tag("tag2", 2),
                    Tag("tag3", 1),
                ),
            )
        }
    }

    @Nested
    inner class RenameTagTests {

        @Test
        fun `WHEN the result is done THEN the tags are returned`() = runTest {
            // GIVEN
            coEvery { mockApi.renameTag(any(), any()) } returns RenameTagResponseDto(result = "done")
            coEvery { mockApi.getTags() } returns mapOf("new-name" to 1)

            // WHEN
            val result = dataSource.renameTag(oldName = "old-name", newName = "new-name")

            // THEN
            assertThat(result.getOrNull()).isEqualTo(listOf(Tag("new-name", 1)))
        }

        @Test
        fun `WHEN the result is not done THEN an APIException is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.renameTag(any(), any()) } returns RenameTagResponseDto(result = "something-else")

            // WHEN
            val result = dataSource.renameTag(oldName = "old-name", newName = "new-name")

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        }

        @Test
        fun `WHEN the network call fails THEN the error is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.renameTag(any(), any()) } throws Exception()

            // WHEN
            val result = dataSource.renameTag(oldName = "old-name", newName = "new-name")

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }
    }
}
