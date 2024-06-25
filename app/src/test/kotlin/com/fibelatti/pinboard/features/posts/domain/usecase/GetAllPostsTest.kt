package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.bookmarking.MockDataProvider.MOCK_TAGS
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_VALID
import com.fibelatti.bookmarking.features.appstate.Alphabetical
import com.fibelatti.bookmarking.features.appstate.AlphabeticalReverse
import com.fibelatti.bookmarking.features.appstate.NewestFirst
import com.fibelatti.bookmarking.features.appstate.OldestFirst
import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.core.functional.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class GetAllPostsTest {

    private val mockResponse = mockk<PostListResult>()

    private val mockPostsRepository = mockk<PostsRepository>()

    private val getAllPosts = GetAllPosts(mockPostsRepository)

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
                forceRefresh = any(),
            )
        } returns flowOf(Success(mockResponse))
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SortingTest {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN sorting was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`(
            sorting: SortType,
        ) = runTest {
            // GIVEN
            val params = GetPostParams(sorting = sorting)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = sorting,
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

        fun testCases(): List<SortType> = listOf(NewestFirst, OldestFirst, Alphabetical, AlphabeticalReverse)
    }

    @Test
    fun `GIVEN search term was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(searchTerm = MOCK_URL_VALID)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = NewestFirst,
                    searchTerm = MOCK_URL_VALID,
                    tags = null,
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = params.limit,
                    pageOffset = params.offset,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was None WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.None)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = isNull(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was Untagged WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.Untagged)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = true,
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN tagParams was Tagged WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(tags = GetPostParams.Tags.Tagged(MOCK_TAGS))

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = MOCK_TAGS,
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN visibilityParams was None WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(visibility = PostVisibility.None)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = PostVisibility.None,
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN visibilityParams was Public WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(visibility = PostVisibility.Public)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = PostVisibility.Public,
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN visibilityParams was Private WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(visibility = PostVisibility.Private)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = PostVisibility.Private,
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN read later only was set as true in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(readLater = true)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = true,
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN read later only was set as false in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(readLater = false)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN limit was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(limit = 100)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = 100,
                    pageOffset = any(),
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN offset was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(offset = 100)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = 100,
                    forceRefresh = false,
                )
            }
        }

    @Test
    fun `GIVEN forceRefresh was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`() =
        runTest {
            // GIVEN
            val params = GetPostParams(forceRefresh = true)

            // WHEN
            getAllPosts(params)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = any(),
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = -1,
                    pageLimit = any(),
                    pageOffset = any(),
                    forceRefresh = true,
                )
            }
        }
}
