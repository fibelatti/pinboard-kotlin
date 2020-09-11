package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.BaseDbTest
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
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class PostsDaoTest : BaseDbTest() {

    // region Data
    private val mockTerm = "ter" // Intentionally incomplete to test wildcard matching
    private val mockSecondTerm = "second" // Intentionally incomplete to test wildcard matching

    private val postWithoutTerm = createPostDto(
        hash = randomHash(),
        href = "",
        description = "",
        extended = ""
    )
    private val postWithTermInTheHref = createPostDto(
        hash = randomHash(),
        href = "term second",
        description = "",
        extended = ""
    )
    private val postWithTermInTheDescription = createPostDto(
        hash = randomHash(),
        href = "",
        description = "term-with-hyphen",
        extended = ""
    )
    private val postWithTermInTheExtended = createPostDto(
        hash = randomHash(),
        href = "",
        description = "",
        extended = "term"
    )

    private val postWithNoTags = createPostDto(hash = randomHash(), tags = "")
    private val postWithOneTag = createPostDto(hash = randomHash(), tags = mockTagString1)
    private val postWithTwoTags = createPostDto(
        hash = randomHash(),
        tags = listOf(mockTagString1, mockTagString2)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " ")
    )
    private val postWithThreeTags = createPostDto(
        hash = randomHash(),
        tags = listOf(mockTagString1, mockTagString2, mockTagString3)
            .shuffled() // Intentionally shuffled because the order shouldn't matter
            .joinToString(separator = " ")
    )

    private val postPublic = createPostDto(
        hash = randomHash(),
        shared = AppConfig.PinboardApiLiterals.YES
    )
    private val postPrivate = createPostDto(
        hash = randomHash(),
        shared = AppConfig.PinboardApiLiterals.NO
    )

    private val postReadLater = createPostDto(
        hash = randomHash(),
        toread = AppConfig.PinboardApiLiterals.YES
    )
    private val postNotReadLater = createPostDto(
        hash = randomHash(),
        toread = AppConfig.PinboardApiLiterals.NO
    )

    private val postFirst = createPostDto(hash = randomHash(), time = mockTime1)
    private val postSecond = createPostDto(hash = randomHash(), time = mockTime2)
    private val postThird = createPostDto(hash = randomHash(), time = mockTime3)
    private val postFourth = createPostDto(hash = randomHash(), time = mockTime4)
    private val postFifth = createPostDto(hash = randomHash(), time = mockTime5)
    // endregion

    private val postsDao get() = appDatabase.postDao()

    private fun randomHash(): String = UUID.randomUUID().toString()

    @Test
    fun whenDeleteIsCalledThenAllDataIsDeleted() {
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
    fun givenEntryAlreadyExistsInDbWhenSavePostsIsCalledThenReplaceIsUsed() {
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
    fun givenDbHasNoDataWhenGetPostCountIsCalledThenZeroIsReturned() {
        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun givenDbHasDataWhenGetPostCountIsCalledThenCountIsReturned() {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        assertThat(result).isEqualTo(list.size)
    }

    @Test
    @Suppress("MagicNumber")
    fun givenDbHasDataAndTermFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(term = PostsDao.preFormatTerm(mockTerm))

        // THEN
        assertThat(result).isEqualTo(3)
    }

    @Test
    @Suppress("MagicNumber")
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(term = PostsDao.preFormatTerm("$mockTerm $mockSecondTerm"))

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(term = PostsDao.preFormatTerm("term-with"))

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    @Suppress("MagicNumber")
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(tag1 = PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(3)
    }

    @Test
    @Suppress("MagicNumber")
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(tag1 = PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(
            tag1 = PostsDao.preFormatTag(mockTagString1),
            tag2 = PostsDao.preFormatTag(mockTagString2)
        )

        // THEN
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(
            tag1 = PostsDao.preFormatTag(mockTagString1),
            tag2 = PostsDao.preFormatTag(mockTagString2),
            tag3 = PostsDao.preFormatTag(mockTagString3)
        )

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(untaggedOnly = true)

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndPublicPostsOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postPublic,
            postPrivate
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(publicPostsOnly = true)

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndPrivatePostsOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postPublic,
            postPrivate
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(privatePostsOnly = true)

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetPostCountIsCalledThenCountOfPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postReadLater,
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(readLaterOnly = true)

        // THEN
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetPostCountIsCalledThenTheLimitSizeIsReturned() {
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
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount(limit = list.size - 1)

        // THEN
        assertThat(result).isEqualTo(list.size - 1)
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetPostCountIsCalledThenTheDataSizeIsReturned() {
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
            postNotReadLater
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
    fun givenDbHasNoDataWhenGetAllPostsIsCalledThenEmptyListIsReturned() {
        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun givenDbHasDataWhenGetAllPostsIsCalledThenListIsReturned() {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndNewestFirstIsTrueWhenGetAllPostsIsCalledThenPostsAreReturnedOrderByTimeDesc() {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(newestFirst = true)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFifth,
                postFourth,
                postThird,
                postSecond,
                postFirst
            )
        )
    }

    @Test
    fun givenDbHasDataAndNewestFirstIsFalseWhenGetAllPostsIsCalledThenPostsAreReturnedOrderByTimeAsc() {
        // GIVEN
        val list = listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(newestFirst = false)

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postFirst,
                postSecond,
                postThird,
                postFourth,
                postFifth
            )
        )
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWithMoreThanOneWordWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(term = PostsDao.preFormatTerm("$mockTerm $mockSecondTerm"))

        // THEN
        assertThat(result).isEqualTo(listOf(postWithTermInTheHref))
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(term = PostsDao.preFormatTerm(mockTerm))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithTermInTheHref,
                postWithTermInTheDescription,
                postWithTermInTheExtended
            )
        )
    }

    @Test
    fun givenDbHasDataAndTermFilterIsPassedAndItContainsAHyphenWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithoutTerm,
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(term = PostsDao.preFormatTerm("term-with"))

        // THEN
        assertThat(result).isEqualTo(listOf(postWithTermInTheDescription))
    }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(tag1 = PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
    }

    @Test
    fun givenDbHasDataAndTag1FilterIsPassedAndItContainsAHyphenWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(tag1 = PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(listOf(postWithOneTag, postWithTwoTags, postWithThreeTags))
    }

    @Test
    fun givenDbHasDataAndTag1AndTag2FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(
            tag1 = PostsDao.preFormatTag(mockTagString1),
            tag2 = PostsDao.preFormatTag(mockTagString2)
        )

        // THEN
        assertThat(result).isEqualTo(listOf(postWithTwoTags, postWithThreeTags))
    }

    @Test
    fun givenDbHasDataAndTag1AndTag2AndTag3FilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(
            tag1 = PostsDao.preFormatTag(mockTagString1),
            tag2 = PostsDao.preFormatTag(mockTagString2),
            tag3 = PostsDao.preFormatTag(mockTagString3)
        )

        // THEN
        assertThat(result).isEqualTo(listOf(postWithThreeTags))
    }

    @Test
    fun givenDbHasDataAndUntaggedOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(untaggedOnly = true)

        // THEN
        assertThat(result).isEqualTo(listOf(postWithNoTags))
    }

    @Test
    fun givenDbHasDataAndPublicPostsOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postPublic,
            postPrivate
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(publicPostsOnly = true)

        // THEN
        assertThat(result).isEqualTo(listOf(postPublic))
    }

    @Test
    fun givenDbHasDataAndPrivatePostsOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postPublic,
            postPrivate
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(privatePostsOnly = true)

        // THEN
        assertThat(result).isEqualTo(listOf(postPrivate))
    }

    @Test
    fun givenDbHasDataAndReadLaterOnlyFilterIsPassedWhenGetAllPostsIsCalledThenPostsThatMatchTheFilterIsReturned() {
        // GIVEN
        val list = listOf(
            postReadLater,
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(readLaterOnly = true)

        // THEN
        assertThat(result).isEqualTo(listOf(postReadLater))
    }

    @Test
    fun givenDbHasDataAndAndLimitIsLowerThenDataSizeWhenGetAllPostsIsCalledThenTheLimitSizeIsReturned() {
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
            postNotReadLater
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
                postReadLater
            )
        )
    }

    @Test
    fun givenDbHasDataAndAndLimitIsHigherThenDataSizeWhenGetAllPostsIsCalledThenTheDataSizeIsReturned() {
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
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(limit = list.size + 1)

        // THEN
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun givenDbHasDataAndAndOffsetIsLowerThenDataSizeWhenGetAllPostsIsCalledThenTheRemainingDataIsReturned() {
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
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(offset = list.size - 1)

        // THEN
        assertThat(result).hasSize(1)
    }

    @Test
    fun givenDbHasDataAndAndOffsetIsHigherThenDataSizeWhenGetAllPostsIsCalledThenNoDataIsReturned() {
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
            postNotReadLater
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts(offset = list.size)

        // THEN
        assertThat(result).isEmpty()
    }
    // endregion

    @Test
    fun whenSearchExistingPostTagIsCalledThenAListOfStringsContainingThatTagIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.searchExistingPostTag(PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithOneTag.tags,
                postWithTwoTags.tags,
                postWithThreeTags.tags
            )
        )
    }

    @Test
    fun whenSearchExistingPostTagIsCalledWithAQueryThatContainsAHyphenThenAListOfStringsContainingThatTagIsReturned() {
        // GIVEN
        val list = listOf(
            postWithNoTags,
            postWithOneTag,
            postWithTwoTags,
            postWithThreeTags
        )
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.searchExistingPostTag(PostsDao.preFormatTag(mockTagString1))

        // THEN
        assertThat(result).isEqualTo(
            listOf(
                postWithOneTag.tags,
                postWithTwoTags.tags,
                postWithThreeTags.tags
            )
        )
    }
}
