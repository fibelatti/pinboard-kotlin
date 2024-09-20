package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRecentPostsTest {

    private val mockResponse = mockk<PostListResult>()

    private val mockPostsRepository = mockk<PostsRepository>(relaxed = true)

    private val getRecentPosts = GetRecentPosts(mockPostsRepository)

    @BeforeEach
    fun setup() {
        every {
            mockPostsRepository.getAllPosts(
                sortType = any(),
                searchTerm = any(),
                tags = any(),
                untaggedOnly = any(),
                postVisibility = any(),
                readLaterOnly = any(),
                countLimit = any(),
                pageLimit = any(),
                pageOffset = any(),
                forceRefresh = false,
            )
        } returns flowOf(Success(mockResponse))
    }

    @Test
    fun `GIVEN search term was set in the params WHEN getRecentPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(searchTerm = mockUrlValid)

            // WHEN
            getRecentPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
                    searchTerm = mockUrlValid,
                    tags = null,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = DEFAULT_RECENT_QUANTITY,
                    pageLimit = DEFAULT_RECENT_QUANTITY,
                    pageOffset = 0,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was None WHEN getRecentPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.None)

            // WHEN
            getRecentPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
                    searchTerm = "",
                    tags = null,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = DEFAULT_RECENT_QUANTITY,
                    pageLimit = DEFAULT_RECENT_QUANTITY,
                    pageOffset = 0,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was Untagged WHEN getRecentPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.Untagged)

            // WHEN
            getRecentPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
                    searchTerm = "",
                    tags = null,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = DEFAULT_RECENT_QUANTITY,
                    pageLimit = DEFAULT_RECENT_QUANTITY,
                    pageOffset = 0,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was Tagged WHEN getRecentPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.Tagged(mockTags))

            // WHEN
            getRecentPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
                    searchTerm = "",
                    tags = mockTags,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = DEFAULT_RECENT_QUANTITY,
                    pageLimit = DEFAULT_RECENT_QUANTITY,
                    pageOffset = 0,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN forceRefresh was set in the params WHEN getRecentPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(forceRefresh = true)

            // WHEN
            getRecentPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
                    searchTerm = "",
                    tags = null,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = DEFAULT_RECENT_QUANTITY,
                    pageLimit = DEFAULT_RECENT_QUANTITY,
                    pageOffset = 0,
                    forceRefresh = true,
                )
            }
        }
}
