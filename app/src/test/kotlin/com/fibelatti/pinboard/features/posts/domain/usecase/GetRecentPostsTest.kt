package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString

class GetRecentPostsTest {

    private val mockResponse = mock<Pair<Int, List<Post>>>()

    private val mockPostsRepository = mock<PostsRepository>()

    private val getRecentPosts = GetRecentPosts(mockPostsRepository)

    @BeforeEach
    fun setup() {
        givenSuspend {
            mockPostsRepository.getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = anyInt(),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }.willReturn(Success(mockResponse))
    }

    @Test
    fun `GIVEN search term was set in the params WHEN getRecentPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(searchTerm = mockUrlValid)

        // WHEN
        callSuspend { getRecentPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
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
        callSuspend { getRecentPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
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
        callSuspend { getRecentPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
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
        callSuspend { getRecentPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
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
