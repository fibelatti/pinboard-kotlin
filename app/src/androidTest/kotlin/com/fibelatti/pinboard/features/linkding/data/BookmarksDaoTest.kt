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
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
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

    private val bookmarkChineseTitle = createBookmarkLocal(
        id = randomHash(),
        title = "这个Bug还没有解决",
    )
    private val bookmarkChineseDescription = createBookmarkLocal(
        id = randomHash(),
        description = "这个Bug还没有解决",
    )
    private val bookmarkChineseNotes = createBookmarkLocal(
        id = randomHash(),
        notes = "这个Bug还没有解决",
    )
    private val bookmarkChineseWebsiteTitle = createBookmarkLocal(
        id = randomHash(),
        websiteTitle = "这个Bug还没有解决",
    )
    private val bookmarkChineseWebsiteDescription = createBookmarkLocal(
        id = randomHash(),
        websiteDescription = "这个Bug还没有解决",
    )
    private val bookmarkChineseTag = createBookmarkLocal(
        id = randomHash(),
        tagNames = "主题",
    )
    private val bookmarkCyrillicTitle = createBookmarkLocal(
        id = randomHash(),
        title = "Эта ошибка еще не устранена.",
    )
    private val bookmarkCyrillicDescription = createBookmarkLocal(
        id = randomHash(),
        description = "Эта ошибка еще не устранена.",
    )
    private val bookmarkCyrillicNotes = createBookmarkLocal(
        id = randomHash(),
        notes = "Эта ошибка еще не устранена.",
    )
    private val bookmarkCyrillicWebsiteTitle = createBookmarkLocal(
        id = randomHash(),
        websiteTitle = "Эта ошибка еще не устранена.",
    )
    private val bookmarkCyrillicWebsiteDescription = createBookmarkLocal(
        id = randomHash(),
        websiteDescription = "Эта ошибка еще не устранена.",
    )
    private val bookmarkCyrillicTag = createBookmarkLocal(
        id = randomHash(),
        tagNames = "Тема",
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

    private val bookmarkFirst = createBookmarkLocal(
        id = randomHash(),
        title = "A title",
        dateAdded = SAMPLE_DATE_TIME_1,
    )
    private val bookmarkSecond = createBookmarkLocal(
        id = randomHash(),
        title = "B title",
        dateAdded = SAMPLE_DATE_TIME_2,
    )
    private val bookmarkThird = createBookmarkLocal(
        id = randomHash(),
        title = "C title",
        dateAdded = SAMPLE_DATE_TIME_3,
    )
    private val bookmarkFourth = createBookmarkLocal(
        id = randomHash(),
        title = "D title",
        dateAdded = SAMPLE_DATE_TIME_4,
    )
    private val bookmarkFifth = createBookmarkLocal(
        id = randomHash(),
        title = "E title",
        dateAdded = SAMPLE_DATE_TIME_5,
    )
    // endregion

    private val bookmarksDao get() = appDatabase.linkdingBookmarksDao()

    private fun randomHash(): String = UUID.randomUUID().toString()

    @Test
    fun whenDeleteIsCalled_ThenAllDataIsDeleted() = runTest {
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
    fun whenDeleteAllSyncedBookmarksIsCalled_ThenOnlySyncedBookmarksAreDeleted() = runTest {
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
    fun givenEntryAlreadyExistsInDb_WhenSaveBookmarksIsCalled_ThenReplaceIsUsed() = runTest {
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
    fun givenDbHasNoData_WhenGetBookmarkCountIsCalled_ThenZeroIsReturned() = runTest {
        // WHEN
        val result = bookmarksDao.getBookmarkCount()

        // THEN
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun givenDbHasData_WhenGetBookmarkCountIsCalled_ThenCountIsReturned() = runTest {
        // GIVEN
        val list = listOf(createBookmarkLocal())
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getBookmarkCount()

        // THEN
        assertThat(result).isEqualTo(list.size)
    }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(term = mockTerm),
            )

            // THEN
            assertThat(result).isEqualTo(6)
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassedWithMoreThanOneWord_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(term = "$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_AndItContainsAHyphen_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(term = "term-with"),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_AndItContainsAHyphen_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2FilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2_AndTag3FilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                    tag3 = SAMPLE_TAG_VALUE_3,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndUntaggedOnlyFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.bookmarksCountFtsQuery(untaggedOnly = true),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndIgnoreVisibilityFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(postVisibility = PostVisibility.None),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndPublicBookmarksOnlyFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(postVisibility = PostVisibility.Public),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndPrivateBookmarksOnlyFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(postVisibility = PostVisibility.Private),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndReadLaterOnlyFilterIsPassed_WhenGetBookmarkCountIsCalled_ThenCountOfBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkReadLater,
                bookmarkNotReadLater,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(readLaterOnly = true),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndLimitIsLowerThanDataSize_WhenGetBookmarkCountIsCalled_ThenTheLimitSizeIsReturned() =
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
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(
                    limit = list.size - 1,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(list.size - 1)
        }

    @Test
    fun givenDbHasData_AndLimitIsHigherThanDataSize_WhenGetBookmarkCountIsCalled_ThenTheDataSizeIsReturned() =
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
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountFtsQuery(
                    limit = list.size + 1,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(list.size)
        }

    @Test
    fun givenDbHasData_AndTermFilterContainsChineseChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountNoFtsQuery(term = "还没"),
            )

            // THEN
            assertThat(result).isEqualTo(5)
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsChineseChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountNoFtsQuery(tag1 = "主题"),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndTermFilterContainsCyrillicChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountNoFtsQuery(term = "ошибка"),
            )

            // THEN
            assertThat(result).isEqualTo(5)
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsCyrillicChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getBookmarkCount(
                query = BookmarksDao.bookmarksCountNoFtsQuery(tag1 = "Тема"),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }
    // endregion

    // region getAllBookmarks
    @Test
    fun givenDbHasNoData_WhenGetAllBookmarksIsCalled_ThenEmptyListIsReturned() = runTest {
        // WHEN
        val result = bookmarksDao.getAllBookmarks()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasData_WhenGetAllBookmarksIsCalled_ThenListIsReturned() = runTest {
        // GIVEN
        val list = listOf(createBookmarkLocal())
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.getAllBookmarks()

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasData_AndSortTypeIs0_WhenGetAllBookmarksIsCalled_ThenBookmarksAreReturnedOrderByTimeDesc() = runTest {
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
        val result = bookmarksDao.getAllBookmarks(
            query = BookmarksDao.allBookmarksFtsQuery(sortType = 0),
        )

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
    fun givenDbHasData_AndSortTypeIs1_WhenGetAllBookmarksIsCalled_ThenBookmarksAreReturnedOrderByTimeAsc() = runTest {
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
        val result = bookmarksDao.getAllBookmarks(
            query = BookmarksDao.allBookmarksFtsQuery(sortType = 1),
        )

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
    fun givenDbHasData_AndSortTypeIs2_WhenGetAllBookmarksIsCalled_ThenBookmarksAreReturnedOrderByTitleAsc() = runTest {
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
        val result = bookmarksDao.getAllBookmarks(
            query = BookmarksDao.allBookmarksFtsQuery(sortType = 4),
        )

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
    fun givenDbHasData_AndSortTypeIs3_WhenGetAllBookmarksIsCalled_ThenBookmarksAreReturnedOrderByTitleDesc() = runTest {
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
        val result = bookmarksDao.getAllBookmarks(
            query = BookmarksDao.allBookmarksFtsQuery(sortType = 5),
        )

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
    fun givenDbHasData_AndTermFilterIsPassedWithMoreThanOneWord_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(term = "$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTermInTheUrl, bookmarkWithTermInTheNotes))
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(term = mockTerm),
            )

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
    fun givenDbHasData_AndTermFilterIsPassed_AndItContainsAHyphen_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(term = "term-with"),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTermInTheTitle, bookmarkWithTermInTheWebsiteTitle))
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithOneTag, bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_AndItContainsAHyphen_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithOneTag, bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2FilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithTwoTags, bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2_AndTag3FilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                    tag3 = SAMPLE_TAG_VALUE_3,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndUntaggedOnlyFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
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
                query = BookmarksDao.allBookmarksFtsQuery(untaggedOnly = true),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkWithoutTags))
        }

    @Test
    fun givenDbHasData_AndIgnoreVisibilityFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(postVisibility = PostVisibility.None),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPublic, bookmarkPrivate))
        }

    @Test
    fun givenDbHasData_AndPublicBookmarksOnlyFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(postVisibility = PostVisibility.Public),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPublic))
        }

    @Test
    fun givenDbHasData_AndPrivateBookmarksOnlyFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkPublic,
                bookmarkPrivate,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(postVisibility = PostVisibility.Private),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkPrivate))
        }

    @Test
    fun givenDbHasData_AndReadLaterOnlyFilterIsPassed_WhenGetAllBookmarksIsCalled_ThenBookmarksThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkReadLater,
                bookmarkNotReadLater,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(readLaterOnly = true),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(bookmarkReadLater))
        }

    @Test
    fun givenDbHasData_AndLimitIsLowerThanDataSize_WhenGetAllBookmarksIsCalled_ThenTheLimitSizeIsReturned() =
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
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(limit = list.size - 1),
            )

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
    fun givenDbHasData_AndLimitIsHigherThanDataSize_WhenGetAllBookmarksIsCalled_ThenTheDataSizeIsReturned() =
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
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(limit = list.size + 1),
            )

            // THEN
            assertThat(result).isEqualTo(list)
        }

    @Test
    fun givenDbHasData_AndOffsetIsLowerThanDataSize_WhenGetAllBookmarksIsCalled_ThenTheRemainingDataIsReturned() =
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
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksFtsQuery(offset = list.size - 1),
            )

            // THEN
            assertThat(result).hasSize(1)
        }

    @Test
    fun givenDbHasData_AndOffsetIsHigherThanDataSize_WhenGetAllBookmarksIsCalled_ThenNoDataIsReturned() = runTest {
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
        val result = bookmarksDao.getAllBookmarks(
            query = BookmarksDao.allBookmarksFtsQuery(offset = list.size),
        )

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasData_AndTermFilterContainsChineseChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksNoFtsQuery(term = "还没"),
            )

            // THEN
            assertThat(result).containsExactly(
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
            )
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsChineseChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksNoFtsQuery(tag1 = "主题"),
            )

            // THEN
            assertThat(result).containsExactly(bookmarkChineseTag)
        }

    @Test
    fun givenDbHasData_AndTermFilterContainsCyrillicChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksNoFtsQuery(term = "ошибка"),
            )

            // THEN
            assertThat(result).containsExactly(
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
            )
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsCyrillicChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                bookmarkWithoutTerm,
                bookmarkChineseTitle,
                bookmarkChineseDescription,
                bookmarkChineseNotes,
                bookmarkChineseWebsiteTitle,
                bookmarkChineseWebsiteDescription,
                bookmarkChineseTag,
                bookmarkCyrillicTitle,
                bookmarkCyrillicDescription,
                bookmarkCyrillicNotes,
                bookmarkCyrillicWebsiteTitle,
                bookmarkCyrillicWebsiteDescription,
                bookmarkCyrillicTag,
            )
            bookmarksDao.saveBookmarks(list)

            // WHEN
            val result = bookmarksDao.getAllBookmarks(
                query = BookmarksDao.allBookmarksNoFtsQuery(tag1 = "Тема"),
            )

            // THEN
            assertThat(result).containsExactly(bookmarkCyrillicTag)
        }
    // endregion

    @Test
    fun whenSearchExistingBookmarkTagIsCalled_ThenAListOfStringsContainingThatTagIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            bookmarkWithoutTags,
            bookmarkWithOneTag,
            bookmarkWithTwoTags,
            bookmarkWithThreeTags,
        )
        bookmarksDao.saveBookmarks(list)

        // WHEN
        val result = bookmarksDao.searchExistingBookmarkTags(
            query = BookmarksDao.existingBookmarkTagFtsQuery(tag = SAMPLE_TAG_VALUE_1),
        )

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
    fun whenSearchExistingBookmarkTagIsCalledWithAQueryThatContainsAHyphen_ThenAListOfStringsContainingThatTagIsReturned() =
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
            val result = bookmarksDao.searchExistingBookmarkTags(
                query = BookmarksDao.existingBookmarkTagFtsQuery(tag = SAMPLE_TAG_VALUE_1),
            )

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
    fun whenGetPendingSyncedBookmarksIsCalled_ThenOnlyPendingSyncBookmarksAreReturned() = runTest {
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
    fun whenDeletePendingSyncedBookmarkIsCalled_ThenThatBookmarkIsDeleted() = runTest {
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
