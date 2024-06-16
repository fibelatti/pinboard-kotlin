package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.tooling.BaseDbTest
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.MockDataProvider.mockHash
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTagString3
import com.fibelatti.pinboard.MockDataProvider.mockTime1
import com.fibelatti.pinboard.MockDataProvider.mockTime2
import com.fibelatti.pinboard.MockDataProvider.mockTime3
import com.fibelatti.pinboard.MockDataProvider.mockTime4
import com.fibelatti.pinboard.MockDataProvider.mockTime5
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

class PostsDaoTest : BaseDbTest() {

    // region Data
    private val mockTerm = "ter" // Intentionally incomplete to test wildcard matching
    private val mockSecondTerm = "oth" // Intentionally incomplete to test wildcard matching

    private val postWithoutTerm = createPostDto(
        hash = randomHash(),
        href = "",
        description = "",
        extended = "",
    )
    private val postWithTermInTheHref = createPostDto(
        hash = randomHash(),
        href = "term with some other stuff",
        description = "",
        extended = "",
    )
    private val postWithTermInTheDescription = createPostDto(
        hash = randomHash(),
        href = "",
        description = "term-with-hyphen",
        extended = "",
    )
    private val postWithTermInTheExtended = createPostDto(
        hash = randomHash(),
        href = "",
        description = "",
        extended = "term",
    )

    private val postWithNoTags = createPostDto(hash = randomHash(), tags = "")
    private val postWithOneTag = createPostDto(hash = randomHash(), tags = mockTagString1)
    private val postWithTwoTags = createPostDto(
        hash = randomHash(),
        tags = listOf(mockTagString1, mockTagString2)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " "),
    )
    private val postWithThreeTags = createPostDto(
        hash = randomHash(),
        tags = listOf(mockTagString1, mockTagString2, mockTagString3)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " "),
    )

    private val postPublic = createPostDto(
        hash = randomHash(),
        shared = AppConfig.PinboardApiLiterals.YES,
    )
    private val postPrivate = createPostDto(
        hash = randomHash(),
        shared = AppConfig.PinboardApiLiterals.NO,
    )

    private val postReadLater = createPostDto(
        hash = randomHash(),
        toread = AppConfig.PinboardApiLiterals.YES,
    )
    private val postNotReadLater = createPostDto(
        hash = randomHash(),
        toread = AppConfig.PinboardApiLiterals.NO,
    )

    private val postFirst = createPostDto(hash = randomHash(), description = "A title", time = mockTime1)
    private val postSecond = createPostDto(hash = randomHash(), description = "B title", time = mockTime2)
    private val postThird = createPostDto(hash = randomHash(), description = "C title", time = mockTime3)
    private val postFourth = createPostDto(hash = randomHash(), description = "D title", time = mockTime4)
    private val postFifth = createPostDto(hash = randomHash(), description = "E title", time = mockTime5)
    // endregion

    private val postsDao get() = appDatabase.postDao()

    private fun randomHash(): String = UUID.randomUUID().toString()

    @Test
    fun whenDeleteIsCalledThenAllDataIsDeleted() = runTest {
        // GIVEN
        val list = listOf(createPostDto(), createPostDto(hash = "other-$mockHash"))
        postsDao.savePosts(list)

        // WHEN
        postsDao.deleteAllPosts()

        // THEN
        val result = postsDao.getAllPosts()
        assertThat(result).isEmpty()
    }

    @Test
    fun whenDeleteAllSyncedPostsIsCalledThenOnlySyncedPostsAreDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$mockHash"),
            createPostDto(hash = "not-synced-add", pendingSync = PendingSyncDto.ADD),
            createPostDto(hash = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
            createPostDto(hash = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
        )
        postsDao.savePosts(list)

        // WHEN
        postsDao.deleteAllSyncedPosts()

        // THEN
        val result = postsDao.getAllPosts()
        assertThat(result).isEqualTo(
            listOf(
                createPostDto(hash = "not-synced-add", pendingSync = PendingSyncDto.ADD),
                createPostDto(hash = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
                createPostDto(hash = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }

    @Test
    fun givenEntryAlreadyExistsInDbWhenSavePostsIsCalledThenReplaceIsUsed() = runTest {
        // GIVEN
        val original = createPostDto(toread = AppConfig.PinboardApiLiterals.YES)
        val modified = createPostDto(toread = AppConfig.PinboardApiLiterals.NO)
        val other = createPostDto(hash = "other-$mockHash")
        val another = createPostDto(hash = "another-$mockHash")

        val list = listOf(original, other)
        postsDao.savePosts(list)

        // WHEN
        postsDao.savePosts(listOf(modified, another))

        // THEN
        val result = postsDao.getAllPosts()
        assertThat(result).containsExactly(modified, other, another)
    }

    // region getPostCount
    @Test
    fun givenDbHasNoDataWhenGetPostCountIsCalledThenZeroIsReturned() = runTest {
        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun givenDbHasDataWhenGetPostCountIsCalledThenCountIsReturned() = runTest {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(list.size)
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(term = PostsDao.preFormatTerm(mockTerm))

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                term = PostsDao.preFormatTerm("$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(term = PostsDao.preFormatTerm("term-with"))

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(tag1 = PostsDao.preFormatTag(mockTagString1))

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(tag1 = PostsDao.preFormatTag(mockTagString1))

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                tag1 = PostsDao.preFormatTag(mockTagString1),
                tag2 = PostsDao.preFormatTag(mockTagString2),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                tag1 = PostsDao.preFormatTag(mockTagString1),
                tag2 = PostsDao.preFormatTag(mockTagString2),
                tag3 = PostsDao.preFormatTag(mockTagString3),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(untaggedOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndIgnoreVisibilityFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(ignoreVisibility = true)

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasDataAndPublicPostsOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(publicPostsOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndPrivatePostsOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(privatePostsOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postReadLater,
                postNotReadLater,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(readLaterOnly = true)

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetPostCountIsCalledThenTheLimitSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(limit = list.size - 1)

        // THEN
        assertThat(result).isEqualTo(list.size - 1)
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetPostCountIsCalledThenTheDataSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(limit = list.size + 1)

        // THEN
        assertThat(result).isEqualTo(list.size)
    }
    // endregion

    // region getAllPosts
    @Test
    fun givenDbHasNoDataWhenGetAllPostsIsCalledThenEmptyListIsReturned() = runTest {
        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasDataWhenGetAllPostsIsCalledThenListIsReturned() = runTest {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndSortTypeIs0WhenGetAllPostsIsCalledThenPostsAreReturnedOrderByTimeDesc() = runTest {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(sortType = 0)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFifth,
                postFourth,
                postThird,
                postSecond,
                postFirst,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs1WhenGetAllPostsIsCalledThenPostsAreReturnedOrderByTimeAsc() = runTest {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(sortType = 1)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFirst,
                postSecond,
                postThird,
                postFourth,
                postFifth,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs2WhenGetAllPostsIsCalledThenPostsAreReturnedOrderByDescriptionAsc() = runTest {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(sortType = 2)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFirst,
                postSecond,
                postThird,
                postFourth,
                postFifth,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndSortTypeIs3WhenGetAllPostsIsCalledThenPostsAreReturnedOrderByDescriptionDesc() = runTest {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(sortType = 3)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFifth,
                postFourth,
                postThird,
                postSecond,
                postFirst,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                term = PostsDao.preFormatTerm("$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTermInTheHref))
        }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(term = PostsDao.preFormatTerm(mockTerm))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(term = PostsDao.preFormatTerm("term-with"))

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTermInTheDescription))
        }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(tag1 = PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
    }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(tag1 = PostsDao.preFormatTag(mockTagString1))

            // THEN
            assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                tag1 = PostsDao.preFormatTag(mockTagString1),
                tag2 = PostsDao.preFormatTag(mockTagString2),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTwoTags, postWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                tag1 = PostsDao.preFormatTag(mockTagString1),
                tag2 = PostsDao.preFormatTag(mockTagString2),
                tag3 = PostsDao.preFormatTag(mockTagString3),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithThreeTags))
        }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(untaggedOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(postWithNoTags))
        }

    @Test
    fun givenDbHasDataAndIgnoreVisibilityFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(ignoreVisibility = true)

            // THEN
            assertThat(result).isEqualTo(listOf(postPublic, postPrivate))
        }

    @Test
    fun givenDbHasDataAndPublicPostsOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(publicPostsOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(postPublic))
        }

    @Test
    fun givenDbHasDataAndPrivatePostsOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(privatePostsOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(postPrivate))
        }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postReadLater,
                postNotReadLater,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(readLaterOnly = true)

            // THEN
            assertThat(result).isEqualTo(listOf(postReadLater))
        }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetAllPostsIsCalledThenTheLimitSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(limit = list.size - 1)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithoutTerm,
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended,
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
                postPublic,
                postPrivate,
                postReadLater,
            ),
        )
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetAllPostsIsCalledThenTheDataSizeIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(limit = list.size + 1)

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndAndOffsetIsLowerThenDataSizeWhenGetAllPostsIsCalledThenTheRemainingDataIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(offset = list.size - 1)

        // THEN
        assertThat(result).hasSize(1)
    }

    @Test
    fun givenDbHasDataAndAndOffsetIsHigherThenDataSizeWhenGetAllPostsIsCalledThenNoDataIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended,
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
            postPublic,
            postPrivate,
            postReadLater,
            postNotReadLater,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(offset = list.size)

        // THEN
        assertThat(result).isEmpty()
    }
    // endregion

    @Test
    fun whenSearchExistingPostTagIsCalledThenAListOfStringsContainingThatTagIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.searchExistingPostTag(PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithOneTag.tags,
                postWithTwoTags.tags,
                postWithThreeTags.tags,
            ),
        )
    }

    @Test
    fun whenSearchExistingPostTagIsCalledWithAQueryThatContainsAHyphenThenAListOfStringsContainingThatTagIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithNoTags,
                postWithOneTag,
                postWithTwoTags,
                postWithThreeTags,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.searchExistingPostTag(PostsDao.preFormatTag(mockTagString1))

            // THEN
            assertThat(result).isEqualTo(
                listOf(
                    postWithOneTag.tags,
                    postWithTwoTags.tags,
                    postWithThreeTags.tags,
                ),
            )
        }

    @Test
    fun whenGetPendingSyncedPostsIsCalledThenOnlyPendingSyncPostsAreReturned() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$mockHash"),
            createPostDto(hash = "not-synced-add", pendingSync = PendingSyncDto.ADD),
            createPostDto(hash = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
            createPostDto(hash = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPendingSyncPosts()

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                createPostDto(hash = "not-synced-add", pendingSync = PendingSyncDto.ADD),
                createPostDto(hash = "not-synced-update", pendingSync = PendingSyncDto.UPDATE),
                createPostDto(hash = "not-synced-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }

    @Test
    fun whenDeletePendingSyncedPostIsCalledThenThatPostIsDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$mockHash"),
            createPostDto(hash = "not-synced-add", href = "href-add", pendingSync = PendingSyncDto.ADD),
            createPostDto(hash = "not-synced-update", href = "href-update", pendingSync = PendingSyncDto.UPDATE),
            createPostDto(hash = "not-synced-delete", href = "href-delete", pendingSync = PendingSyncDto.DELETE),
        )
        postsDao.savePosts(list)

        // WHEN
        postsDao.deletePendingSyncPost(url = "href-add")

        // THEN
        val result = postsDao.getAllPosts()
        assertThat(result).isEqualTo(
            listOf(
                createPostDto(),
                createPostDto(hash = "other-$mockHash"),
                createPostDto(hash = "not-synced-update", href = "href-update", pendingSync = PendingSyncDto.UPDATE),
                createPostDto(hash = "not-synced-delete", href = "href-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }
}
