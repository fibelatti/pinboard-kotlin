package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.core.test.extension.verifySuspend
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
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsRequest
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.TestRateLimitRunner
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
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
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verifyNoMoreInteractions
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.coroutines.CoroutineContext

class PostsDataSourceTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockApi = mock<PostsApi>()
    private val mockDao = mock<PostsDao>()
    private val mockPostDtoMapper = mock<PostDtoMapper>()
    private val mockSuggestedTagsDtoMapper = mock<SuggestedTagDtoMapper>()
    private val mockDateFormatter = mock<DateFormatter>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()
    private val mockRunner = TestRateLimitRunner()
    private val mockIoScope = spy(CoroutineScope(Dispatchers.Unconfined))

    private val mockCoroutineContext = mock<CoroutineContext>()

    private val mockListPostDto = listOf(mock<PostDto>())
    private val mockListPost = listOf(mock<Post>())
    private val mockSuggestedTagsDto = mock<SuggestedTagsDto>()
    private val mockSuggestedTags = mock<SuggestedTags>()

    private val dataSource = spy(
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
            givenSuspend { mockApi.update() }
                .willAnswer { throw Exception() }

            // WHEN
            val result = runBlocking { dataSource.update() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN update is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.update() }
                .willReturn(UpdateDto(mockTime))

            // WHEN
            val result = runBlocking { dataSource.update() }

            // THEN
            result.shouldBeAnInstanceOf<Success<String>>()
            result.getOrNull() shouldBe mockTime
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AddTests {

        @BeforeEach
        fun setup() {
            reset(mockApi)
        }

        @Test
        fun `GIVEN that the api returns an error WHEN add is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest
                )
            }.willAnswer { throw Exception() }

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags
                )
            }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest
                )
            }.willReturn(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags
                )
            }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() {
            // GIVEN
            givenSuspend {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsRequest
                )
            }.willReturn(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    private = null,
                    readLater = null,
                    tags = mockTags
                )
            }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
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

            givenSuspend {
                mockApi.add(
                    mockUrlValid,
                    mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsRequest
                )
            }.willReturn(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = runBlocking {
                dataSource.add(
                    mockUrlValid,
                    mockUrlTitle,
                    description = null,
                    private = testCases.private,
                    readLater = testCases.readLater,
                    tags = mockTags
                )
            }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
            verifySuspend(mockApi) {
                add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsRequest
                )
            }
        }

        fun testCases(): List<Params> = mutableListOf<Params>().apply {
            val values = listOf(true, false, null)

            values.forEach { private ->
                values.forEach { readLater ->
                    add(Params(private, readLater))
                }
            }
        }

        inner class Params(
            val private: Boolean?,
            val readLater: Boolean?
        ) {
            override fun toString(): String = "Params(private=$private, readLater=$readLater)"
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `GIVEN that the api returns an error WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.delete(mockUrlValid) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.delete(mockUrlValid) }
                .willReturn(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN delete is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.delete(mockUrlValid) }
                .willReturn(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = runBlocking { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }
    }

    @Nested
    inner class GetAllPostsTests {

        private val mockLocalData = mock<Pair<Int, List<Post>>>()

        @Nested
        inner class NoConnectionTest {

            @BeforeEach
            fun setup() {
                given(mockConnectivityInfoProvider.isConnected())
                    .willReturn(false)
            }

            @Test
            fun `GIVEN isConnected is false WHEN getAllPosts is called THEN local data is returned`() {
                // GIVEN
                runBlocking {
                    willReturn(Success(mockLocalData)).given(dataSource)
                        .getLocalData(
                            newestFirst = true,
                            searchTerm = "",
                            tags = null,
                            untaggedOnly = false,
                            publicPostsOnly = false,
                            privatePostsOnly = false,
                            readLaterOnly = false,
                            countLimit = -1,
                            pageLimit = -1,
                            pageOffset = 0
                        )
                }

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
            }
        }

        @Nested
        inner class ConnectedTests {

            @BeforeEach
            fun setup() {
                given(mockIoScope.coroutineContext).willReturn(mockCoroutineContext)

                given(mockConnectivityInfoProvider.isConnected())
                    .willReturn(true)
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                runBlocking {
                    willReturn(Success(mockLocalData)).given(dataSource)
                        .getLocalData(
                            newestFirst = true,
                            searchTerm = "",
                            tags = null,
                            untaggedOnly = false,
                            publicPostsOnly = false,
                            privatePostsOnly = false,
                            readLaterOnly = false,
                            countLimit = -1,
                            pageLimit = -1,
                            pageOffset = 0
                        )
                }
            }

            @Test
            fun `WHEN update fails THEN nowAsTzFormat is used instead`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .will { throw Exception() }
                given(mockDateFormatter.nowAsTzFormat())
                    .willReturn(mockTime)

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
                verify(mockDateFormatter).nowAsTzFormat()
                verifySuspend(mockApi, never()) { getAllPosts() }
                verifySuspend(mockDao, never()) { deleteAllPosts() }
                verifySuspend(mockDao, never()) { savePosts(safeAny()) }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
            }

            @Test
            fun `WHEN user last update is null THEN api should be called`() {
                // GIVEN
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn("")
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .willReturn(mockListPostDto)

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao) { deleteAllPosts() }
                verifySuspend(mockDao) { savePosts(mockListPostDto) }
                verifySuspend(mockUserRepository) { setLastUpdate(mockFutureTime) }
            }

            @Test
            fun `WHEN update time stamps match THEN local data is returned`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockTime))

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
                verifySuspend(mockApi, never()) { getAllPosts() }
                verifySuspend(mockDao, never()) { deleteAllPosts() }
                verifySuspend(mockDao, never()) { savePosts(safeAny()) }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
            }

            @Test
            fun `WHEN getAllPosts fails THEN Failure is returned`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .will { throw Exception() }

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao, never()) { deleteAllPosts() }
                verifySuspend(mockDao, never()) { savePosts(safeAny()) }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
            }

            @Test
            fun `WHEN deleteAllPosts fails THEN Failure is returned`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .willReturn(mockListPostDto)
                given(mockDao.deleteAllPosts())
                    .will { throw Exception() }

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao) { deleteAllPosts() }
                verifySuspend(mockDao, never()) { savePosts(safeAny()) }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
            }

            @Test
            fun `WHEN savePosts fails THEN Failure is returned`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .willReturn(mockListPostDto)
                given(mockDao.savePosts(mockListPostDto))
                    .will { throw Exception() }

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao) { deleteAllPosts() }
                verifySuspend(mockDao) { savePosts(mockListPostDto) }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
            }

            @Test
            fun `WHEN all calls are successful THEN local data is returned`() {
                // GIVEN
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .willReturn(mockListPostDto)

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao) { deleteAllPosts() }
                verifySuspend(mockDao) { savePosts(mockListPostDto) }
                verifySuspend(mockUserRepository) { setLastUpdate(mockFutureTime) }
            }

            @Test
            fun `WHEN first call result list has the same size as API_PAGE_SIZE THEN getAdditionalPages is called`() {
                // GIVEN
                val mockListPostDto = mock<List<PostDto>>()
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                    .willReturn(mockListPostDto)
                given(mockListPostDto.size).willReturn(API_PAGE_SIZE)
                willDoNothing().given(dataSource).getAdditionalPages()

                // WHEN
                val result = runBlocking {
                    dataSource.getAllPosts(
                        newestFirst = true,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        publicPostsOnly = false,
                        privatePostsOnly = false,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0
                    )
                }

                // THEN
                result.getOrThrow() shouldBe mockLocalData
                verify(mockCoroutineContext).cancelChildren()
                verifySuspend(mockApi) { getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                verifySuspend(mockDao) { deleteAllPosts() }
                verifySuspend(mockDao) { savePosts(mockListPostDto) }
                verify(dataSource).getAdditionalPages()
                verifySuspend(mockUserRepository) { setLastUpdate(mockFutureTime) }
            }
        }
    }

    @Nested
    inner class GetAdditionalPagesTest {

        @Test
        fun `getAdditionalPages should run at least once`() {
            // GIVEN
            val mockPosts = mock<List<PostDto>>()

            givenSuspend { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
                .willReturn(mockPosts)

            // WHEN
            dataSource.getAdditionalPages()

            // THEN
            verify(mockDao).savePosts(mockPosts)
            verifySuspend(mockApi) { getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
            verifyNoMoreInteractions(mockApi)
        }

        @Test
        fun `getAdditionalPages should run again if the first time returned the same as the page size`() {
            // GIVEN
            val mockPosts = mock<List<PostDto>>()
            given(mockPosts.size).willReturn(API_PAGE_SIZE, API_PAGE_SIZE, API_PAGE_SIZE - 1)

            givenSuspend { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
                .willReturn(mockPosts)
            givenSuspend { mockApi.getAllPosts(offset = API_PAGE_SIZE * 2, limit = API_PAGE_SIZE) }
                .willReturn(mockPosts)

            // WHEN
            dataSource.getAdditionalPages()

            // THEN
            verify(mockDao, times(2)).savePosts(mockPosts)
            verifySuspend(mockApi) { getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
            verifySuspend(mockApi) { getAllPosts(offset = API_PAGE_SIZE * 2, limit = API_PAGE_SIZE) }
            verifyNoMoreInteractions(mockApi)
        }
    }

    @Nested
    inner class GetLocalDataSizeTests {

        @Test
        fun `WHEN tags is empty THEN tag1 tag2 and tag3 are sent as empty`() {
            // GIVEN
            given(
                mockDao.getPostCount(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt()
                )
            ).willReturn(0)

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify(mockDao).getPostCount(
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
            result shouldBe 0
        }

        @Test
        fun `WHEN tags size is 1 THEN tag2 and tag3 are sent as empty`() {
            // GIVEN
            given(
                mockDao.getPostCount(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt()
                )
            ).willReturn(0)

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify(mockDao).getPostCount(
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
            result shouldBe 0
        }

        @Test
        fun `WHEN tags size is 2 THEN tag3 is sent as empty`() {
            // GIVEN
            given(
                mockDao.getPostCount(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt()
                )
            ).willReturn(0)

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify(mockDao).getPostCount(
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
            result shouldBe 0
        }

        @Test
        fun `WHEN tags size is 3 THEN all tags are sent`() {
            // GIVEN
            given(
                mockDao.getPostCount(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt()
                )
            ).willReturn(0)

            // WHEN
            val result = runBlocking {
                dataSource.getLocalDataSize(
                    searchTerm = mockUrlTitle,
                    tags = listOf(mockTag1, mockTag2, mockTag3),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1
                )
            }

            // THEN
            verify(mockDao).getPostCount(
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
            result shouldBe 0
        }
    }

    @Nested
    inner class GetLocalDataTests {

        private val mockLocalDataSize = 42

        @BeforeEach
        fun setup() {
            runBlocking {
                willReturn(mockLocalDataSize).given(dataSource)
                    .getLocalDataSize(
                        searchTerm = anyString(),
                        tags = any(),
                        untaggedOnly = anyBoolean(),
                        publicPostsOnly = anyBoolean(),
                        privatePostsOnly = anyBoolean(),
                        readLaterOnly = anyBoolean(),
                        countLimit = anyInt()
                    )
            }

            given(
                mockDao.getAllPosts(
                    anyBoolean(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt(),
                    anyInt()
                )
            ).willReturn(mockListPostDto)

            given(mockPostDtoMapper.mapList(mockListPostDto))
                .willReturn(mockListPost)
        }

        @Test
        fun `WHEN local data size is 0 THEN null is returned and getAllPosts is not called`() {
            // GIVEN
            runBlocking {
                willReturn(0).given(dataSource)
                    .getLocalDataSize(
                        searchTerm = anyString(),
                        tags = any(),
                        untaggedOnly = anyBoolean(),
                        publicPostsOnly = anyBoolean(),
                        privatePostsOnly = anyBoolean(),
                        readLaterOnly = anyBoolean(),
                        countLimit = anyInt()
                    )
            }

            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            result.getOrThrow() shouldBe null
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
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            verify(mockDao).getAllPosts(
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
            result.getOrThrow() shouldBe Pair(mockLocalDataSize, mockListPost)
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
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            verify(mockDao).getAllPosts(
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
            result.getOrThrow() shouldBe Pair(mockLocalDataSize, mockListPost)
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
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            verify(mockDao).getAllPosts(
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
            result.getOrThrow() shouldBe Pair(mockLocalDataSize, mockListPost)
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
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            verify(mockDao).getAllPosts(
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
            result.getOrThrow() shouldBe Pair(mockLocalDataSize, mockListPost)
        }

        @Test
        fun `WHEN getGetAll posts fails THEN Failure is returned`() {
            // GIVEN
            given(
                mockDao.getAllPosts(
                    anyBoolean(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyBoolean(),
                    anyInt(),
                    anyInt()
                )
            ).will { throw Exception() }

            // WHEN
            val result = runBlocking {
                dataSource.getLocalData(
                    newestFirst = true,
                    searchTerm = mockUrlTitle,
                    tags = emptyList(),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    countLimit = -1,
                    pageLimit = -1,
                    pageOffset = 0
                )
            }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
        }
    }

    @Nested
    inner class GetPostTests {
        @Test
        fun `GIVEN that the api returns an error WHEN getPost is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getPost(mockUrlValid) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN the list is empty WHEN getPost is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getPost(mockUrlValid) }
                .willReturn(createGetPostDto(posts = emptyList()))

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
        }

        @Test
        fun `WHEN getPost is called THEN Success is returned`() {
            // GIVEN
            val post = createPost()

            givenSuspend { mockApi.getPost(mockUrlValid) }
                .willReturn(createGetPostDto())
            given(mockPostDtoMapper.map(createPostDto()))
                .willReturn(post)

            // WHEN
            val result = runBlocking { dataSource.getPost(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Post>>()
            result.getOrNull() shouldBe post
        }
    }

    @Nested
    inner class SearchExistingPostTagTest {

        @Test
        fun `WHEN the db call fails THEN Failure is returned`() {
            // GIVEN
            given(mockDao.searchExistingPostTag(anyString()))
                .will { throw Exception() }

            // WHEN

            val result = runBlocking { dataSource.searchExistingPostTag(mockTagString1) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
        }

        @Test
        fun `WHEN the db call succeeds THEN distinct and sorted values are returned`() {
            // GIVEN
            given(mockDao.searchExistingPostTag(anyString()))
                .willReturn(
                    listOf(
                        mockTagString1,
                        "$mockTagString2 $mockTagString1",
                        "$mockTagString1 $mockTagString2 $mockTagString3"
                    )
                )

            val commonPrefix = mockTagString1
                .commonPrefixWith(mockTagString2)
                .commonPrefixWith(mockTagString3)

            // WHEN

            val result = runBlocking { dataSource.searchExistingPostTag(commonPrefix) }

            // THEN
            result.getOrNull() shouldBe listOf(mockTagString1, mockTagString2, mockTagString3)
        }
    }

    @Nested
    inner class GetSuggestedTagsForUrlTests {

        @Test
        fun `GIVEN that the api returns an error WHEN getSuggestedTagsForUrl is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getSuggestedTagsForUrl(mockUrlValid) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = runBlocking { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getSuggestedTagsForUrl is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.getSuggestedTagsForUrl(mockUrlValid) }
                .willReturn(mockSuggestedTagsDto)
            given(mockSuggestedTagsDtoMapper.map(mockSuggestedTagsDto))
                .willReturn(mockSuggestedTags)

            // WHEN
            val result = runBlocking { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<SuggestedTags>>()
            result.getOrNull() shouldBe mockSuggestedTags
        }
    }

    @Nested
    inner class ClearCache {

        @Test
        fun `WHEN clearCache is called THEN dao deleteAllPosts is called`() {
            // WHEN
            runBlocking { dataSource.clearCache() }

            // THEN
            verify(mockDao).deleteAllPosts()
        }
    }
}
