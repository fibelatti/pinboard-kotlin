package com.fibelatti.pinboard.features.posts.data

import androidx.test.runner.AndroidJUnit4
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldContain
import com.fibelatti.core.test.extension.sizeShouldBe
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
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PostsDaoTest : BaseDbTest() {

    // region Data
    private val mockTerm = "term"

    private val postWithoutTerm = createPostDto(hash = randomHash(), href = "", description = "", extended = "")
    private val postWithTermInTheHref = createPostDto(hash = randomHash(), href = "term", description = "", extended = "")
    private val postWithTermInTheDescription = createPostDto(hash = randomHash(), href = "", description = "term", extended = "")
    private val postWithTermInTheExtended = createPostDto(hash = randomHash(), href = "", description = "", extended = "term")

    private val postWithNoTags = createPostDto(hash = randomHash(), tags = "")
    private val postWithOneTag = createPostDto(hash = randomHash(), tags = mockTagString1)
    private val postWithTwoTags = createPostDto(hash = randomHash(), tags = "$mockTagString1 $mockTagString2")
    private val postWithThreeTags = createPostDto(hash = randomHash(), tags = "$mockTagString1 $mockTagString2 $mockTagString3")

    private val postPublic = createPostDto(hash = randomHash(), shared = AppConfig.PinboardApiLiterals.YES)
    private val postPrivate = createPostDto(hash = randomHash(), shared = AppConfig.PinboardApiLiterals.NO)

    private val postReadLater = createPostDto(hash = randomHash(), toread = AppConfig.PinboardApiLiterals.YES)
    private val postNotReadLater = createPostDto(hash = randomHash(), toread = AppConfig.PinboardApiLiterals.NO)

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
        result.isEmpty() shouldBe true
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
        result shouldContain listOf(modified, other, another)
    }

    // region getPostCount
    @Test
    fun givenDbHasNoDataWhenGetPostCountIsCalledThenZeroIsReturned() {
        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        result shouldBe 0
    }

    @Test
    fun givenDbHasDataWhenGetPostCountIsCalledThenCountIsReturned() {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getPostCount()

        // THEN
        result shouldBe list.size
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
        val result = postsDao.getPostCount(term = mockTerm)

        // THEN
        result shouldBe 3
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
        val result = postsDao.getPostCount(tag1 = mockTagString1)

        // THEN
        result shouldBe 3
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
        val result = postsDao.getPostCount(tag1 = mockTagString1, tag2 = mockTagString2)

        // THEN
        result shouldBe 2
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
        val result = postsDao.getPostCount(tag1 = mockTagString1, tag2 = mockTagString2, tag3 = mockTagString3)

        // THEN
        result shouldBe 1
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
        result shouldBe 1
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
        result shouldBe 1
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
        result shouldBe 1
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
        result shouldBe 1
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
        result shouldBe list.size - 1
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
        result shouldBe list.size
    }
    // endregion

    // region getAllPosts {
    @Test
    fun givenDbHasNoDataWhenGetAllPostsIsCalledThenEmptyListIsReturned() {
        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        result.isEmpty() shouldBe true
    }

    @Test
    fun givenDbHasDataWhenGetAllPostsIsCalledThenListIsReturned() {
        // GIVEN
        val list = listOf(createPostDto())
        postsDao.savePosts(list)

        // WHEN
        val result = postsDao.getAllPosts()

        // THEN
        result shouldBe list
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
        result shouldBe listOf(
            postFifth,
            postFourth,
            postThird,
            postSecond,
            postFirst
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
        result shouldBe listOf(
            postFirst,
            postSecond,
            postThird,
            postFourth,
            postFifth
        )
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
        val result = postsDao.getAllPosts(term = mockTerm)

        // THEN
        result shouldBe listOf(
            postWithTermInTheHref,
            postWithTermInTheDescription,
            postWithTermInTheExtended
        )
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
        val result = postsDao.getAllPosts(tag1 = mockTagString1)

        // THEN
        result shouldBe listOf(postWithOneTag, postWithTwoTags, postWithThreeTags)
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
        val result = postsDao.getAllPosts(tag1 = mockTagString1, tag2 = mockTagString2)

        // THEN
        result shouldBe listOf(postWithTwoTags, postWithThreeTags)
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
        val result = postsDao.getAllPosts(tag1 = mockTagString1, tag2 = mockTagString2, tag3 = mockTagString3)

        // THEN
        result shouldBe listOf(postWithThreeTags)
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
        result shouldBe listOf(postWithNoTags)
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
        result shouldBe listOf(postPublic)
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
        result shouldBe listOf(postPrivate)
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
        result shouldBe listOf(postReadLater)
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
        result shouldBe listOf(
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
        result shouldBe list
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
        result sizeShouldBe 1
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
        result shouldBe emptyList<Post>()
    }
    // endregion
}
