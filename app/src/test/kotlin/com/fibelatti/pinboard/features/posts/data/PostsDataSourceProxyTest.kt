package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.linkding.data.PostsDataSourceLinkdingApi
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.randomInt
import com.fibelatti.pinboard.randomString
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PostsDataSourceProxyTest {

    private val postsDataSourcePinboardApi = mockk<PostsDataSourcePinboardApi>()
    private val postsDataSourceLinkdingApi = mockk<PostsDataSourceLinkdingApi>()
    private val postsDataSourceNoApi = mockk<PostsDataSourceNoApi>()

    private val booleanArg = randomBoolean()
    private val intArg = randomInt()
    private val stringArg = randomString()

    fun testCases(): List<Pair<AppMode, PostsRepository>> = listOf(
        AppMode.NO_API to postsDataSourceNoApi,
        AppMode.PINBOARD to postsDataSourcePinboardApi,
        AppMode.LINKDING to postsDataSourceLinkdingApi,
    )

    private fun getProxy(appMode: AppMode): PostsRepository = PostsDataSourceProxy(
        postsDataSourcePinboardApi = { postsDataSourcePinboardApi },
        postsDataSourceLinkdingApi = { postsDataSourceLinkdingApi },
        postsDataSourceNoApi = { postsDataSourceNoApi },
        appModeProvider = mockk {
            every { this@mockk.appMode } returns MutableStateFlow(appMode)
        },
    )

    private fun verifyAllSources() {
        confirmVerified(postsDataSourceNoApi, postsDataSourcePinboardApi, postsDataSourceLinkdingApi)
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `update calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<String>>()

        coEvery { repository.update() } returns expectedResult

        val result = proxy.update()

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.update() }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `add calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val input = mockk<Post>()
        val expectedResult = mockk<Result<Post>>()

        coEvery { repository.add(input) } returns expectedResult

        val result = proxy.add(input)

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.add(input) }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `delete calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<Unit>>()
        val post = mockk<Post>()

        coEvery { repository.delete(post) } returns expectedResult

        val result = proxy.delete(post)

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.delete(post) }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `getAllPosts calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<PostListResult>>()

        every {
            repository.getAllPosts(
                sortType = any(),
                searchTerm = stringArg,
                tags = any(),
                matchAll = booleanArg,
                exactMatch = booleanArg,
                untaggedOnly = booleanArg,
                postVisibility = any(),
                readLaterOnly = booleanArg,
                countLimit = intArg,
                pageLimit = intArg,
                pageOffset = intArg,
                forceRefresh = booleanArg,
            )
        } returns flowOf(expectedResult)

        val result = proxy.getAllPosts(
            sortType = mockk(),
            searchTerm = stringArg,
            tags = mockk(),
            matchAll = booleanArg,
            exactMatch = booleanArg,
            untaggedOnly = booleanArg,
            postVisibility = mockk(),
            readLaterOnly = booleanArg,
            countLimit = intArg,
            pageLimit = intArg,
            pageOffset = intArg,
            forceRefresh = booleanArg,
        )

        assertThat(result.first()).isEqualTo(expectedResult)

        coVerify {
            @Suppress("UnusedFlow")
            repository.getAllPosts(
                sortType = any(),
                searchTerm = stringArg,
                tags = any(),
                matchAll = booleanArg,
                exactMatch = booleanArg,
                untaggedOnly = booleanArg,
                postVisibility = any(),
                readLaterOnly = booleanArg,
                countLimit = intArg,
                pageLimit = intArg,
                pageOffset = intArg,
                forceRefresh = booleanArg,
            )
        }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `getQueryResultSize calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = 13

        coEvery {
            repository.getQueryResultSize(
                searchTerm = stringArg,
                tags = any(),
                matchAll = booleanArg,
                exactMatch = booleanArg,
            )
        } returns expectedResult

        val result = proxy.getQueryResultSize(
            searchTerm = stringArg,
            tags = mockk(),
            matchAll = booleanArg,
            exactMatch = booleanArg,
        )

        assertThat(result).isEqualTo(expectedResult)

        coVerify {
            repository.getQueryResultSize(
                searchTerm = stringArg,
                tags = any(),
                matchAll = booleanArg,
                exactMatch = booleanArg,
            )
        }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `getPost calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<Post>>()

        coEvery {
            repository.getPost(
                id = stringArg,
                url = stringArg,
            )
        } returns expectedResult

        val result = proxy.getPost(
            id = stringArg,
            url = stringArg,
        )

        assertThat(result).isEqualTo(expectedResult)

        coVerify {
            repository.getPost(
                id = stringArg,
                url = stringArg,
            )
        }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `searchExistingPostTag calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<List<String>>>()

        coEvery { repository.searchExistingPostTag(tag = stringArg) } returns expectedResult

        val result = proxy.searchExistingPostTag(tag = stringArg)

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.searchExistingPostTag(tag = stringArg) }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `getPendingSyncPosts calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<List<Post>>>()

        coEvery { repository.getPendingSyncPosts() } returns expectedResult

        val result = proxy.getPendingSyncPosts()

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.getPendingSyncPosts() }
        verifyAllSources()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `clearCache calls the expected data source`(params: Pair<AppMode, PostsRepository>) = runTest {
        val (appMode, repository) = params
        val proxy = getProxy(appMode)
        val expectedResult = mockk<Result<Unit>>()

        coEvery { repository.clearCache() } returns expectedResult

        val result = proxy.clearCache()

        assertThat(result).isEqualTo(expectedResult)

        coVerify { repository.clearCache() }
        verifyAllSources()
    }
}
