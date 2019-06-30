package com.fibelatti.pinboard.features.posts.data

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
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
import com.fibelatti.pinboard.MockDataProvider.createRecentDto
import com.fibelatti.pinboard.MockDataProvider.mockFutureTime
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsRequest
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.TestRateLimitRunner
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.user.domain.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify

class PostsDataSourceTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockApi = mock<PostsApi>()
    private val mockDao = mock<PostsDao>()
    private val mockPostDtoMapper = mock<PostDtoMapper>()
    private val mockSuggestedTagsDtoMapper = mock<SuggestedTagDtoMapper>()
    private val mockDateFormatter = mock<DateFormatter>()
    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockRunner = TestRateLimitRunner()

    private val mockListPostDto = listOf(mock<PostDto>())
    private val mockListPost = listOf(mock<Post>())
    private val mockSuggestedTagsDto = mock<SuggestedTagsDto>()
    private val mockSuggestedTags = mock<SuggestedTags>()

    private val dataSource: PostsRepository = PostsDataSource(
        mockUserRepository,
        mockApi,
        mockDao,
        mockPostDtoMapper,
        mockSuggestedTagsDtoMapper,
        mockDateFormatter,
        mockConnectivityManager,
        mockRunner
    )

    @Nested
    inner class UpdateTests {
        @Test
        fun `GIVEN that the api returns an error WHEN update is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.update() }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.update() }

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
            val result = callSuspend { dataSource.update() }

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
            givenSuspend { mockApi.add(mockUrlValid, mockUrlTitle, tags = mockTagsRequest) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlTitle, tags = mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.add(mockUrlValid, mockUrlTitle, tags = mockTagsRequest) }
                .willReturn(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlTitle, tags = mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.add(mockUrlValid, mockUrlTitle, tags = mockTagsRequest) }
                .willReturn(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlTitle, tags = mockTags) }

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
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsRequest
                )
            }.willReturn(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend {
                dataSource.add(
                    mockUrlValid,
                    mockUrlTitle,
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
            val result = callSuspend { dataSource.delete(mockUrlValid) }

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
            val result = callSuspend { dataSource.delete(mockUrlValid) }

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
            val result = callSuspend { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }
    }

    @Nested
    inner class GetRecentPostsTests {
        @Test
        fun `GIVEN that the api returns an error WHEN getRecentPosts is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getRecentPosts(mockTagsRequest) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.getRecentPosts(mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getRecentPosts is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.getRecentPosts(mockTagsRequest) }
                .willReturn(createRecentDto(mockListPostDto))
            given(mockPostDtoMapper.mapList(mockListPostDto))
                .willReturn(mockListPost)

            // WHEN
            val result = callSuspend { dataSource.getRecentPosts(mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Success<List<Post>>>()
            result.getOrNull() shouldBe mockListPost
        }
    }

    @Nested
    inner class GetAllPostsTests {

        private val mockActiveNetworkInfo = mock<NetworkInfo>()

        @Nested
        inner class API {

            @BeforeEach
            fun setup() {
                given(mockConnectivityManager.activeNetworkInfo)
                    .willReturn(mockActiveNetworkInfo)
                given(mockActiveNetworkInfo.isConnected)
                    .willReturn(true)
            }

            @Test
            fun `GIVEN that the api returns an error WHEN getAllPosts is called THEN Failure is returned`() {
                // GIVEN
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(mockTagsRequest) }
                    .willAnswer { throw ApiException() }

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()

                verifySuspend(mockApi) { getAllPosts(mockTagsRequest) }
                verify(mockDao, never()).getAllPosts()
            }

            @Test
            fun `WHEN getAllPosts is called THEN Success is returned`() {
                // GIVEN
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockFutureTime))
                givenSuspend { mockApi.getAllPosts(mockTagsRequest) }
                    .willReturn(mockListPostDto)
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost

                verifySuspend(mockApi) { getAllPosts(mockTagsRequest) }
                verify(mockDao, never()).getAllPosts()
            }
        }

        @Nested
        inner class Database {
            @Test
            fun `GIVEN isConnected is false and local data is available WHEN getAllPosts is called THEN localData is returned`() {
                // GIVEN
                given(mockConnectivityManager.activeNetworkInfo)
                    .willReturn(mockActiveNetworkInfo)
                given(mockActiveNetworkInfo.isConnected)
                    .willReturn(false)
                given(mockDao.getPostCount())
                    .willReturn(1)
                given(mockDao.getAllPosts())
                    .willReturn(mockListPostDto)
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost

                verifySuspend(mockApi, never()) { update() }
                verifySuspend(mockApi, never()) { getAllPosts(safeAny()) }
            }

            @Test
            fun `GIVEN lastUpdate matches and localPosts is empty WHEN getAllPosts is called THEN api response is returned`() {
                // GIVEN
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockTime))
                given(mockDao.getPostCount())
                    .willReturn(0)
                givenSuspend { mockApi.getAllPosts(mockTagsRequest) }
                    .willAnswer { throw Exception() }

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()

                verifySuspend(mockApi) { update() }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
                verify(mockDao, never()).getAllPosts()
                verify(mockDao, never()).savePosts(safeAny())
            }

            @Test
            fun `GIVEN lastUpdate matches and localPosts is empty WHEN getAllPosts is called and it fails to save the result THEN error is returned`() {
                // GIVEN
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockTime))
                given(mockDao.getPostCount())
                    .willReturn(0)
                givenSuspend { mockApi.getAllPosts(mockTagsRequest) }
                    .willReturn(mockListPostDto)
                given(mockDao.savePosts(mockListPostDto))
                    .willAnswer { throw Exception() }

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()

                verifySuspend(mockApi) { update() }
                verifySuspend(mockUserRepository, never()) { setLastUpdate(anyString()) }
                verify(mockDao, never()).getAllPosts()
                verify(mockDao).deleteAllPosts()
                verify(mockDao).savePosts(mockListPostDto)
            }

            @Test
            fun `GIVEN lastUpdate matches and localPosts is not empty WHEN getAllPosts is called THEN local posts are returned`() {
                // GIVEN
                given(mockConnectivityManager.activeNetworkInfo)
                    .willReturn(mockActiveNetworkInfo)
                given(mockActiveNetworkInfo.isConnected)
                    .willReturn(true)
                givenSuspend { mockUserRepository.getLastUpdate() }
                    .willReturn(mockTime)
                givenSuspend { mockApi.update() }
                    .willReturn(UpdateDto(mockTime))
                given(mockDao.getPostCount())
                    .willReturn(1)
                given(mockDao.getAllPosts())
                    .willReturn(mockListPostDto)
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost

                verifySuspend(mockApi) { update() }
                verifySuspend(mockApi, never()) { getAllPosts(safeAny()) }
            }
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
            val result = callSuspend { dataSource.getPost(mockUrlValid) }

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
            val result = callSuspend { dataSource.getPost(mockUrlValid) }

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
            val result = callSuspend { dataSource.getPost(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Post>>()
            result.getOrNull() shouldBe post
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
            val result = callSuspend { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

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
            val result = callSuspend { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Success<SuggestedTags>>()
            result.getOrNull() shouldBe mockSuggestedTags
        }
    }
}
