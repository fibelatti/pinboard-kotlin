package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.OldestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull

class GetAllPostsTest {

    // region Mock data
    private val mockResponse = mock<Pair<Int, List<Post>>>()

    private val mockPostsRepository = mock<PostsRepository>()

    private val getAllPosts = GetAllPosts(mockPostsRepository)

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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SortingTest {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN sorting was set in the params WHEN getAllPosts is called THEN repository is called with the expected params`(sorting: SortType) {
            // GIVEN
            val params = GetPostParams(sorting = sorting)

            // WHEN
            runBlocking { getAllPosts(params) }

            // THEN
            verifySuspend(mockPostsRepository) {
                getAllPosts(
                    newestFirst = eq(sorting == NewestFirst),
                    searchTerm = anyString(),
                    tags = any(),
                    untaggedOnly = anyBoolean(),
                    publicPostsOnly = anyBoolean(),
                    privatePostsOnly = anyBoolean(),
                    readLaterOnly = anyBoolean(),
                    countLimit = eq(-1),
                    pageLimit = anyInt(),
                    pageOffset = anyInt()
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
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = true,
                searchTerm = mockUrlValid,
                tags = null,
                untaggedOnly = false,
                publicPostsOnly = false,
                privatePostsOnly = false,
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
        val params = GetPostParams(tagParams = GetPostParams.Tags.None)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = isNull(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Untagged WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tagParams = GetPostParams.Tags.Untagged)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = eq(true),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }
    }

    @Test
    fun `GIVEN tagParams was Tagged WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(tagParams = GetPostParams.Tags.Tagged(mockTags))

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = eq(mockTags),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was None WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibilityParams = GetPostParams.Visibility.None)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = eq(false),
                privatePostsOnly = eq(false),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was Public WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibilityParams = GetPostParams.Visibility.Public)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = eq(true),
                privatePostsOnly = eq(false),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
            )
        }
    }

    @Test
    fun `GIVEN visibilityParams was Private WHEN getAllPosts is called THEN repository is called with the expected params`() {
        // GIVEN
        val params = GetPostParams(visibilityParams = GetPostParams.Visibility.Private)

        // WHEN
        runBlocking { getAllPosts(params) }

        // THEN
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = eq(false),
                privatePostsOnly = eq(true),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
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
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = eq(true),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
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
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = eq(false),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = anyInt()
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
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = eq(100),
                pageOffset = anyInt()
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
        verifySuspend(mockPostsRepository) {
            getAllPosts(
                newestFirst = anyBoolean(),
                searchTerm = anyString(),
                tags = any(),
                untaggedOnly = anyBoolean(),
                publicPostsOnly = anyBoolean(),
                privatePostsOnly = anyBoolean(),
                readLaterOnly = anyBoolean(),
                countLimit = eq(-1),
                pageLimit = anyInt(),
                pageOffset = eq(100)
            )
        }
    }
}
