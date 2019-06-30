package com.fibelatti.pinboard.features.posts.data

import androidx.test.runner.AndroidJUnit4
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldContain
import com.fibelatti.pinboard.BaseDbTest
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.MockDataProvider.mockHash
import com.fibelatti.pinboard.core.AppConfig
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostsDaoTest : BaseDbTest() {

    private val postsDao get() = appDatabase.postDao()

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
}
