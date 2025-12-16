package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabetical
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabeticalReverse
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@Suppress("UnusedFlow")
class GetRecentPostsTest {

    private val mockResponse = mockk<PostListResult>()

    private val mockPostsRepository = mockk<PostsRepository> {
        every {
            getAllPosts(
                sortType = any(),
                searchTerm = any(),
                tags = any(),
                matchAll = any(),
                exactMatch = any(),
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

    private val getRecentPosts = GetRecentPosts(
        postsRepository = mockPostsRepository,
    )

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SortingTest {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN sorting was set in the params WHEN getRecentPosts is called THEN repository is called with the expected params`(
            sorting: SortType,
        ) = runTest {
            // WHEN
            getRecentPosts(sorting)

            // THEN
            verify {
                mockPostsRepository.getAllPosts(
                    sortType = sorting,
                    searchTerm = "",
                    tags = null,
                    matchAll = true,
                    exactMatch = false,
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

        fun testCases(): List<SortType> = listOf(
            ByDateAddedNewestFirst,
            ByDateAddedOldestFirst,
            ByTitleAlphabetical,
            ByTitleAlphabeticalReverse,
        )
    }
}
