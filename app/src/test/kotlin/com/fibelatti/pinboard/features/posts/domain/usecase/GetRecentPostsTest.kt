package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.Sorting
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Test

class GetRecentPostsTest {

    private val mockPostsRepository = mock<PostsRepository>()

    // region Mock data
    private val mockTag1 = "tag1"
    private val mockTag2 = "tag2"
    private val mockTag3 = "tag3"
    private val mockTag4 = "tag4"

    private val mockTime1 = "2019-01-10T08:20:10Z"
    private val mockTime2 = "2019-01-11T08:20:10Z"
    private val mockTime3 = "2019-01-12T08:20:10Z"
    private val mockTime4 = "2019-01-13T08:20:10Z"

    private val mockTags = listOf(mockTag1, mockTag2, mockTag3, mockTag4)
    private val mockTagsTrimmed = listOf(mockTag1, mockTag2, mockTag3)

    private val mockResponseFull = listOf(
        createPost(time = mockTime1, tags = listOf(mockTag1)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime4, tags = listOf(mockTag4))
    )
    private val mockResponseExpectedOldestFirst = listOf(
        createPost(time = mockTime1, tags = listOf(mockTag1)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime3, tags = listOf(mockTag3))
    )
    private val mockResponseExpectedNewestFirst = listOf(
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime1, tags = listOf(mockTag1))
    )
    // endregion

    private val getRecentPosts = GetRecentPosts(mockPostsRepository)

    @Test
    fun `GIVEN more than API_FILTER_MAX_TAGS is sent as a parameter WHEN GetRecentPosts is called THEN only the first API_FILTER_MAX_TAGS are used`() {
        // GIVEN
        val params = GetRecentPosts.Params(tags = mockTags)
        givenSuspend { mockPostsRepository.getRecentPosts(mockTagsTrimmed) }
            .willReturn(Success(mockResponseFull))

        // WHEN
        val result = callSuspend { getRecentPosts(params) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockResponseExpectedNewestFirst
    }

    @Test
    fun `GIVEN sorting is NEWEST_FIRST WHEN GetRecentPosts is called THEN the response is sorted by descending time`() {
        // GIVEN
        val params = GetRecentPosts.Params(tags = mockTags, sorting = Sorting.NEWEST_FIRST)
        givenSuspend { mockPostsRepository.getRecentPosts(mockTagsTrimmed) }
            .willReturn(Success(mockResponseFull))

        // WHEN
        val result = callSuspend { getRecentPosts(params) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockResponseExpectedNewestFirst
    }

    @Test
    fun `GIVEN sorting is OLDEST_FIRST WHEN GetRecentPosts is called THEN the response is sorted by time`() {
        // GIVEN
        val params = GetRecentPosts.Params(tags = mockTags, sorting = Sorting.OLDEST_FIRST)
        givenSuspend { mockPostsRepository.getRecentPosts(mockTagsTrimmed) }
            .willReturn(Success(mockResponseFull))

        // WHEN
        val result = callSuspend { getRecentPosts(params) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockResponseExpectedOldestFirst
    }

    @Test
    fun `GIVEN repository fails WHEN GetRecentPosts is called THEN Failure is returned`() {
        // GIVEN
        val params = GetRecentPosts.Params()
        givenSuspend { mockPostsRepository.getRecentPosts(params.tags) }
            .willReturn(Failure(Exception()))

        // WHEN
        val result = callSuspend { getRecentPosts(params) }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
    }
}
