package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class GetRecentPostsTest {

    private val mockResponse = mockk<PostListResult>()

    private val mockPostsRepository = mockk<PostsRepository>(relaxed = true)

    private val getRecentPosts = GetRecentPosts(mockPostsRepository)

    fun setup() {
        coEvery {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                publicPostsOnly = any(),
                privatePostsOnly = any(),
                readLaterOnly = any(),
                countLimit = any(),
                pageLimit = any(),
                pageOffset = any()
            )
        } returns flowOf(Success(mockResponse))
    }

    @Test
    fun `GIVEN search term was set in the params WHEN getRecentPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(searchTerm = mockUrlValid)

        // WHEN
        runBlocking { getRecentPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = mockUrlValid,
                tags = null,
                untaggedOnly = false,
                publicPostsOnly = false,
                privatePostsOnly = false,
                readLaterOnly = false,
                countLimit = DEFAULT_RECENT_QUANTITY,
                pageLimit = DEFAULT_RECENT_QUANTITY,
                pageOffset = 0
            )
        }
    }

    @Test
    fun `GIVEN tagParams was None WHEN getRecentPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tagParams = GetPostParams.Tags.None)

        // WHEN
        runBlocking { getRecentPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = "",
                tags = null,
                untaggedOnly = false,
                publicPostsOnly = false,
                privatePostsOnly = false,
                readLaterOnly = false,
                countLimit = DEFAULT_RECENT_QUANTITY,
                pageLimit = DEFAULT_RECENT_QUANTITY,
                pageOffset = 0
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Untagged WHEN getRecentPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tagParams = GetPostParams.Tags.Untagged)

        // WHEN
        runBlocking { getRecentPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = "",
                tags = null,
                untaggedOnly = false,
                publicPostsOnly = false,
                privatePostsOnly = false,
                readLaterOnly = false,
                countLimit = DEFAULT_RECENT_QUANTITY,
                pageLimit = DEFAULT_RECENT_QUANTITY,
                pageOffset = 0
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Tagged WHEN getRecentPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tagParams = GetPostParams.Tags.Tagged(mockTags))

        // WHEN
        runBlocking { getRecentPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = "",
                tags = mockTags,
                untaggedOnly = false,
                publicPostsOnly = false,
                privatePostsOnly = false,
                readLaterOnly = false,
                countLimit = DEFAULT_RECENT_QUANTITY,
                pageLimit = DEFAULT_RECENT_QUANTITY,
                pageOffset = 0
            )
        }
    }
}
