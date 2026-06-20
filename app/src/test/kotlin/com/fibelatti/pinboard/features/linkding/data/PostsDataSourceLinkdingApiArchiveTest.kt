package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_HASH
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_VALID
import com.fibelatti.pinboard.MockDataProvider.createBookmarkLocal
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PostsDataSourceLinkdingApiArchiveTest {

    private val mockApi = mockk<LinkdingApi>()
    private val mockDao = mockk<BookmarksDao>(relaxUnitFun = true)
    private val mockRemoteMapper = mockk<BookmarkRemoteMapper>()
    private val mockLocalMapper = mockk<BookmarkLocalMapper>()
    private val mockDateFormatter = mockk<DateFormatter>(relaxed = true)
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns true
    }

    private val dataSource = PostsDataSourceLinkdingApi(
        linkdingApi = mockApi,
        linkdingDao = mockDao,
        bookmarkRemoteMapper = mockRemoteMapper,
        bookmarkLocalMapper = mockLocalMapper,
        dateFormatter = mockDateFormatter,
        connectivityInfoProvider = mockConnectivityInfoProvider,
    )

    private val expectedPost = mockk<Post>()

    @Test
    fun `GIVEN connected WHEN archive is called THEN remote endpoint is called AND row is saved as archived`() =
        runTest {
            // GIVEN
            val post = createPost(pendingSync = null)
            val existing = createBookmarkLocal(isArchived = false, pendingSync = null)
            coEvery { mockDao.getBookmark(id = SAMPLE_HASH, url = SAMPLE_URL_VALID) } returns existing
            coEvery { mockApi.archiveBookmark(id = SAMPLE_HASH) } returns true
            every { mockLocalMapper.map(any()) } returns expectedPost

            // WHEN
            val result = dataSource.archive(post)

            // THEN
            coVerify { mockApi.archiveBookmark(id = SAMPLE_HASH) }
            coVerify {
                mockDao.saveBookmarks(
                    withArg { saved ->
                        assertThat(saved.single())
                            .isEqualTo(existing.copy(isArchived = true, pendingSync = null))
                    },
                )
            }
            assertThat(result.getOrNull()).isEqualTo(expectedPost)
        }

    @Test
    fun `GIVEN not connected WHEN archive is called THEN remote endpoint is NOT called AND row is marked pending`() =
        runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false
            val post = createPost(pendingSync = null)
            val existing = createBookmarkLocal(isArchived = false, pendingSync = null)
            coEvery { mockDao.getBookmark(id = SAMPLE_HASH, url = SAMPLE_URL_VALID) } returns existing
            every { mockLocalMapper.map(any()) } returns expectedPost

            // WHEN
            dataSource.archive(post)

            // THEN
            coVerify(exactly = 0) { mockApi.archiveBookmark(id = any()) }
            coVerify {
                mockDao.saveBookmarks(
                    withArg { saved ->
                        assertThat(saved.single())
                            .isEqualTo(existing.copy(isArchived = true, pendingSync = PendingSyncDto.ARCHIVE))
                    },
                )
            }
        }

    @Test
    fun `GIVEN post pending ADD WHEN archive is called THEN the ADD marker is preserved`() = runTest {
        // GIVEN
        val post = createPost(pendingSync = PendingSync.ADD)
        val existing = createBookmarkLocal(isArchived = false, pendingSync = PendingSyncDto.ADD)
        coEvery { mockDao.getBookmark(id = SAMPLE_HASH, url = SAMPLE_URL_VALID) } returns existing
        every { mockLocalMapper.map(any()) } returns expectedPost

        // WHEN
        dataSource.archive(post)

        // THEN
        coVerify(exactly = 0) { mockApi.archiveBookmark(id = any()) }
        coVerify {
            mockDao.saveBookmarks(
                withArg { saved ->
                    assertThat(saved.single())
                        .isEqualTo(existing.copy(isArchived = true, pendingSync = PendingSyncDto.ADD))
                },
            )
        }
    }

    @Test
    fun `GIVEN connected WHEN unarchive is called THEN remote endpoint is called AND row is saved as not archived`() =
        runTest {
            // GIVEN
            val post = createPost(pendingSync = null)
            val existing = createBookmarkLocal(isArchived = true, pendingSync = null)
            coEvery { mockDao.getBookmark(id = SAMPLE_HASH, url = SAMPLE_URL_VALID) } returns existing
            coEvery { mockApi.unarchiveBookmark(id = SAMPLE_HASH) } returns true
            every { mockLocalMapper.map(any()) } returns expectedPost

            // WHEN
            val result = dataSource.unarchive(post)

            // THEN
            coVerify { mockApi.unarchiveBookmark(id = SAMPLE_HASH) }
            coVerify {
                mockDao.saveBookmarks(
                    withArg { saved ->
                        assertThat(saved.single())
                            .isEqualTo(existing.copy(isArchived = false, pendingSync = null))
                    },
                )
            }
            assertThat(result.getOrNull()).isEqualTo(expectedPost)
        }
}
