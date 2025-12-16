@file:Suppress("ktlint:standard:max-line-length")

package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_3
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_4
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME_5
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_HASH
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUE_3
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.tooling.BaseDbTest
import com.google.common.truth.Truth.assertThat
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Test

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

    private val postChineseDescription = createPostDto(
        hash = randomHash(),
        description = "这个Bug还没有解决",
    )
    private val postChineseExtended = createPostDto(
        hash = randomHash(),
        extended = "这个Bug还没有解决",
    )
    private val postChineseTag = createPostDto(
        hash = randomHash(),
        tags = "主题",
    )
    private val postCyrillicDescription = createPostDto(
        hash = randomHash(),
        description = "Эта ошибка еще не устранена.",
    )
    private val postCyrillicExtended = createPostDto(
        hash = randomHash(),
        extended = "Эта ошибка еще не устранена.",
    )
    private val postCyrillicTag = createPostDto(
        hash = randomHash(),
        tags = "Тема",
    )

    private val postWithNoTags = createPostDto(hash = randomHash(), tags = "")
    private val postWithOneTag = createPostDto(hash = randomHash(), tags = SAMPLE_TAG_VALUE_1)
    private val postWithTwoTags = createPostDto(
        hash = randomHash(),
        tags = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " "),
    )
    private val postWithThreeTags = createPostDto(
        hash = randomHash(),
        tags = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2, SAMPLE_TAG_VALUE_3)
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

    private val postFirst = createPostDto(hash = randomHash(), description = "A title", time = SAMPLE_DATE_TIME_1)
    private val postSecond = createPostDto(hash = randomHash(), description = "B title", time = SAMPLE_DATE_TIME_2)
    private val postThird = createPostDto(hash = randomHash(), description = "C title", time = SAMPLE_DATE_TIME_3)
    private val postFourth = createPostDto(hash = randomHash(), description = "D title", time = SAMPLE_DATE_TIME_4)
    private val postFifth = createPostDto(hash = randomHash(), description = "E title", time = SAMPLE_DATE_TIME_5)
    // endregion

    private val postsDao get() = appDatabase.postDao()

    private fun randomHash(): String = UUID.randomUUID().toString()

    @Test
    fun whenDeleteIsCalled_ThenAllDataIsDeleted() = runTest {
        // GIVEN
        val list = listOf(createPostDto(), createPostDto(hash = "other-$SAMPLE_HASH"))
        postsDao.savePosts(list)

        // WHEN
        postsDao.deleteAllPosts()

        // THEN
        val result = postsDao.getAllPosts()
        assertThat(result).isEmpty()
    }

    @Test
    fun whenDeleteAllSyncedPostsIsCalled_ThenOnlySyncedPostsAreDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$SAMPLE_HASH"),
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
    fun givenEntryAlreadyExistsInDb_WhenSavePostsIsCalled_ThenReplaceIsUsed() = runTest {
        // GIVEN
        val original = createPostDto(toread = AppConfig.PinboardApiLiterals.YES)
        val modified = createPostDto(toread = AppConfig.PinboardApiLiterals.NO)
        val other = createPostDto(hash = "other-$SAMPLE_HASH")
        val another = createPostDto(hash = "another-$SAMPLE_HASH")

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
    fun givenDbHasNoData_WhenGetPostCountIsCalled_ThenZeroIsReturned() = runTest {
        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun givenDbHasData_WhenGetPostCountIsCalled_ThenCountIsReturned() = runTest {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(list.size)
    }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(term = mockTerm),
            )

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassedWithMoreThanOneWord_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(term = "$mockTerm $mockSecondTerm"),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_AndItContainsAHyphen_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(term = "term-with"),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_AndItContainsAHyphen_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(tag1 = SAMPLE_TAG_VALUE_1),
            )

            // THEN
            assertThat(result).isEqualTo(3)
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2FilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2_AndTag3FilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                    tag3 = SAMPLE_TAG_VALUE_3,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndUntaggedOnlyFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.postCountFtsQuery(
                    untaggedOnly = true,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndIgnoreVisibilityFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountFtsQuery(
                    postVisibility = PostVisibility.None,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndPublicPostsOnlyFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountFtsQuery(
                    postVisibility = PostVisibility.Public,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndPrivatePostsOnlyFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountFtsQuery(
                    postVisibility = PostVisibility.Private,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndReadLaterOnlyFilterIsPassed_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postReadLater,
                postNotReadLater,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountFtsQuery(
                    readLaterOnly = true,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndLimitIsLowerThanDataSize_WhenGetPostCountIsCalled_ThenTheLimitSizeIsReturned() = runTest {
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
        val result = postsDao.getPostCount(
            query = PostsDao.postCountFtsQuery(
                limit = list.size - 1,
            ),
        )

        // THEN
        assertThat(result).isEqualTo(list.size - 1)
    }

    @Test
    fun givenDbHasData_AndLimitIsHigherThanDataSize_WhenGetPostCountIsCalled_ThenTheDataSizeIsReturned() = runTest {
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
        val result = postsDao.getPostCount(
            query = PostsDao.postCountFtsQuery(
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
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountNoFtsQuery(
                    term = "还没",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsChineseChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountNoFtsQuery(
                    tag1 = "主题",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }

    @Test
    fun givenDbHasData_AndTermFilterContainsCyrillicChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountFtsQuery(
                    term = "ошибка",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(2)
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsCyrillicChars_WhenGetPostCountIsCalled_ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getPostCount(
                query = PostsDao.postCountNoFtsQuery(
                    tag1 = "Тема",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(1)
        }
    // endregion

    // region getAllPosts
    @Test
    fun givenDbHasNoData_WhenGetAllPostsIsCalled_ThenEmptyListIsReturned() = runTest {
        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasData_WhenGetAllPostsIsCalled_ThenListIsReturned() = runTest {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasData_AndSortTypeIs0_WhenGetAllPostsIsCalled_ThenPostsAreReturnedOrderByTimeDesc() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                sortType = 0,
            ),
        )

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
    fun givenDbHasData_AndSortTypeIs1_WhenGetAllPostsIsCalled_ThenPostsAreReturnedOrderByTimeAsc() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                sortType = 1,
            ),
        )

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
    fun givenDbHasData_AndSortTypeIs2_WhenGetAllPostsIsCalled_ThenPostsAreReturnedOrderByDescriptionAsc() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                sortType = 4,
            ),
        )

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
    fun givenDbHasData_AndSortTypeIs3_WhenGetAllPostsIsCalled_ThenPostsAreReturnedOrderByDescriptionDesc() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                sortType = 5,
            ),
        )

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
    fun givenDbHasData_AndTermFilterIsPassedWithMoreThanOneWord_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    term = "$mockTerm $mockSecondTerm",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTermInTheHref))
        }

    @Test
    fun givenDbHasData_AndTermFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() = runTest {
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
            query = PostsDao.allPostsFtsQuery(
                term = mockTerm,
            ),
        )

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
    fun givenDbHasData_AndTermFilterIsPassed_AndItContainsAHyphen_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    term = "term-with",
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTermInTheDescription))
        }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() = runTest {
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
            query = PostsDao.allPostsFtsQuery(
                tag1 = SAMPLE_TAG_VALUE_1,
            ),
        )

        // THEN
        assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
    }

    @Test
    fun givenDbHasData_AndTag1FilterIsPassed_AndItContainsAHyphen_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2FilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithTwoTags, postWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndTag1_AndTag2_AndTag3FilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    tag1 = SAMPLE_TAG_VALUE_1,
                    tag2 = SAMPLE_TAG_VALUE_2,
                    tag3 = SAMPLE_TAG_VALUE_3,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithThreeTags))
        }

    @Test
    fun givenDbHasData_AndUntaggedOnlyFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
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
                query = PostsDao.allPostsFtsQuery(
                    untaggedOnly = true,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postWithNoTags))
        }

    @Test
    fun givenDbHasData_AndIgnoreVisibilityFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsFtsQuery(
                    postVisibility = PostVisibility.None,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postPublic, postPrivate))
        }

    @Test
    fun givenDbHasData_AndPublicPostsOnlyFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsFtsQuery(
                    postVisibility = PostVisibility.Public,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postPublic))
        }

    @Test
    fun givenDbHasData_AndPrivatePostsOnlyFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postPublic,
                postPrivate,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsFtsQuery(
                    postVisibility = PostVisibility.Private,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postPrivate))
        }

    @Test
    fun givenDbHasData_AndReadLaterOnlyFilterIsPassed_WhenGetAllPostsIsCalled_ThenPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postReadLater,
                postNotReadLater,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsFtsQuery(
                    readLaterOnly = true,
                ),
            )

            // THEN
            assertThat(result).isEqualTo(listOf(postReadLater))
        }

    @Test
    fun givenDbHasData_AndLimitIsLowerThanDataSize_WhenGetAllPostsIsCalled_ThenTheLimitSizeIsReturned() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                limit = list.size - 1,
            ),
        )

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
    fun givenDbHasData_AndLimitIsHigherThanDataSize_WhenGetAllPostsIsCalled_ThenTheDataSizeIsReturned() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                limit = list.size + 1,
            ),
        )

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasData_AndOffsetIsLowerThanDataSize_WhenGetAllPostsIsCalled_ThenTheRemainingDataIsReturned() =
        runTest {
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
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsFtsQuery(
                    offset = list.size - 1,
                ),
            )

            // THEN
            assertThat(result).hasSize(1)
        }

    @Test
    fun givenDbHasData_AndOffsetIsHigherThanDataSize_WhenGetAllPostsIsCalled_ThenNoDataIsReturned() = runTest {
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
        val result = postsDao.getAllPosts(
            query = PostsDao.allPostsFtsQuery(
                offset = list.size,
            ),
        )

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasData_AndTermFilterContainsChineseChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsNoFtsQuery(
                    term = "还没",
                ),
            )

            // THEN
            assertThat(result).containsExactly(
                postChineseDescription,
                postChineseExtended,
            )
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsChineseChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsNoFtsQuery(
                    tag1 = "主题",
                ),
            )

            // THEN
            assertThat(result).containsExactly(postChineseTag)
        }

    @Test
    fun givenDbHasData_AndTermFilterContainsCyrillicChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsNoFtsQuery(
                    term = "ошибка",
                ),
            )

            // THEN
            assertThat(result).containsExactly(
                postCyrillicExtended,
                postCyrillicDescription,
            )
        }

    @Test
    fun givenDbHasData_AndTagFilterContainsCyrillicChars_WhenGetAllPostsIsCalled__ThenCountOfPostsThatMatchTheFilterIsReturned() =
        runTest {
            // GIVEN
            val list = listOf(
                postWithoutTerm,
                postChineseDescription,
                postChineseExtended,
                postChineseTag,
                postCyrillicExtended,
                postCyrillicDescription,
                postCyrillicTag,
            )
            postsDao.savePosts(list)

            // WHEN
            val result = postsDao.getAllPosts(
                query = PostsDao.allPostsNoFtsQuery(
                    tag1 = "Тема",
                ),
            )

            // THEN
            assertThat(result).containsExactly(postCyrillicTag)
        }
    // endregion

    @Test
    fun whenSearchExistingPostTagIsCalled_ThenAListOfStringsContainingThatTagIsReturned() = runTest {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags,
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.searchExistingPostTag(
            query = PostsDao.existingPostTagFtsQuery(tag = SAMPLE_TAG_VALUE_1),
        )

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
    fun whenSearchExistingPostTagIsCalledWithAQueryThatContainsAHyphen_ThenAListOfStringsContainingThatTagIsReturned() =
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
            val result = postsDao.searchExistingPostTag(
                query = PostsDao.existingPostTagFtsQuery(tag = SAMPLE_TAG_VALUE_1),
            )

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
    fun whenGetPendingSyncedPostsIsCalled_ThenOnlyPendingSyncPostsAreReturned() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$SAMPLE_HASH"),
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
    fun whenDeletePendingSyncedPostIsCalled_ThenThatPostIsDeleted() = runTest {
        // GIVEN
        val list = listOf(
            createPostDto(),
            createPostDto(hash = "other-$SAMPLE_HASH"),
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
                createPostDto(hash = "other-$SAMPLE_HASH"),
                createPostDto(hash = "not-synced-update", href = "href-update", pendingSync = PendingSyncDto.UPDATE),
                createPostDto(hash = "not-synced-delete", href = "href-delete", pendingSync = PendingSyncDto.DELETE),
            ),
        )
    }
}
