package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.pinboard.MockDataProvider.createGenericResponse
import com.fibelatti.pinboard.MockDataProvider.createGetPostDto
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.MockDataProvider.mockFutureTime
import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTag3
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTagString3
import com.fibelatti.pinboard.MockDataProvider.mockTagStringHtml
import com.fibelatti.pinboard.MockDataProvider.mockTagStringHtmlEscaped
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsRequest
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.TestRateLimitRunner
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.extension.HTML_CHAR_MAP
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@ExperimentalCoroutinesApi
class PostsDataSourceTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockApi = mockk<PostsApi>()
    private val mockDao = mockk<PostsDao>(relaxUnitFun = true)
    private val mockPostDtoMapper = mockk<PostDtoMapper>()
    private val mockSuggestedTagsDtoMapper = mockk<SuggestedTagDtoMapper>()
    private val mockDateFormatter = mockk<DateFormatter>(relaxed = true)
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()
    private val mockRunner = TestRateLimitRunner()
    private val mockIoScope = TestCoroutineScope(TestCoroutineDispatcher())

    private val mockPostDto = mockk<PostDto>()
    private val mockListPostDto = listOf(mockPostDto)
    private val mockPost = mockk<Post>()
    private val mockListPost = listOf(mockPost)
    private val mockSuggestedTagsDto = mockk<SuggestedTagsDto>()
    private val mockSuggestedTags = mockk<SuggestedTags>()

    private val dataSource = spyk(
        PostsDataSource(
            mockUserRepository,
            mockApi,
            mockDao,
            mockPostDtoMapper,
            mockSuggestedTagsDtoMapper,
            mockDateFormatter,
            mockConnectivityInfoProvider,
            mockRunner,
            mockIoScope
        )
    )

    @Nested
    inner class UpdateTests {

        @Test
        fun `GIVEN that the api returns an error WHEN update is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.update() } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.update() }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN update is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.update() } returns UpdateDto(mockTime)

            // WHEN
            val result = runBlocking { dataSource.update() }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(mockTime)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AddTests {

        @BeforeEach
        fun setup() {
            clearMocks(mockApi, mockUserRepository)
        }

        @Test
        fun `GIVEN that the api returns an error WHEN add is called THEN Failure is returned`() {
            // GIVEN
            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest,
                    replace = AppConfig.PinboardApiLiterals.YES
                )
            } throws Exception()

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags,
                    replace = true
                )
            }

            // THEN
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() {
            // GIVEN
            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest,
                    replace = AppConfig.PinboardApiLiterals.YES
                )
            } returns createGenericResponse(ApiResultCodes.MISSING_URL)

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags,
                    replace = true
                )
            }

            // THEN
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class ItemAlreadyExists {

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS WHEN add is called THEN the db result is returned`() {
                // GIVEN
                coEvery {
                    mockApi.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        public = null,
                        readLater = null,
                        tags = mockTagsRequest,
                        replace = AppConfig.PinboardApiLiterals.YES
                    )
                } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                every { mockDao.getPost(mockUrlValid) } returns mockPostDto
                every { mockPostDtoMapper.map(mockPostDto) } returns mockPost

                // WHEN
                val result = runBlocking {
                    dataSource.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        private = null,
                        readLater = null,
                        tags = mockTags,
                        replace = true
                    )
                }

                // THEN
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                assertThat(result.getOrNull()).isEqualTo(mockPost)
            }

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS AND db has no data WHEN add is called THEN the api result is returned`() {
                // GIVEN
                coEvery {
                    mockApi.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        public = null,
                        readLater = null,
                        tags = mockTagsRequest,
                        replace = AppConfig.PinboardApiLiterals.YES
                    )
                } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                every { mockDao.getPost(mockUrlValid) } returns null
                coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = mockListPostDto)
                every { mockPostDtoMapper.map(mockPostDto) } returns mockPost

                // WHEN
                val result = runBlocking {
                    dataSource.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        private = null,
                        readLater = null,
                        tags = mockTags,
                        replace = true
                    )
                }

                // THEN
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                assertThat(result.getOrNull()).isEqualTo(mockPost)
            }

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS AND both gets fail WHEN add is called THEN failure returned`() {
                // GIVEN
                coEvery {
                    mockApi.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        public = null,
                        readLater = null,
                        tags = mockTagsRequest,
                        replace = AppConfig.PinboardApiLiterals.YES
                    )
                } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                every { mockDao.getPost(mockUrlValid) } returns null
                coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = emptyList())

                // WHEN
                val result = runBlocking {
                    dataSource.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        private = null,
                        readLater = null,
                        tags = mockTags,
                        replace = true
                    )
                }

                // THEN
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

                assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
            }
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest,
                    replace = AppConfig.PinboardApiLiterals.YES
                )
            } returns createGenericResponse(ApiResultCodes.DONE)
            coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = mockListPostDto)
            every { mockDao.savePosts(mockListPostDto) } just Runs
            every { mockPostDtoMapper.map(mockPostDto) } returns mockPost

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags,
                    replace = true
                )
            }

            // THEN
            verify { mockDao.savePosts(mockListPostDto) }
            assertThat(result.getOrNull()).isEqualTo(mockPost)
        }

        @Test
        fun `GIVEN the tags contain a plus sign THEN their are escaped before the request is sent`() {
            // GIVEN
            val expectedTags = "C%2b%2b+Tag%2bTag"
            val inputTags = listOf(Tag(name = "C++"), Tag(name="Tag+Tag"))

            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = expectedTags,
                    replace = AppConfig.PinboardApiLiterals.NO
                )
            } returns createGenericResponse(ApiResultCodes.DONE)

            // WHEN
            runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = inputTags,
                    replace = false
                )
            }

            // THEN
            coVerify {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = expectedTags,
                    replace = AppConfig.PinboardApiLiterals.NO
                )
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN the parameters THEN the expected api call parameters are sent`(testCases: Params) {
            // GIVEN
            val expectedPublic = when (testCases.private) {
                true -> AppConfig.PinboardApiLiterals.NO
                false -> AppConfig.PinboardApiLiterals.YES
                else -> null
            }
            val expectedReadLater = when (testCases.readLater) {
                true -> AppConfig.PinboardApiLiterals.YES
                false -> AppConfig.PinboardApiLiterals.NO
                else -> null
            }
            val expectedReplace = if (testCases.replace) {
                AppConfig.PinboardApiLiterals.YES
            } else {
                AppConfig.PinboardApiLiterals.NO
            }

            coEvery {
                mockApi.add(
                    mockUrlValid,
                    mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsRequest,
                    replace = expectedReplace
                )
            } returns createGenericResponse(ApiResultCodes.DONE)

            // WHEN
            runBlocking {
                dataSource.add(
                    mockUrlValid,
                    mockUrlTitle,
                    description = null,
                    private = testCases.private,
                    readLater = testCases.readLater,
                    tags = mockTags,
                    replace = testCases.replace
                )
            }

            // THEN
            coVerify {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsRequest,
                    replace = expectedReplace
                )
            }
        }

        fun testCases(): List<Params> = mutableListOf<Params>().apply {
            val values = listOf(true, false, null)

            values.forEach { private ->
                values.forEach { readLater ->
                    listOf(true, false).forEach { replace ->
                        add(Params(private, readLater, replace))
                    }
                }
            }
        }

        inner class Params(
            val private: Boolean?,
            val readLater: Boolean?,
            val replace: Boolean
        ) {

            override fun toString(): String =
                "Params(private=$private, readLater=$readLater, replace=$replace)"
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `GIVEN that the api returns an error WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.delete(mockUrlValid) } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            verify(exactly = 0) { mockDao.deletePost(mockUrlValid) }
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.delete(mockUrlValid) } returns createGenericResponse(ApiResultCodes.MISSING_URL)

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            verify(exactly = 0) { mockDao.deletePost(mockUrlValid) }
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

            assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN delete is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.delete(mockUrlValid) } returns createGenericResponse(ApiResultCodes.DONE)
            coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
            every { mockDao.deletePost(mockUrlValid) } just Runs

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            verify { mockDao.deletePost(mockUrlValid) }
            assertThat(result.getOrNull()).isEqualTo(Unit)
        }
    }

    @Nested
    inner class GetAllPostsTests {

        private val mockLocalData: PostListResult = mockk()

        @Nested
        inner class NoConnectionTest {

            @BeforeEach
            fun setup() {
                every { mockConnectivityInfoProvider.isConnected() } returns false
            }

            @Test
            fun `GIVEN isConnected is false WHEN getAllPosts is called THEN local data is returned`() {
                runBlocking {
                    // GIVEN
                    coEvery {
                        dataSource.getLocalData(
                            newestFirst = true,
                            searchTerm = "",
                            tags = null,
                            untaggedOnly = false,
                            postVisibility = PostVisibility.None,
                            readLaterOnly = false,
                            countLimit = -1,
                            pageLimit = -1,
                            pageOffset = 0,
                            upToDate = true
                        )
                    } returns Success(mockLocalData)

                    // WHEN
                    val result = dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                    // THEN
                    result.collectLatest {
                        assertThat(it.getOrThrow()).isEqualTo(mockLocalData)
                    }
                }
            }
        }

        @Nested
        inner class ConnectedTests {

            @BeforeEach
            fun setup() {
                every { mockConnectivityInfoProvider.isConnected() } returns true
                coEvery { mockUserRepository.lastUpdate } returns mockTime
                coEvery {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                } returns Success(mockLocalData)
                coEvery {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                } returns Success(mockLocalData)
            }

            @Test
            fun `WHEN update fails THEN nowAsTzFormat is used instead`() {
                // GIVEN
                coEvery { mockApi.update() } throws Exception()
                every { mockDateFormatter.nowAsTzFormat() } returns mockTime

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectLatest {
                        assertThat(it.getOrThrow()).isEqualTo(mockLocalData)
                    }
                }
                verify { mockDateFormatter.nowAsTzFormat() }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                }
                coVerify(exactly = 0) { mockApi.getAllPosts() }
                coVerify(exactly = 0) { mockDao.deleteAllPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN user last update is null THEN api should be called`() {
                // GIVEN
                coEvery { mockUserRepository.lastUpdate } returns ""
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE
                    )
                } returns mockListPostDto
                every { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            else -> fail("Unexpected number of collections")
                        }
                    }
                }

                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllPosts() }
                verify { dataSource.savePosts(mockListPostDto) }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                }
                coVerify { mockUserRepository.lastUpdate = mockFutureTime }
            }

            @Test
            fun `WHEN update time stamps match THEN local data is returned`() {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockTime)

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectLatest {
                        assertThat(it.getOrThrow()).isEqualTo(mockLocalData)
                    }
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                }
                coVerify(exactly = 0) { mockApi.getAllPosts() }
                coVerify(exactly = 0) { mockDao.deleteAllPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN force refresh is true THEN then api data is returned`() {
                // GIVEN
                val mockListPostDto = mockk<List<PostDto>>()
                coEvery { mockApi.update() } returns UpdateDto(mockTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE
                    )
                } returns mockListPostDto
                every { mockListPostDto.size } returns API_PAGE_SIZE - 1
                every { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = true,
                    )
                }

                // THEN
                runBlocking {
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            else -> fail("Unexpected number of collections")
                        }
                    }
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                }
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllPosts() }
                verify { dataSource.savePosts(mockListPostDto) }
                coVerify { mockUserRepository.lastUpdate = mockTime }

            }

            @Test
            fun `WHEN getAllPosts fails THEN Failure is returned`() {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE
                    )
                } throws Exception()

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.exceptionOrNull()).isInstanceOf(Exception::class.java)
                            else -> fail("Unexpected number of collections")
                        }
                    }
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                }
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify(exactly = 0) { mockDao.deleteAllPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN deleteAllPosts fails THEN Failure is returned`() {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE
                    )
                } returns mockListPostDto
                every { mockDao.deleteAllPosts() } throws Exception()

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.exceptionOrNull()).isInstanceOf(Exception::class.java)
                            else -> fail("Unexpected number of collections")
                        }
                    }
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                }
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllPosts() }
                verify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN savePosts fails THEN Failure is returned`() {
                runBlocking {
                    // GIVEN
                    coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                    coEvery {
                        mockApi.getAllPosts(
                            offset = 0,
                            limit = API_PAGE_SIZE
                        )
                    } returns mockListPostDto
                    every { dataSource.savePosts(any()) } throws Exception()

                    // WHEN
                    val result = dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )

                    // THEN
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.exceptionOrNull()).isInstanceOf(Exception::class.java)
                        }
                    }
                    coVerify {
                        dataSource.getLocalData(
                            newestFirst = true,
                            searchTerm = "",
                            tags = null,
                            untaggedOnly = false,
                            postVisibility = PostVisibility.None,
                            readLaterOnly = false,
                            countLimit = -1,
                            pageLimit = -1,
                            pageOffset = 0,
                            upToDate = false
                        )
                    }

                    coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    verify { mockDao.deleteAllPosts() }
                    verify { dataSource.savePosts(mockListPostDto) }
                    verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                }
            }

            @Test
            fun `WHEN first call result list has the same size as API_PAGE_SIZE THEN getAdditionalPages is called`() {
                // GIVEN
                val mockListPostDto = mockk<List<PostDto>>()
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE
                    )
                } returns mockListPostDto
                every { mockListPostDto.size } returns API_PAGE_SIZE
                every { dataSource.getAdditionalPages() } returns Unit
                every { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        forceRefresh = false,
                    )
                }

                // THEN
                runBlocking {
                    result.collectIndexed { index, value ->
                        when (index) {
                            0 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            1 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            2 -> assertThat(value.getOrNull()).isEqualTo(mockLocalData)
                            else -> fail("Unexpected number of collections")
                        }
                    }
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false
                    )
                }
                coVerify {
                    dataSource.getLocalData(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true
                    )
                }
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllPosts() }
                verify { dataSource.savePosts(mockListPostDto) }
                verify { dataSource.getAdditionalPages() }
                coVerify { mockUserRepository.lastUpdate = mockFutureTime }
            }
        }
    }

    @Nested
    inner class GetAdditionalPagesTest {

        @Test
        fun `getAdditionalPages should run at least once`() {
            // GIVEN
            val mockPosts = mockk<List<PostDto>>()

            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE,
                    limit = API_PAGE_SIZE
                )
            } returns mockPosts
            every { dataSource.savePosts(any()) } returns Unit

            // WHEN
            dataSource.getAdditionalPages()

            // THEN
            verify { dataSource.savePosts(mockPosts) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
        }

        @Test
        fun `getAdditionalPages should run again if the first time returned the same as the page size`() {
            // GIVEN
            val mockPosts = mockk<List<PostDto>>()
            every { mockPosts.size } returnsMany listOf(
                API_PAGE_SIZE,
                API_PAGE_SIZE,
                API_PAGE_SIZE - 1
            )

            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE,
                    limit = API_PAGE_SIZE
                )
            } returns mockPosts
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE * 2,
                    limit = API_PAGE_SIZE
                )
            } returns mockPosts
            every { dataSource.savePosts(any()) } returns Unit

            // WHEN
            dataSource.getAdditionalPages()

            // THEN
            verify(exactly = 2) { dataSource.savePosts(mockPosts) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE * 2, limit = API_PAGE_SIZE) }
        }
    }

    @Nested
    inner class SavePostsTests {

        @Test
        fun `WHEN tags contain escaped html characters THEN tags are replaced`() {
            val input = HTML_CHAR_MAP.keys.map { createPostDto(tags = it) }
                .toMutableList().apply { add(createPostDto()) }
            val expected = HTML_CHAR_MAP.values.map { createPostDto(tags = it) }
                .toMutableList().apply { add(createPostDto()) }
            every { mockDao.savePosts(expected) } just Runs

            dataSource.savePosts(input)

            coVerify { mockDao.savePosts(expected) }
        }
    }

    @Nested
    inner class GetLocalDataSizeTests {

        @Test
        fun `WHEN tags is empty THEN tag1 tag2 and tag3 are sent as empty`() {
            // GIVEN
            every {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any()
                )
            } returns 0

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = "",
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 1 THEN tag2 and tag3 are sent as empty`() {
            // GIVEN
            every {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any()
                )
            } returns 0

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 2 THEN tag3 is sent as empty`() {
            // GIVEN
            every {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any()
                )
            } returns 0

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 3 THEN all tags are sent`() {
            // GIVEN
            every {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any()
                )
            } returns 0

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2, mockTag3),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = PostsDao.preFormatTag(mockTag3.name),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1
                )
            }
            assertThat(result).isEqualTo(0)
        }
    }

    @Nested
    inner class GetLocalDataTests {

        private val upToDate = randomBoolean()
        private val mockLocalDataSize = 42

        @BeforeEach
        fun setup() {
            coEvery {
                dataSource.getLocalDataSize(
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = any()
                )
            } returns mockLocalDataSize

            every {
                mockDao.getAllPosts(
                    newestFirst = any(),
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                    offset = any()
                )
            } returns mockListPostDto

            every { mockPostDtoMapper.mapList(mockListPostDto) } returns mockListPost
        }

        @Test
        fun `WHEN local data size is 0 THEN PostListResult is returned and getAllPosts is not called`() {
            // GIVEN
            coEvery {
                dataSource.getLocalDataSize(
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = any()
                )
            } returns 0

            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = 0,
                    posts = emptyList(),
                    upToDate = upToDate
                )
            )
        }

        @Test
        fun `WHEN tags is empty THEN tag1 tag2 and tag3 are sent as empty`() {
            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            verify {
                mockDao.getAllPosts(
                    newestFirst = true,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = "",
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    mockLocalDataSize,
                    mockListPost,
                    upToDate
                )
            )
        }

        @Test
        fun `WHEN tags size is 1 THEN tag2 and tag3 are sent as empty`() {
            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            verify {
                mockDao.getAllPosts(
                    newestFirst = true,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    mockLocalDataSize,
                    mockListPost,
                    upToDate
                )
            )
        }

        @Test
        fun `WHEN tags size is 2 THEN tag3 is sent as empty`() {
            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            verify {
                mockDao.getAllPosts(
                    newestFirst = true,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    mockLocalDataSize,
                    mockListPost,
                    upToDate
                )
            )
        }

        @Test
        fun `WHEN tags size is 3 THEN all tags are sent`() {
            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2, mockTag3),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            verify {
                mockDao.getAllPosts(
                    newestFirst = true,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = PostsDao.preFormatTag(mockTag3.name),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    mockLocalDataSize,
                    mockListPost,
                    upToDate
                )
            )
        }

        @Test
        fun `WHEN getGetAll posts fails THEN Failure is returned`() {
            // GIVEN
            every {
                mockDao.getAllPosts(
                    newestFirst = any(),
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                    offset = any()
                )
            } throws Exception()

            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    postVisibility = PostVisibility.None,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0,
                    upToDate = upToDate
                )
            }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }
    }

    @Nested
    inner class GetPostTests {

        @Test
        fun `GIVEN that the api returns an error WHEN getPost is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.getPost(mockUrlValid) } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN the list is empty WHEN getPost is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = emptyList())

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN getPost is called THEN Success is returned`() {
            // GIVEN
            val post = createPost()

            every { mockDao.getPost(mockUrlValid) } returns null
            coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto()
            every { mockPostDtoMapper.map(createPostDto()) } returns post

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(post)
        }
    }

    @Nested
    inner class SearchExistingPostTagTest {

        @Test
        fun `WHEN the db call fails THEN Failure is returned`() {
            // GIVEN
            every { mockDao.searchExistingPostTag(any()) } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.searchExistingPostTag(mockTagString1) }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN the db call succeeds THEN distinct and sorted values are returned`() {
            // GIVEN
            every { mockDao.searchExistingPostTag(any()) } returns
                listOf(
                    mockTagString1,
                    "$mockTagString2 $mockTagString1",
                    "$mockTagString1 $mockTagString2 $mockTagString3",
                    mockTagStringHtmlEscaped
                )

            val commonPrefix = mockTagString1
                .commonPrefixWith(mockTagString2)
                .commonPrefixWith(mockTagString3)
                .commonPrefixWith(mockTagStringHtmlEscaped)

            // WHEN

            val result = runBlocking { dataSource.searchExistingPostTag(commonPrefix) }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(
                listOf(
                    mockTagString1,
                    mockTagString2,
                    mockTagString3,
                    mockTagStringHtml
                )
            )
        }
    }

    @Nested
    inner class GetSuggestedTagsForUrlTests {

        @Test
        fun `GIVEN that the api returns an error WHEN getSuggestedTagsForUrl is called THEN Failure is returned`() {
            // GIVEN
            coEvery { mockApi.getSuggestedTagsForUrl(mockUrlValid) } throws Exception()

            // WHEN
            val result = runBlocking { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN getSuggestedTagsForUrl is called THEN Success is returned`() {
            // GIVEN
            coEvery { mockApi.getSuggestedTagsForUrl(mockUrlValid) } returns mockSuggestedTagsDto
            every { mockSuggestedTagsDtoMapper.map(mockSuggestedTagsDto) } returns mockSuggestedTags

            // WHEN
            val result = runBlocking { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            assertThat(result.getOrNull()).isEqualTo(mockSuggestedTags)
        }
    }

    @Nested
    inner class ClearCache {

        @Test
        fun `WHEN clearCache is called THEN dao deleteAllPosts is called`() {
            // WHEN
            runBlocking { dataSource.clearCache() }

            // THEN
            verify { mockDao.deleteAllPosts() }
        }
    }
}
