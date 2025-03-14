@file:Suppress("ktlint:standard:max-line-length")

package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_3
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_4
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_5
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_HASH
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_3
import com.fibelatti.pinboard.MockDataProvider.createBookmarkLocal
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.tooling.BaseDbTest
import com.google.common.truth.Truth.assertThat
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BookmarksDaoTest : BaseDbTest() {

    // region Data
    private val mockTerm = "ter" // Intentionally incomplete to test wildcard matching
    private val mockSecondTerm = "oth" // Intentionally incomplete to test wildcard matching

    private val bookmarkWithoutTerm = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "",
        description = "",
    )
    private val bookmarkWithTermInTheUrl = createBookmarkLocal(
        id = randomHash(),
        url = "term with some other stuff",
        title = "",
        description = "",
    )
    private val bookmarkWithTermInTheTitle = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "term-with-hyphen",
        description = "",
    )
    private val bookmarkWithTermInTheDescription = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "",
        description = "term",
    )
    private val bookmarkWithTermInTheNotes = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "",
        description = "",
        notes = "Notes with term and other",
    )
    private val bookmarkWithTermInTheWebsiteTitle = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "",
        description = "",
        notes = "",
        websiteTitle = "Website title term-with-hyphen",
    )
    private val bookmarkWithTermInTheWebsiteDescription = createBookmarkLocal(
        id = randomHash(),
        url = "",
        title = "",
        description = "",
        notes = "",
        websiteTitle = "",
        websiteDescription = "Website description term",
    )

    private val bookmarkWithoutTags = createBookmarkLocal(id = randomHash(), tagNames = "")
    private val bookmarkWithOneTag = createBookmarkLocal(id = randomHash(), tagNames = SAMPLE_TAG_VALUE_1)
    private val bookmarkWithTwoTags = createBookmarkLocal(
        id = randomHash(),
        tagNames = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " "),
    )
    private val bookmarkWithThreeTags = createBookmarkLocal(
        id = randomHash(),
        tagNames = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2, SAMPLE_TAG_VALUE_3)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " "),
    )

    private val bookmarkPublic = createBookmarkLocal(
        id = randomHash(),
        shared = true,
    )
    private val bookmarkPrivate = createBookmarkLocal(
        id = randomHash(),
        shared = false,
    )

    private val bookmarkReadLater = createBookmarkLocal(
        id = randomHash(),
        unread = true,
    )
    private val bookmarkNotReadLater = createBookmarkLocal(
        id = randomHash(),
        unread = false,
    )

    private val bookmarkFirst =
        createBookmarkLocal(id = randomHash(), title = "A title", dateAdded = SAMPLE_DATE_TIME_1)
    private val bookmarkSecond =
        createBookmarkLocal(id = randomHash(), title = "B title", dateAdded = SAMPLE_DATE_TIME_2)
    private val bookmarkThird =
        createBookmarkLocal(id = randomHash(), title = "C title", dateAdded = SAMPLE_DATE_TIME_3)
    private val bookmarkFourth =
        createBookmarkLocal(id = randomHash(), title = "D title", dateAdded = SAMPLE_DATE_TIME_4)
    private val bookmarkFifth =
        createBookmarkLocal(id = randomHash(), title = "E title", dateAdded = SAMPLE_DATE_TIME_5)
    // endregion

    private val bookmarksDao get() = appDatabase.linkdingBookmarksDao()

    private fun randomHash(): String = UUID.randomUUID().toString()

    @Test
    fun whenDeleteIsCalledThenAllDataIsDeleted() = runTest {
        // GIVEN
        val list = listOf(createBookmarkLocal(), createBookmarkLocal(id = "other-$SAMPLE_HASH"))
        bookmarksDao.saveBookmarks(list)

        // WHEN
        bookmarksDao.deleteAllBookmarks()

        // THEN
        val result = bookmarksDao.getAllBookmarks()
        assertThat(result).isEmpty()
    }

    @Test
    fun whenDeleteAllSyncedBookmarksIsCalledThenOnlySyncedBookmarksAreDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createBookmarkLocal(),
            createBookmarkLocal(id = "other-$SAMPLE_HASH"),
            createBookmarkLocal(id = "not-synced-add", pendingSync = PendingSyncDto.ADD),
            createBookmarkLocal(id = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
            createBookmarkLocal(id = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        bookmarksDao.deleteAllSyncedBookmarks()

        // THEN
        val result = bookmarksDao.getAllBookmarks()
        assertThat(result).isEqualTo(
            listOf(
                createBookmarkLocal(id = "not-synced-add", pendingSync = PendingSyncDto.ADD),
                createBookmarkLocal(id = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
                createBookmarkLocal(id = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }

    @Test
    fun givenEntryAlreadyExistsInDbWhenSaveBookmarksIsCalledThenReplaceIsUsed() = runTest {
        // GIVEN
        val original = createBookmarkLocal(unread = true)
        val modified = createBookmarkLocal(unread = false)
        val other = createBookmarkLocal(id = "other-$SAMPLE_HASH")
        val another = createBookmarkLocal(id = "another-$SAMPLE_HASH")

        val list = listOf(original, other)
        bookmarksDao.saveBookmarks(list)

        // WHEN
        bookmarksDao.saveBookmarks(listOf(modified, another))

        // THEN
        val result = bookmarksDao.getAllBookmarks()
        assertThat(result).containsExactly(modified, other, another)
    }

    // region getBookmarkCount
    @Test
    fun givenDbHasNoDataWhenGetBookmarkCountIsCalledThenZeroIsReturned() = runTest {
        // WHEN
        val result = bookmarksDao.getBookmarkCount()

        // THEN
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun givenDbHasDataWhenGetBookmarkCountIsCalledThenCountIsReturned() = runTest {
        // GIVEN
        val list = listOf(createBookmarkLocal())
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getBookmarkCount()

        // THEN
        assertThat(result).isEqualTo(list.size)
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(term = BookmarksDao.preFormatTerm(mockTerm))

            // THEN
            assertThat(result).isEqualTo(6)
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                term = BookmarksDao.preFormatTerm("$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(term = BookmarksDao.preFormatTerm("term-with"))

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1),
                tag2 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_2),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1),
                tag2 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_2),
                tag3 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_3),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(untaggedOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndIgnoreVisibilityFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(ignoreVisibility = true)

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndPublicBookmarksOnlyFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(publicBookmarksOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndPrivateBookmarksOnlyFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(privateBookmarksOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetBookmarkCountIsCalledThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkReadLater,
                bookmarkNotReadLater,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(readLaterOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetBookmarkCountIsCalledThenTheLimitSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTerm,
            bookmarkWithTermInTheUrl,
            bookmarkWithTermInTheTitle,
            bookmarkWithTermInTheDescription,
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
            bookmarkPublic,
            bookmarkPrivate,
            bookmarkReadLater,
            bookmarkNotReadLater,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getBookmarkCount(limit = list.size - 1)

        // THEN
        assertThat(result).isEqualTo(list.size - 1)
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetBookmarkCountIsCalledThenTheDataSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTerm,
            bookmarkWithTermInTheUrl,
            bookmarkWithTermInTheTitle,
            bookmarkWithTermInTheDescription,
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
            bookmarkPublic,
            bookmarkPrivate,
            bookmarkReadLater,
            bookmarkNotReadLater,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getBookmarkCount(limit = list.size + 1)

        // THEN
        assertThat(result).isEqualTo(list.size)
    }
    // endregion

    // region getAllBookmarks
    @Test
    fun givenDbHasNoDataWhenGetAllBookmarksIsCalledThenEmptyListIsReturned() = runTest {
        // WHEN
        val result = bookmarksDao.getAllBookmarks()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasDataWhenGetAllBookmarksIsCalledThenListIsReturned() = runTest {
        // GIVEN
        val list = listOf(createBookmarkLocal())
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks()

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndSortTypeIs0WhenGetAllBookmarksIsCalledThenBookmarksAreReturnedOrderByTimeDesc() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkFirst,
            bookmarkSecond,
            bookmarkThird,
            bookmarkFourth,
            bookmarkFifth,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(sortType = 0)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkFifth,
                bookmarkFourth,
                bookmarkThird,
                bookmarkSecond,
                bookmarkFirst,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs1WhenGetAllBookmarksIsCalledThenBookmarksAreReturnedOrderByTimeAsc() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkFirst,
            bookmarkSecond,
            bookmarkThird,
            bookmarkFourth,
            bookmarkFifth,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(sortType = 1)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkFirst,
                bookmarkSecond,
                bookmarkThird,
                bookmarkFourth,
                bookmarkFifth,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs2WhenGetAllBookmarksIsCalledThenBookmarksAreReturnedOrderByTitleAsc() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkFirst,
            bookmarkSecond,
            bookmarkThird,
            bookmarkFourth,
            bookmarkFifth,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(sortType = 4)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkFirst,
                bookmarkSecond,
                bookmarkThird,
                bookmarkFourth,
                bookmarkFifth,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs3WhenGetAllBookmarksIsCalledThenBookmarksAreReturnedOrderByTitleDesc() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkFirst,
            bookmarkSecond,
            bookmarkThird,
            bookmarkFourth,
            bookmarkFifth,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(sortType = 5)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkFifth,
                bookmarkFourth,
                bookmarkThird,
                bookmarkSecond,
                bookmarkFirst,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                term = BookmarksDao.preFormatTerm("$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTermInTheUrl, bookmarkWithTermInTheNotes))
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(term = BookmarksDao.preFormatTerm(mockTerm))

            // THEN
            assertThat(result).isEqualTo(
                listOf(
                    bookmarkWithTermInTheUrl,
                    bookmarkWithTermInTheTitle,
                    bookmarkWithTermInTheDescription,
                    bookmarkWithTermInTheNotes,
                    bookmarkWithTermInTheWebsiteTitle,
                    bookmarkWithTermInTheWebsiteDescription,
                ),
            )
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithTermInTheNotes,
                bookmarkWithTermInTheWebsiteTitle,
                bookmarkWithTermInTheWebsiteDescription,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(term = BookmarksDao.preFormatTerm("term-with"))

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTermInTheTitle, bookmarkWithTermInTheWebsiteTitle))
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithOneTag, bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithOneTag, bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1),
                tag2 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_2),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                tag1 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1),
                tag2 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_2),
                tag3 = BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_3),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(untaggedOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithoutTags))
        }

    @Test
    fun givenDbHasDataAndIgnoreVisibilityFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(ignoreVisibility = true)

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPublic, bookmarkPrivate))
        }

    @Test
    fun givenDbHasDataAndPublicBookmarksOnlyFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(publicBookmarksOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPublic))
        }

    @Test
    fun givenDbHasDataAndPrivateBookmarksOnlyFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(privateBookmarksOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPrivate))
        }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetAllBookmarksIsCalledThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkReadLater,
                bookmarkNotReadLater,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(readLaterOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkReadLater))
        }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetAllBookmarksIsCalledThenTheLimitSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTerm,
            bookmarkWithTermInTheUrl,
            bookmarkWithTermInTheTitle,
            bookmarkWithTermInTheDescription,
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
            bookmarkPublic,
            bookmarkPrivate,
            bookmarkReadLater,
            bookmarkNotReadLater,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(limit = list.size - 1)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
                bookmarkPublic,
                bookmarkPrivate,
                bookmarkReadLater,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetAllBookmarksIsCalledThenTheDataSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTerm,
            bookmarkWithTermInTheUrl,
            bookmarkWithTermInTheTitle,
            bookmarkWithTermInTheDescription,
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
            bookmarkPublic,
            bookmarkPrivate,
            bookmarkReadLater,
            bookmarkNotReadLater,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(limit = list.size + 1)

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndAndOffsetIsLowerThenDataSizeWhenGetAllBookmarksIsCalledThenTheRemainingDataIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkWithTermInTheUrl,
                bookmarkWithTermInTheTitle,
                bookmarkWithTermInTheDescription,
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
                bookmarkPublic,
                bookmarkPrivate,
                bookmarkReadLater,
                bookmarkNotReadLater,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(offset = list.size - 1)

            // THEN
            assertThat(result).hasSize(1)
        }

    @Test
    fun givenDbHasDataAndAndOffsetIsHigherThenDataSizeWhenGetAllBookmarksIsCalledThenNoDataIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTerm,
            bookmarkWithTermInTheUrl,
            bookmarkWithTermInTheTitle,
            bookmarkWithTermInTheDescription,
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
            bookmarkPublic,
            bookmarkPrivate,
            bookmarkReadLater,
            bookmarkNotReadLater,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks(offset = list.size)

        // THEN
        assertThat(result).isEmpty()
    }
    // endregion

    @Test
    fun whenSearchExistingBookmarkTagIsCalledThenAListOfStringsContainingThatTagIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.searchExistingBookmarkTags(BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                bookmarkWithOneTag.tagNames,
                bookmarkWithTwoTags.tagNames,
                bookmarkWithThreeTags.tagNames,
            ),
        )
    }

    @Test
    fun whenSearchExistingBookmarkTagIsCalledWithAQueryThatContainsAHyphenThenAListOfStringsContainingThatTagIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTags,
                bookmarkWithOneTag,
                bookmarkWithTwoTags,
                bookmarkWithThreeTags,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.searchExistingBookmarkTags(BookmarksDao.preFormatTag(SAMPLE_TAG_VALUE_1))

            // THEN
            assertThat(result).isEqualTo(
                listOf(
                    bookmarkWithOneTag.tagNames,
                    bookmarkWithTwoTags.tagNames,
                    bookmarkWithThreeTags.tagNames,
                ),
            )
        }

    @Test
    fun whenGetPendingSyncedBookmarksIsCalledThenOnlyPendingSyncBookmarksAreReturned() = runTest {
        // GIVEN
        val list = listOf(
            createBookmarkLocal(),
            createBookmarkLocal(id = "other-$SAMPLE_HASH"),
            createBookmarkLocal(id = "not-synced-add", pendingSync = PendingSyncDto.ADD),
            createBookmarkLocal(id = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
            createBookmarkLocal(id = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getPendingSyncBookmarks()

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                createBookmarkLocal(id = "not-synced-add", pendingSync = PendingSyncDto.ADD),
                createBookmarkLocal(id = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
                createBookmarkLocal(id = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }

    @Test
    fun whenDeletePendingSyncedBookmarkIsCalledThenThatBookmarkIsDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createBookmarkLocal(),
            createBookmarkLocal(id = "other-$SAMPLE_HASH"),
            createBookmarkLocal(id = "not-synced-add", url = "href-add", pendingSync = PendingSyncDto.ADD),
            createBookmarkLocal(id = "not-synced-update", url = "href-update", pendingSync = PendingSyncDto.UPDATE),
            createBookmarkLocal(id = "not-synced-delete", url = "href-delete", pendingSync = PendingSyncDto.DELETE),
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        bookmarksDao.deletePendingSyncBookmark(url = "href-add")

        // THEN
        val result = bookmarksDao.getAllBookmarks()
        assertThat(result).isEqualTo(
            listOf(
                createBookmarkLocal(),
                createBookmarkLocal(id = "other-$SAMPLE_HASH"),
                createBookmarkLocal(id = "not-synced-update", url = "href-update", pendingSync = PendingSyncDto.UPDATE),
                createBookmarkLocal(id = "not-synced-delete", url = "href-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }
}
