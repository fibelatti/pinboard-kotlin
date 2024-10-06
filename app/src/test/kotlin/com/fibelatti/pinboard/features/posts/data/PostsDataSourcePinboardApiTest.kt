package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.pinboard.MockDataProvider.createGenericResponse
import com.fibelatti.pinboard.MockDataProvider.createGetPostDto
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.MockDataProvider.createPostRemoteDto
import com.fibelatti.pinboard.MockDataProvider.mockFutureTime
import com.fibelatti.pinboard.MockDataProvider.mockHash
import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTag3
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTagString3
import com.fibelatti.pinboard.MockDataProvider.mockTagStringHtml
import com.fibelatti.pinboard.MockDataProvider.mockTagStringHtmlEscaped
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsResponse
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.extension.HTML_CHAR_MAP
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDto
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PostsDataSourcePinboardApiTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockApi = mockk<PostsApi>()
    private val mockDao = mockk<PostsDao>(relaxUnitFun = true)
    private val mockPostRemoteDtoMapper = mockk<PostRemoteDtoMapper>()
    private val mockPostDtoMapper = mockk<PostDtoMapper>()
    private val mockDateFormatter = mockk<DateFormatter>(relaxed = true)
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns true
    }

    private val mockPostDto = mockk<PostDto>()
    private val mockPostRemoteDto = mockk<PostRemoteDto>()
    private val mockListPostDto = listOf(mockPostDto)
    private val mockListPostRemoteDto = listOf(mockPostRemoteDto)
    private val mockPost = mockk<Post>()
    private val mockListPost = listOf(mockPost)

    private val dataSource = spyk(
        PostsDataSourcePinboardApi(
            userRepository = mockUserRepository,
            postsApi = mockApi,
            postsDao = mockDao,
            postDtoMapper = mockPostDtoMapper,
            postRemoteDtoMapper = mockPostRemoteDtoMapper,
            dateFormatter = mockDateFormatter,
            connectivityInfoProvider = mockConnectivityInfoProvider,
        ),
    )

    @Nested
    inner class UpdateTests {

        @Test
        fun `GIVEN that the api returns an error WHEN update is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.update() } throws Exception()

            // WHEN
            val result = dataSource.update()

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN update is called THEN Success is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.update() } returns UpdateDto(mockTime)

            // WHEN
            val result = dataSource.update()

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
        fun `GIVEN that the api returns an error WHEN add is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = mockTagsResponse,
                    replace = AppConfig.PinboardApiLiterals.YES,
                )
            } throws Exception()

            // WHEN
            val result = dataSource.add(
                Post(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = "",
                    private = null,
                    readLater = null,
                    tags = mockTags,
                    id = "",
                    dateAdded = "",
                ),
            )

            // THEN
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() =
            runTest {
                // GIVEN
                coEvery {
                    mockApi.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        public = null,
                        readLater = null,
                        tags = mockTagsResponse,
                        replace = AppConfig.PinboardApiLiterals.YES,
                    )
                } returns createGenericResponse(ApiResultCodes.MISSING_URL)

                // WHEN
                val result = dataSource.add(
                    Post(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = "",
                        private = null,
                        readLater = null,
                        tags = mockTags,
                        id = "",
                        dateAdded = "",
                    ),
                )

                // THEN
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
            }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class ItemAlreadyExists {

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS WHEN add is called THEN the db result is returned`() =
                runTest {
                    // GIVEN
                    coEvery {
                        mockApi.add(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = null,
                            public = null,
                            readLater = null,
                            tags = mockTagsResponse,
                            replace = AppConfig.PinboardApiLiterals.YES,
                        )
                    } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                    coEvery { mockDao.getPost(mockUrlValid) } returns mockPostDto
                    every { mockPostDtoMapper.map(mockPostDto) } returns mockPost

                    // WHEN
                    val result = dataSource.add(
                        Post(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = "",
                            private = null,
                            readLater = null,
                            tags = mockTags,
                            id = "",
                            dateAdded = "",
                        ),
                    )

                    // THEN
                    verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                    assertThat(result.getOrNull()).isEqualTo(mockPost)
                }

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS AND db has no data WHEN add is called THEN the api result is returned`() =
                runTest {
                    // GIVEN
                    coEvery {
                        mockApi.add(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = null,
                            public = null,
                            readLater = null,
                            tags = mockTagsResponse,
                            replace = AppConfig.PinboardApiLiterals.YES,
                        )
                    } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                    coEvery { mockDao.getPost(mockUrlValid) } returns null
                    coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = mockListPostRemoteDto)
                    every { mockPostRemoteDtoMapper.map(mockPostRemoteDto) } returns mockPostDto
                    every { mockPostDtoMapper.map(mockPostDto) } returns mockPost

                    // WHEN
                    val result = dataSource.add(
                        Post(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = "",
                            private = null,
                            readLater = null,
                            tags = mockTags,
                            id = "",
                            dateAdded = "",
                        ),
                    )

                    // THEN
                    verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
                    assertThat(result.getOrNull()).isEqualTo(mockPost)
                }

            @Test
            fun `GIVEN that the api returns 200 AND the result code is ITEM_ALREADY_EXISTS AND both gets fail WHEN add is called THEN failure returned`() =
                runTest {
                    // GIVEN
                    coEvery {
                        mockApi.add(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = null,
                            public = null,
                            readLater = null,
                            tags = mockTagsResponse,
                            replace = AppConfig.PinboardApiLiterals.YES,
                        )
                    } returns createGenericResponse(ApiResultCodes.ITEM_ALREADY_EXISTS)
                    coEvery { mockDao.getPost(mockUrlValid) } returns null
                    coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = emptyList())
                    every { mockPostRemoteDtoMapper.mapList(emptyList()) } returns emptyList()

                    // WHEN
                    val result = dataSource.add(
                        Post(
                            url = mockUrlValid,
                            title = mockUrlTitle,
                            description = "",
                            private = null,
                            readLater = null,
                            tags = mockTags,
                            id = "",
                            dateAdded = "",
                        ),
                    )

                    // THEN
                    verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

                    assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
                }
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() =
            runTest {
                // GIVEN
                val input = Post(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = "",
                    id = "",
                    dateAdded = "",
                    private = null,
                    readLater = null,
                    tags = mockTags,
                )

                val expectedPost = input.copy(
                    id = mockHash,
                    dateAdded = mockTime,
                )

                mockkStatic(UUID::class)
                every { UUID.randomUUID() } returns mockk {
                    every { this@mockk.toString() } returns mockHash
                }
                every { mockDateFormatter.nowAsDataFormat() } returns mockTime
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.add(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = null,
                        public = null,
                        readLater = null,
                        tags = mockTagsResponse,
                        replace = AppConfig.PinboardApiLiterals.YES,
                    )
                } returns createGenericResponse(ApiResultCodes.DONE)
                every { mockPostDtoMapper.mapReverse(expectedPost) } returns mockPostDto
                coEvery { dataSource.savePosts(listOf(mockPostDto)) } returns Unit

                // WHEN
                val result = dataSource.add(input)

                // THEN
                coVerify { mockDao.deletePendingSyncPost(mockUrlValid) }
                coVerify { dataSource.savePosts(listOf(mockPostDto)) }
                assertThat(result.getOrNull()).isEqualTo(expectedPost)
            }

        @Test
        fun `GIVEN the tags contain a plus sign THEN their are preserved as the request is sent`() = runTest {
            // GIVEN
            val expectedTags = "C++ Tag+Tag"
            val inputTags = listOf(Tag(name = "C++"), Tag(name = "Tag+Tag"))

            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = expectedTags,
                    replace = AppConfig.PinboardApiLiterals.YES,
                )
            } returns createGenericResponse(ApiResultCodes.DONE)

            // WHEN
            dataSource.add(
                Post(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = "",
                    private = null,
                    readLater = null,
                    tags = inputTags,
                    id = "",
                    dateAdded = "",
                ),
            )

            // THEN
            coVerify {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = null,
                    readLater = null,
                    tags = expectedTags,
                    replace = AppConfig.PinboardApiLiterals.YES,
                )
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `GIVEN the parameters THEN the expected api call parameters are sent`(testCases: Params) = runTest {
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
            val expectedReplace = AppConfig.PinboardApiLiterals.YES

            coEvery {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsResponse,
                    replace = expectedReplace,
                )
            } returns createGenericResponse(ApiResultCodes.DONE)

            // WHEN
            dataSource.add(
                Post(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = "",
                    private = testCases.private,
                    readLater = testCases.readLater,
                    tags = mockTags,
                    id = "",
                    dateAdded = "",
                ),
            )

            // THEN
            coVerify {
                mockApi.add(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = null,
                    public = expectedPublic,
                    readLater = expectedReadLater,
                    tags = mockTagsResponse,
                    replace = expectedReplace,
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
            val readLater: Boolean?,
        ) {

            override fun toString(): String = "Params(private=$private, readLater=$readLater)"
        }

        @Nested
        inner class NoConnectionTests {

            private val private = randomBoolean()
            private val readLater = randomBoolean()

            private val baseExpectedPost = createPostDto(
                href = mockUrlValid,
                shared = if (private) AppConfig.PinboardApiLiterals.NO else AppConfig.PinboardApiLiterals.YES,
                toread = if (readLater) AppConfig.PinboardApiLiterals.YES else AppConfig.PinboardApiLiterals.NO,
            )

            @BeforeEach
            fun setup() {
                every { mockConnectivityInfoProvider.isConnected() } returns false
            }

            @Test
            fun `GIVEN post does not exist THEN a new one is created AND marked for adding`() = runTest {
                // GIVEN
                val expectedPost = baseExpectedPost.copy(pendingSync = PendingSyncDto.ADD)

                coEvery { mockDao.getPost(mockUrlValid) } returns null
                mockkStatic(UUID::class)
                every { UUID.randomUUID() } returns mockk {
                    every { this@mockk.toString() } returns mockHash
                }
                every { mockDateFormatter.nowAsDataFormat() } returns mockTime
                every { mockPostDtoMapper.map(expectedPost) } returns createPost()

                // WHEN
                val result = dataSource.add(
                    Post(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = private,
                        readLater = readLater,
                        tags = mockTags,
                        id = "",
                        dateAdded = "",
                    ),
                )

                // THEN
                coVerify { mockDao.savePosts(listOf(expectedPost)) }
                assertThat(result.getOrNull()).isEqualTo(createPost())
            }

            @Test
            fun `GIVEN post only exists locally THEN it is updated`() = runTest {
                // GIVEN
                val expectedPost = baseExpectedPost.copy(pendingSync = PendingSyncDto.ADD)

                coEvery { mockDao.getPost(mockUrlValid) } returns expectedPost
                every { mockPostDtoMapper.map(expectedPost) } returns createPost()

                // WHEN
                val result = dataSource.add(
                    Post(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = private,
                        readLater = readLater,
                        tags = mockTags,
                        id = "",
                        dateAdded = "",
                    ),
                )

                // THEN
                coVerify { mockDao.savePosts(listOf(expectedPost)) }
                assertThat(result.getOrNull()).isEqualTo(createPost())
            }

            @Test
            fun `GIVEN post already exists THEN it is marked for update`() = runTest {
                // GIVEN
                val expectedPost = baseExpectedPost.copy(pendingSync = PendingSyncDto.UPDATE)

                coEvery { mockDao.getPost(mockUrlValid) } returns baseExpectedPost.copy(pendingSync = null)
                every { mockPostDtoMapper.map(expectedPost) } returns createPost()

                // WHEN
                val result = dataSource.add(
                    Post(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = private,
                        readLater = readLater,
                        tags = mockTags,
                        id = "",
                        dateAdded = "",
                    ),
                )

                // THEN
                coVerify { mockDao.savePosts(listOf(expectedPost)) }
                assertThat(result.getOrNull()).isEqualTo(createPost())
            }

            @Test
            fun `GIVEN save fails THEN failure is returned`() = runTest {
                // GIVEN
                coEvery { mockDao.getPost(mockUrlValid) } returns baseExpectedPost.copy(pendingSync = null)
                coEvery { mockDao.savePosts(any()) } throws Exception()

                // WHEN
                val result = dataSource.add(
                    Post(
                        url = mockUrlValid,
                        title = mockUrlTitle,
                        description = mockUrlDescription,
                        private = private,
                        readLater = readLater,
                        tags = mockTags,
                        id = "",
                        dateAdded = "",
                    ),
                )

                // THEN
                assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `GIVEN that the api returns an error WHEN delete is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.delete(mockUrlValid) } throws Exception()

            // WHEN
            val result = dataSource.delete(id = "", url = mockUrlValid)

            // THEN
            coVerify(exactly = 0) { mockDao.deletePost(mockUrlValid) }
            verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN delete is called THEN Failure is returned`() =
            runTest {
                // GIVEN
                coEvery { mockApi.delete(mockUrlValid) } returns createGenericResponse(ApiResultCodes.MISSING_URL)

                // WHEN
                val result = dataSource.delete(id = "", url = mockUrlValid)

                // THEN
                coVerify(exactly = 0) { mockDao.deletePost(mockUrlValid) }
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }

                assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
            }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN delete is called THEN Success is returned`() =
            runTest {
                // GIVEN
                coEvery { mockApi.delete(mockUrlValid) } returns createGenericResponse(ApiResultCodes.DONE)
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery { mockDao.deletePost(mockUrlValid) } just Runs

                // WHEN
                val result = dataSource.delete(id = "", url = mockUrlValid)

                // THEN
                coVerify { mockDao.deletePost(mockUrlValid) }
                assertThat(result.getOrNull()).isEqualTo(Unit)
            }

        @Nested
        inner class NoConnectionTests {

            @BeforeEach
            fun setup() {
                every { mockConnectivityInfoProvider.isConnected() } returns false
            }

            @Test
            fun `GIVEN the post exists THEN it is updated with the delete flag`() = runTest {
                // GIVEN
                val post = createPostDto()
                coEvery { mockDao.getPost(mockUrlValid) } returns post

                // WHEN
                val result = dataSource.delete(id = "", url = mockUrlValid)

                // THEN
                coVerify { mockDao.savePosts(listOf(post.copy(pendingSync = PendingSyncDto.DELETE))) }
                verify { mockApi wasNot Called }
                assertThat(result.getOrNull()).isEqualTo(Unit)
            }

            @Test
            fun `GIVEN the post exists AND some operation fails THEN failure is returned`() = runTest {
                // GIVEN
                val post = createPostDto()
                coEvery { mockDao.getPost(mockUrlValid) } returns post
                coEvery { mockDao.savePosts(any()) } throws Exception()

                // WHEN
                val result = dataSource.delete(id = "", url = mockUrlValid)

                // THEN
                verify { mockApi wasNot Called }
                assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
            }

            @Test
            fun `GIVEN the post does not exist THEN failure is returned`() = runTest {
                // GIVEN
                coEvery { mockDao.getPost(mockUrlValid) } returns null

                // WHEN
                val result = dataSource.delete(id = "", url = mockUrlValid)

                // THEN
                verify { mockApi wasNot Called }
                assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            }
        }
    }

    @Nested
    inner class GetAllPostsTests {

        private val mockLocalData: PostListResult = mockk()
        private val mockUpToDateLocalData: PostListResult = mockk()

        @Nested
        inner class NoConnectionTest {

            @BeforeEach
            fun setup() {
                every { mockConnectivityInfoProvider.isConnected() } returns false
            }

            @Test
            fun `GIVEN isConnected is false WHEN getAllPosts is called THEN local data is returned`() = runTest {
                // GIVEN
                coEvery {
                    dataSource.getLocalData(
                        sortType = ByDateAddedNewestFirst,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true,
                    )
                } returns Success(mockLocalData)

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList()).hasSize(1)
                assertThat(result.last().getOrThrow()).isEqualTo(mockLocalData)
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
                        sortType = ByDateAddedNewestFirst,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = false,
                    )
                } returns Success(mockLocalData)
                coEvery {
                    dataSource.getLocalData(
                        sortType = ByDateAddedNewestFirst,
                        searchTerm = "",
                        tags = null,
                        untaggedOnly = false,
                        postVisibility = PostVisibility.None,
                        readLaterOnly = false,
                        countLimit = -1,
                        pageLimit = -1,
                        pageOffset = 0,
                        upToDate = true,
                    )
                } returns Success(mockUpToDateLocalData)
            }

            @Test
            fun `WHEN update fails THEN nowAsTzFormat is used instead`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } throws Exception()
                every { mockDateFormatter.nowAsDataFormat() } returns mockTime

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                verify { mockDateFormatter.nowAsDataFormat() }
                coVerify(exactly = 0) { mockApi.getAllPosts() }
                coVerify(exactly = 0) { mockDao.deleteAllSyncedPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN user last update is null THEN api should be called`() = runTest {
                // GIVEN
                coEvery { mockUserRepository.lastUpdate } returns ""
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } returns mockListPostRemoteDto
                every { mockPostRemoteDtoMapper.mapList(mockListPostRemoteDto) } returns mockListPostDto
                coEvery { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllSyncedPosts() }
                coVerify { dataSource.savePosts(mockListPostDto) }
                coVerify { mockUserRepository.lastUpdate = mockFutureTime }
            }

            @Test
            fun `WHEN update time stamps match THEN local data is returned`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockTime)

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify(exactly = 0) { mockApi.getAllPosts() }
                coVerify(exactly = 0) { mockDao.deleteAllSyncedPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN force refresh is true THEN then api data is returned`() = runTest {
                // GIVEN
                val mockListPostDto = mockk<List<PostDto>>()
                val mockListPostRemoteDto = mockk<List<PostRemoteDto>>()
                coEvery { mockApi.update() } returns UpdateDto(mockTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } returns mockListPostRemoteDto
                every { mockPostRemoteDtoMapper.mapList(mockListPostRemoteDto) } returns mockListPostDto
                every { mockListPostRemoteDto.size } returns API_PAGE_SIZE - 1
                coEvery { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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

                // THEN
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllSyncedPosts() }
                coVerify { dataSource.savePosts(mockListPostDto) }
                coVerify { mockUserRepository.lastUpdate = mockTime }
            }

            @Test
            fun `WHEN getAllPosts fails THEN the error is handled and data is returned`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } throws Exception()

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify(exactly = 0) { mockDao.deleteAllSyncedPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN deleteAllPosts fails THEN the error is handled and data is returned`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } returns mockListPostRemoteDto
                coEvery { mockDao.deleteAllSyncedPosts() } throws Exception()

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllSyncedPosts() }
                coVerify(exactly = 0) { mockDao.savePosts(any()) }
                coVerify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN savePosts fails THEN the error is handled and data is returned`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } returns mockListPostRemoteDto
                every { mockPostRemoteDtoMapper.mapList(mockListPostRemoteDto) } returns mockListPostDto
                coEvery { dataSource.savePosts(any()) } throws Exception()

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllSyncedPosts() }
                coVerify { dataSource.savePosts(mockListPostDto) }
                verify(exactly = 0) { mockUserRepository.lastUpdate = any() }
            }

            @Test
            fun `WHEN first call result is successful THEN getAdditionalPages is called`() = runTest {
                // GIVEN
                coEvery { mockApi.update() } returns UpdateDto(mockFutureTime)
                coEvery {
                    mockApi.getAllPosts(
                        offset = 0,
                        limit = API_PAGE_SIZE,
                    )
                } returns mockListPostRemoteDto
                every { mockPostRemoteDtoMapper.mapList(mockListPostRemoteDto) } returns mockListPostDto
                coEvery { dataSource.getAdditionalPages(initialOffset = 1) } returns Unit
                coEvery { dataSource.savePosts(any()) } returns Unit

                // WHEN
                val result = dataSource.getAllPosts(
                    sortType = ByDateAddedNewestFirst,
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
                assertThat(result.toList().map { it.getOrThrow() })
                    .isEqualTo(listOf(mockLocalData, mockUpToDateLocalData))
                coVerify { mockApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
                coVerify { mockDao.deleteAllSyncedPosts() }
                coVerify { dataSource.savePosts(mockListPostDto) }
                coVerify { dataSource.getAdditionalPages(initialOffset = 1) }
                coVerify { mockUserRepository.lastUpdate = mockFutureTime }
            }
        }
    }

    @Nested
    inner class GetAdditionalPagesTest {

        @Test
        fun `getAdditionalPages should run at least once when the initial offset is below the threshold`() = runTest {
            // GIVEN
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE,
                    limit = API_PAGE_SIZE,
                )
            } returns mockListPostRemoteDto
            every { mockPostRemoteDtoMapper.mapList(mockListPostRemoteDto) } returns mockListPostDto
            coEvery { dataSource.savePosts(any()) } returns Unit

            // WHEN
            dataSource.getAdditionalPages(initialOffset = API_PAGE_SIZE)

            // THEN
            coVerify { dataSource.savePosts(mockListPostDto) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
        }

        @Test
        fun `getAdditionalPages should not run when the initial offsite is above the threshold`() = runTest {
            // WHEN
            dataSource.getAdditionalPages(initialOffset = 8000)

            // THEN
            verify {
                mockDao wasNot Called
                mockApi wasNot Called
            }
        }

        @Test
        fun `getAdditionalPages should run again if the next page is above the threshold`() = runTest {
            // GIVEN
            val mockResponse = mockk<List<PostRemoteDto>> {
                every { size } returns API_PAGE_SIZE
            }
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE,
                    limit = API_PAGE_SIZE,
                )
            } returns mockResponse
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE * 2,
                    limit = API_PAGE_SIZE,
                )
            } returns emptyList()
            every { mockPostRemoteDtoMapper.mapList(mockResponse) } returns mockListPostDto
            coEvery { dataSource.savePosts(mockListPostDto) } returns Unit

            // WHEN
            dataSource.getAdditionalPages(initialOffset = API_PAGE_SIZE)

            // THEN
            coVerify(exactly = 1) { dataSource.savePosts(mockListPostDto) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE * 2, limit = API_PAGE_SIZE) }
            confirmVerified(mockApi)
        }

        @Test
        fun `getAdditionalPages should stop running if the next page is below the threshold`() = runTest {
            // GIVEN
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE,
                    limit = API_PAGE_SIZE,
                )
            } returns mockk {
                every { size } returns API_PAGE_SIZE
            }
            coEvery {
                mockApi.getAllPosts(
                    offset = API_PAGE_SIZE * 2,
                    limit = API_PAGE_SIZE,
                )
            } returns mockk {
                every { size } returns 8000
            }
            every { mockPostRemoteDtoMapper.mapList(any()) } returns mockListPostDto
            coEvery { dataSource.savePosts(any()) } returns Unit

            // WHEN
            dataSource.getAdditionalPages(initialOffset = API_PAGE_SIZE)

            // THEN
            coVerify(exactly = 2) { dataSource.savePosts(mockListPostDto) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE, limit = API_PAGE_SIZE) }
            coVerify { mockApi.getAllPosts(offset = API_PAGE_SIZE * 2, limit = API_PAGE_SIZE) }
            confirmVerified(mockApi)
        }
    }

    @Nested
    inner class SavePostsTests {

        @Test
        fun `WHEN href contains escaped escape html character THEN href is replaced`() = runTest {
            val input = listOf(createPostDto(href = "https://www.some-url.com?query=with%20space"))
            val expected = listOf(createPostDto(href = "https://www.some-url.com?query=with space"))
            coEvery { mockDao.savePosts(expected) } just Runs

            dataSource.savePosts(input)

            coVerify { mockDao.savePosts(expected) }
        }

        @Test
        fun `WHEN tags contain escaped html characters THEN tags are replaced`() = runTest {
            val input = HTML_CHAR_MAP.keys.map { createPostDto(tags = it) }
                .toMutableList().apply { add(createPostDto()) }
            val expected = HTML_CHAR_MAP.values.map { createPostDto(tags = it) }
                .toMutableList().apply { add(createPostDto()) }
            coEvery { mockDao.savePosts(expected) } just Runs

            dataSource.savePosts(input)

            coVerify { mockDao.savePosts(expected) }
        }
    }

    @Nested
    inner class GetQueryResultSize {

        @Test
        fun `WHEN getQueryResultSize is called then it returns the dao post count`() = runTest {
            coEvery {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    ignoreVisibility = true,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                )
            } returns 13

            val result = dataSource.getQueryResultSize(mockUrlTitle, listOf(mockTag1))

            assertThat(result).isEqualTo(13)
        }

        @Test
        fun `WHEN getQueryResultSize is called and the dao throws then it returns 0`() = runTest {
            coEvery {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                )
            } throws Exception()

            val result = dataSource.getQueryResultSize(mockUrlTitle, listOf(mockTag1))

            assertThat(result).isEqualTo(0)
        }
    }

    @Nested
    inner class GetLocalDataSizeTests {

        @Test
        fun `WHEN tags is empty THEN tag1 tag2 and tag3 are sent as empty`() = runTest {
            // GIVEN
            coEvery {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                )
            } returns 0

            // WHEN
            val result = dataSource.getLocalDataSize(
                searchTerm = mockUrlTitle,
                tags = emptyList(),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
            )

            // THEN
            coVerify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = "",
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 1 THEN tag2 and tag3 are sent as empty`() = runTest {
            // GIVEN
            coEvery {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                )
            } returns 0

            // WHEN
            val result = dataSource.getLocalDataSize(
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
            )

            // THEN
            coVerify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 2 THEN tag3 is sent as empty`() = runTest {
            // GIVEN
            coEvery {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                )
            } returns 0

            // WHEN
            val result = dataSource.getLocalDataSize(
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1, mockTag2),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
            )

            // THEN
            coVerify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                )
            }
            assertThat(result).isEqualTo(0)
        }

        @Test
        fun `WHEN tags size is 3 THEN all tags are sent`() = runTest {
            // GIVEN
            coEvery {
                mockDao.getPostCount(
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                )
            } returns 0

            // WHEN
            val result = dataSource.getLocalDataSize(
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1, mockTag2, mockTag3),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
            )

            // THEN
            coVerify {
                mockDao.getPostCount(
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = PostsDao.preFormatTag(mockTag3.name),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
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
                    countLimit = any(),
                )
            } returns mockLocalDataSize

            coEvery {
                mockDao.getAllPosts(
                    sortType = any(),
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                    offset = any(),
                )
            } returns mockListPostDto

            every { mockPostDtoMapper.mapList(mockListPostDto) } returns mockListPost
        }

        @Test
        fun `WHEN local data size is 0 THEN PostListResult is returned and getAllPosts is not called`() = runTest {
            // GIVEN
            coEvery {
                dataSource.getLocalDataSize(
                    searchTerm = any(),
                    tags = any(),
                    untaggedOnly = any(),
                    postVisibility = any(),
                    readLaterOnly = any(),
                    countLimit = any(),
                )
            } returns 0

            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = emptyList(),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = 0,
                    posts = emptyList(),
                    upToDate = upToDate,
                    canPaginate = false,
                ),
            )
        }

        @Test
        fun `WHEN tags is empty THEN tag1 tag2 and tag3 are sent as empty`() = runTest {
            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = emptyList(),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            coVerify {
                mockDao.getAllPosts(
                    sortType = ByDateAddedNewestFirst.index,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = "",
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0,
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = mockLocalDataSize,
                    posts = mockListPost,
                    upToDate = upToDate,
                    canPaginate = false,
                ),
            )
        }

        @Test
        fun `WHEN tags size is 1 THEN tag2 and tag3 are sent as empty`() = runTest {
            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            coVerify {
                mockDao.getAllPosts(
                    sortType = ByDateAddedNewestFirst.index,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = "",
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0,
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = mockLocalDataSize,
                    posts = mockListPost,
                    upToDate = upToDate,
                    canPaginate = false,
                ),
            )
        }

        @Test
        fun `WHEN tags size is 2 THEN tag3 is sent as empty`() = runTest {
            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1, mockTag2),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            coVerify {
                mockDao.getAllPosts(
                    sortType = ByDateAddedNewestFirst.index,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = "",
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0,
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = mockLocalDataSize,
                    posts = mockListPost,
                    upToDate = upToDate,
                    canPaginate = false,
                ),
            )
        }

        @Test
        fun `WHEN tags size is 3 THEN all tags are sent`() = runTest {
            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = listOf(mockTag1, mockTag2, mockTag3),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            coVerify {
                mockDao.getAllPosts(
                    sortType = ByDateAddedNewestFirst.index,
                    term = PostsDao.preFormatTerm(mockUrlTitle),
                    tag1 = PostsDao.preFormatTag(mockTag1.name),
                    tag2 = PostsDao.preFormatTag(mockTag2.name),
                    tag3 = PostsDao.preFormatTag(mockTag3.name),
                    untaggedOnly = false,
                    publicPostsOnly = false,
                    privatePostsOnly = false,
                    readLaterOnly = false,
                    limit = -1,
                    offset = 0,
                )
            }
            assertThat(result.getOrThrow()).isEqualTo(
                PostListResult(
                    totalCount = mockLocalDataSize,
                    posts = mockListPost,
                    upToDate = upToDate,
                    canPaginate = false,
                ),
            )
        }

        @Test
        fun `WHEN getGetAll posts fails THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery {
                mockDao.getAllPosts(
                    sortType = any(),
                    term = any(),
                    tag1 = any(),
                    tag2 = any(),
                    tag3 = any(),
                    untaggedOnly = any(),
                    publicPostsOnly = any(),
                    privatePostsOnly = any(),
                    readLaterOnly = any(),
                    limit = any(),
                    offset = any(),
                )
            } throws Exception()

            // WHEN
            val result = dataSource.getLocalData(
                sortType = ByDateAddedNewestFirst,
                searchTerm = mockUrlTitle,
                tags = emptyList(),
                untaggedOnly = false,
                postVisibility = PostVisibility.None,
                readLaterOnly = false,
                countLimit = -1,
                pageLimit = -1,
                pageOffset = 0,
                upToDate = upToDate,
            )

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }
    }

    @Nested
    inner class GetPostTests {

        @Test
        fun `GIVEN that the api returns an error WHEN getPost is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getPost(mockUrlValid) } throws Exception()

            // WHEN
            val result = dataSource.getPost(id = "", url = mockUrlValid)

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN the list is empty WHEN getPost is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto(posts = emptyList())

            // WHEN
            val result = dataSource.getPost(id = "", url = mockUrlValid)

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN getPost is called THEN Success is returned`() = runTest {
            // GIVEN
            val post = createPost()

            coEvery { mockDao.getPost(mockUrlValid) } returns null
            coEvery { mockApi.getPost(mockUrlValid) } returns createGetPostDto()
            every { mockPostRemoteDtoMapper.map(createPostRemoteDto()) } returns createPostDto()
            every { mockPostDtoMapper.map(createPostDto()) } returns post

            // WHEN
            val result = dataSource.getPost(id = "", url = mockUrlValid)

            // THEN
            assertThat(result.getOrNull()).isEqualTo(post)
        }
    }

    @Nested
    inner class SearchExistingPostTagTest {

        @Test
        fun `WHEN the db call fails THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockDao.searchExistingPostTag(any()) } throws Exception()

            // WHEN
            val result = dataSource.searchExistingPostTag(mockTagString1)

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `WHEN the db call succeeds THEN distinct and sorted values are returned`() = runTest {
            // GIVEN
            coEvery { mockDao.searchExistingPostTag(any()) } returns
                listOf(
                    mockTagString1,
                    "$mockTagString2 $mockTagString1",
                    "$mockTagString1 $mockTagString2 $mockTagString3",
                    mockTagStringHtmlEscaped,
                )

            val commonPrefix = mockTagString1
                .commonPrefixWith(mockTagString2)
                .commonPrefixWith(mockTagString3)
                .commonPrefixWith(mockTagStringHtmlEscaped)

            // WHEN
            val result = dataSource.searchExistingPostTag(commonPrefix)

            // THEN
            assertThat(result.getOrNull()).isEqualTo(
                listOf(
                    mockTagString1,
                    mockTagString2,
                    mockTagString3,
                    mockTagStringHtml,
                ),
            )
        }

        @Test
        fun `WHEN the db call succeeds THEN current tags are filtered out`() = runTest {
            // GIVEN
            coEvery { mockDao.searchExistingPostTag(any()) } returns
                listOf(
                    mockTagString1,
                    "$mockTagString2 $mockTagString1",
                    "$mockTagString1 $mockTagString2 $mockTagString3",
                    mockTagStringHtmlEscaped,
                )

            val commonPrefix = mockTagString1
                .commonPrefixWith(mockTagString2)
                .commonPrefixWith(mockTagString3)
                .commonPrefixWith(mockTagStringHtmlEscaped)

            // WHEN
            val result = dataSource.searchExistingPostTag(
                tag = commonPrefix,
                currentTags = listOf(Tag(name = mockTagString1)),
            )

            // THEN
            assertThat(result.getOrNull()).isEqualTo(
                listOf(
                    mockTagString2,
                    mockTagString3,
                    mockTagStringHtml,
                ),
            )
        }

        @Test
        fun `WHEN tag is empty THEN top db tags are returned`() = runTest {
            // GIVEN
            coEvery { mockDao.getAllPostTags() } returns listOf(
                mockTagString1,
                "$mockTagString2 $mockTagString1",
                "$mockTagString1 $mockTagString2 $mockTagString3",
                mockTagStringHtmlEscaped,
            )

            // WHEN
            val result = dataSource.searchExistingPostTag(
                tag = "",
                currentTags = listOf(Tag(name = mockTagString3)),
            )

            // THEN
            assertThat(result.getOrNull()).isEqualTo(
                listOf(
                    mockTagString1,
                    mockTagString2,
                    mockTagStringHtml,
                ),
            )
        }
    }

    @Nested
    inner class GetPendingSyncPostsTests {

        @Test
        fun `WHEN getPendingSyncPosts is called THEN the dao result is returned`() = runTest {
            // GIVEN
            coEvery { mockDao.getPendingSyncPosts() } returns mockk()
            every { mockPostDtoMapper.mapList(any()) } returns listOf(createPost())

            // WHEN
            val result = dataSource.getPendingSyncPosts()

            // THEN
            assertThat(result.getOrNull()).isEqualTo(listOf(createPost()))
        }

        @Test
        fun `WHEN getPendingSyncPosts is called AND the dao throws THEN failure is returned`() = runTest {
            // GIVEN
            coEvery { mockDao.getPendingSyncPosts() } throws Exception()

            // WHEN
            val result = dataSource.getPendingSyncPosts()

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }
    }

    @Nested
    inner class ClearCache {

        @Test
        fun `WHEN clearCache is called THEN dao deleteAllPosts is called`() = runTest {
            // WHEN
            dataSource.clearCache()

            // THEN
            coVerify { mockDao.deleteAllPosts() }
        }
    }
}
