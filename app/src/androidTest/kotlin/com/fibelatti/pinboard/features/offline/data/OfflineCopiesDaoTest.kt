package com.fibelatti.pinboard.features.offline.data

import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.tooling.BaseDbTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineCopiesDaoTest : BaseDbTest() {

    private val dao: OfflineCopiesDao get() = appDatabase.offlineCopiesDao()
    private val postsDao: PostsDao get() = appDatabase.postDao()
    private val bookmarksDao: BookmarksDao get() = appDatabase.linkdingBookmarksDao()

    private val pinboardCopy: OfflineCopyDto = createDto(bookmarkId = "pinboard-1", appMode = AppMode.PINBOARD)
    private val linkdingCopy: OfflineCopyDto = createDto(bookmarkId = "linkding-1", appMode = AppMode.LINKDING)

    @Test
    fun copiesAreScopedToTheirAppMode() = runTest {
        dao.saveOfflineCopy(pinboardCopy)
        dao.saveOfflineCopy(linkdingCopy)

        assertThat(dao.getOfflineCopies(AppMode.PINBOARD.name).first()).containsExactly(pinboardCopy)
        assertThat(dao.getOfflineCopies(AppMode.LINKDING.name).first()).containsExactly(linkdingCopy)
    }

    @Test
    fun theSameBookmarkIdCanBeSavedOncePerAppMode() = runTest {
        // Both backends number their bookmarks independently, so an id collision across modes is
        // expected and must not overwrite the other account's copy.
        val pinboard = createDto(bookmarkId = "1", appMode = AppMode.PINBOARD, title = "Pinboard")
        val linkding = createDto(bookmarkId = "1", appMode = AppMode.LINKDING, title = "Linkding")

        dao.saveOfflineCopy(pinboard)
        dao.saveOfflineCopy(linkding)

        assertThat(dao.getEveryOfflineCopy()).containsExactly(pinboard, linkding)
    }

    @Test
    fun savingTheSameCopyAgainReplacesIt() = runTest {
        dao.saveOfflineCopy(pinboardCopy)
        val refreshed = pinboardCopy.copy(title = "Updated", sizeBytes = 999L, truncated = true)

        dao.saveOfflineCopy(refreshed)

        assertThat(dao.getOfflineCopy(AppMode.PINBOARD.name, "pinboard-1")).isEqualTo(refreshed)
    }

    @Test
    fun copiesAreListedNewestFirst() = runTest {
        val older = createDto(bookmarkId = "older", appMode = AppMode.PINBOARD, dateCreated = "2026-01-01T00:00:00Z")
        val newer = createDto(bookmarkId = "newer", appMode = AppMode.PINBOARD, dateCreated = "2026-06-01T00:00:00Z")

        dao.saveOfflineCopy(older)
        dao.saveOfflineCopy(newer)

        assertThat(dao.getOfflineCopies(AppMode.PINBOARD.name).first())
            .containsExactly(newer, older)
            .inOrder()
    }

    @Test
    fun getOfflineCopyReturnsNullWhenTheIdBelongsToAnotherAppMode() = runTest {
        dao.saveOfflineCopy(pinboardCopy)

        assertThat(dao.getOfflineCopy(AppMode.LINKDING.name, "pinboard-1")).isNull()
    }

    @Test
    fun deleteOnlyRemovesTheMatchingCopy() = runTest {
        dao.saveOfflineCopy(pinboardCopy)
        dao.saveOfflineCopy(linkdingCopy)

        dao.deleteOfflineCopy(AppMode.PINBOARD.name, "pinboard-1")

        assertThat(dao.getEveryOfflineCopy()).containsExactly(linkdingCopy)
    }

    @Test
    fun deleteAllOnlyClearsTheGivenAppMode() = runTest {
        dao.saveOfflineCopy(pinboardCopy)
        dao.saveOfflineCopy(createDto(bookmarkId = "pinboard-2", appMode = AppMode.PINBOARD))
        dao.saveOfflineCopy(linkdingCopy)

        dao.deleteAllOfflineCopies(AppMode.PINBOARD.name)

        assertThat(dao.getEveryOfflineCopy()).containsExactly(linkdingCopy)
    }

    @Test
    fun orphanedPinboardCopiesAreTheOnesWithoutAMatchingPost() = runTest {
        postsDao.savePosts(listOf(createPostDto(hash = "kept")))
        dao.saveOfflineCopy(createDto(bookmarkId = "kept", appMode = AppMode.PINBOARD))
        dao.saveOfflineCopy(createDto(bookmarkId = "orphaned", appMode = AppMode.PINBOARD))

        val result = dao.getOrphanedPinboardCopyIds(AppMode.PINBOARD.name)

        assertThat(result).containsExactly("orphaned")
    }

    @Test
    fun orphanedPinboardCopiesIgnoreOtherAppModes() = runTest {
        postsDao.savePosts(listOf(createPostDto(hash = "kept")))
        dao.saveOfflineCopy(linkdingCopy)

        val result = dao.getOrphanedPinboardCopyIds(AppMode.PINBOARD.name)

        assertThat(result).isEmpty()
    }

    @Test
    fun appReviewModeSharesThePinboardTable() = runTest {
        postsDao.savePosts(listOf(createPostDto(hash = "kept")))
        dao.saveOfflineCopy(createDto(bookmarkId = "kept", appMode = AppMode.NO_API))
        dao.saveOfflineCopy(createDto(bookmarkId = "orphaned", appMode = AppMode.NO_API))

        val result = dao.getOrphanedPinboardCopyIds(AppMode.NO_API.name)

        assertThat(result).containsExactly("orphaned")
    }

    @Test
    fun orphanedLinkdingCopiesAreTheOnesWithoutAMatchingBookmark() = runTest {
        bookmarksDao.saveBookmarks(listOf(createBookmarkLocal(id = "kept")))
        dao.saveOfflineCopy(createDto(bookmarkId = "kept", appMode = AppMode.LINKDING))
        dao.saveOfflineCopy(createDto(bookmarkId = "orphaned", appMode = AppMode.LINKDING))

        val result = dao.getOrphanedLinkdingCopyIds(AppMode.LINKDING.name)

        assertThat(result).containsExactly("orphaned")
    }

    @Test
    fun orphanedLinkdingCopiesIgnoreOtherAppModes() = runTest {
        bookmarksDao.saveBookmarks(listOf(createBookmarkLocal(id = "kept")))
        dao.saveOfflineCopy(pinboardCopy)

        val result = dao.getOrphanedLinkdingCopyIds(AppMode.LINKDING.name)

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllOfflineCopiesIsScopedToTheGivenAppMode() = runTest {
        dao.saveOfflineCopy(pinboardCopy)
        dao.saveOfflineCopy(linkdingCopy)

        assertThat(dao.getAllOfflineCopies(AppMode.PINBOARD.name)).containsExactly(pinboardCopy)
        assertThat(dao.getEveryOfflineCopy()).containsExactly(pinboardCopy, linkdingCopy)
    }

    private fun createDto(
        bookmarkId: String,
        appMode: AppMode,
        title: String = "Title",
        dateCreated: String = "2026-08-02T10:00:00Z",
    ): OfflineCopyDto = OfflineCopyDto(
        bookmarkId = bookmarkId,
        appMode = appMode.name,
        url = "https://example.com/$bookmarkId",
        title = title,
        fileName = "$bookmarkId.html",
        sizeBytes = 1_024L,
        dateCreated = dateCreated,
        truncated = false,
    )

    private fun createBookmarkLocal(id: String): BookmarkLocal = BookmarkLocal(
        id = id,
        url = "https://example.com/$id",
        title = "Title",
        description = "Description",
    )
}
