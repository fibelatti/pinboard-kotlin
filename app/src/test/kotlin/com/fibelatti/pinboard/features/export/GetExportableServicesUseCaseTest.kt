package com.fibelatti.pinboard.features.export

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetExportableServicesUseCaseTest {

    private val mockPostsDao = mockk<PostsDao>()
    private val mockBookmarksDao = mockk<BookmarksDao>()

    private val useCase = GetExportableServicesUseCase(
        postsDao = mockPostsDao,
        bookmarksDao = mockBookmarksDao,
    )

    @Test
    fun `returns the services with cached bookmarks`() = runTest {
        coEvery { mockPostsDao.getPostCount(query = any()) } returns 1
        coEvery { mockBookmarksDao.getBookmarkCount(query = any()) } returns 0

        assertThat(useCase()).containsExactly(AppMode.PINBOARD)
    }

    @Test
    fun `returns no service when both tables are empty`() = runTest {
        coEvery { mockPostsDao.getPostCount(query = any()) } returns 0
        coEvery { mockBookmarksDao.getBookmarkCount(query = any()) } returns 0

        assertThat(useCase()).isEmpty()
    }
}
