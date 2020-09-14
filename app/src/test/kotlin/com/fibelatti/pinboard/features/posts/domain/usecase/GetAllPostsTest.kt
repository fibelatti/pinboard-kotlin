package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.OldestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class GetAllPostsTest {

    // region Mock data
    private val mockResponse = mockk<PostListResult>()

    private val mockPostsRepository = mockk<PostsRepository>()

    private val getAllPosts = GetAllPosts(mockPostsRepository)

    @BeforeEach
    fun setup() {
        coEvery {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = any(),
                pageLimit = any(),
                pageOffset = any()
            )
        } returns flowOf(Success(mockResponse))
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SortingTest {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN sorting was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`(
            sorting: SortType
        ) {
            // GIVEN
            val params = GetPostParams(sorting = sorting)

            // WHEN
            runBlocking { getAllPosts(params) }

            // THEN
            coVerify {
                mockPostsRepository.getAllPosts(
                    newestFirst = sorting == NewestFirst,
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any()
                )
            }
        }

        fun testCases(): List<SortType> = listOf(NewestFirst, OldestFirst)
    }

    @Test
    fun `GIVEN search term was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(searchTerm = mockUrlValid)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = true,
                searchTerm = mockUrlValid,
                tags = null,
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = params.limit,
                pageOffset = params.offset
            )
        }
    }

    @Test
    fun `GIVEN tagParams was None WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tags = GetPostParams.Tags.None)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = isNull(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Untagged WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tags = GetPostParams.Tags.Untagged)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = true,
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Tagged WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tags = GetPostParams.Tags.Tagged(mockTags))

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = mockTags,
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was None WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibility = PostVisibility.None)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = PostVisibility.None,
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was Public WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibility = PostVisibility.Public)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = PostVisibility.Public,
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was Private WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibility = PostVisibility.Private)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = PostVisibility.Private,
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN read later only was set as true in the params WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(readLater = true)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = true,
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN read later only was set as false in the params WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(readLater = false)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = any(),
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN limit was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(limit = 100)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = 100,
                pageOffset = any()
            )
        }
    }

    @Test
    fun `GIVEN offset was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(offset = 100)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        coVerify {
            mockPostsRepository.getAllPosts(
                newestFirst = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = -1,
                pageLimit = any(),
                pageOffset = 100
            )
        }
    }
}
