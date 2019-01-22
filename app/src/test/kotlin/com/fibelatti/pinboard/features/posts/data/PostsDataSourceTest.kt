package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.core.test.extension.willReturnDeferred
import com.fibelatti.core.test.extension.willReturnFailedDeferred
import com.fibelatti.pinboard.MockDataProvider.createGenericResponse
import com.fibelatti.pinboard.MockDataProvider.mockFutureTime
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsRequest
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
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
import kotlinx.coroutines.CompletableDeferred
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class PostsDataSourceTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockApi = mock<PostsApi>()
    private val mockDao = mock<PostsDao>()
    private val mockPostDtoMapper = mock<PostDtoMapper>()
    private val mockSuggestedTagsDtoMapper = mock<SuggestedTagDtoMapper>()
    private val mockDateFormatter = mock<DateFormatter>()

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
        mockDateFormatter
    )

    @Nested
    inner class UpdateTests {
        @Test
        fun `GIVEN that the api returns an error WHEN update is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.update())
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.update() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN update is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.update())
                .willReturnDeferred(UpdateDto(mockTime))

            // WHEN
            val result = callSuspend { dataSource.update() }

            // THEN
            result.shouldBeAnInstanceOf<Success<String>>()
            result.getOrNull() shouldBe mockTime
        }
    }

    @Nested
    inner class AddTests {

        @Test
        fun `GIVEN that the api returns an error WHEN add is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription, tags = mockTagsRequest))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription, tags = mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription, tags = mockTagsRequest))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription, tags = mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription, tags = mockTagsRequest))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription, tags = mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `GIVEN that the api returns an error WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.delete(mockUrlValid))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN delete is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.delete(mockUrlValid))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = callSuspend { dataSource.delete(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN delete is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.delete(mockUrlValid))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.DONE))

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
            given(mockApi.getRecentPosts(mockTagsRequest))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.getRecentPosts(mockTags) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getRecentPosts is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getRecentPosts(mockTagsRequest))
                .willReturn(CompletableDeferred(mockListPostDto))
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
        @Nested
        inner class API {
            @Test
            fun `GIVEN that the api returns an error WHEN getAllPosts is called THEN Failure is returned`() {
                // GIVEN
                given(mockUserRepository.getLastUpdate())
                    .willReturn(mockTime)
                given(mockApi.update())
                    .willReturnDeferred(UpdateDto(mockFutureTime))
                given(mockApi.getAllPosts(mockTagsRequest))
                    .willReturnFailedDeferred(Exception())

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
            }

            @Test
            fun `WHEN getAllPosts is called THEN Success is returned`() {
                // GIVEN
                given(mockUserRepository.getLastUpdate())
                    .willReturn(mockTime)
                given(mockApi.update())
                    .willReturnDeferred(UpdateDto(mockFutureTime))
                given(mockApi.getAllPosts(mockTagsRequest))
                    .willReturnDeferred(mockListPostDto)
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost
            }
        }

        @Nested
        inner class Database {
            @Test
            fun `GIVEN lastUpdate matches and localPosts is empty WHEN getAllPosts is called THEN api response is returned`() {
                // GIVEN
                given(mockUserRepository.getLastUpdate())
                    .willReturn(mockTime)
                given(mockApi.update())
                    .willReturnDeferred(UpdateDto(mockTime))
                given(mockDao.getAllPosts())
                    .willReturn(emptyList())
                given(mockApi.getAllPosts(mockTagsRequest))
                    .willReturnFailedDeferred(Exception())

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Failure>()
                result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()

                verify(mockUserRepository, never()).setLastUpdate(anyString())
                verify(mockDao, never()).savePosts(safeAny())
            }

            @Test
            fun `GIVEN lastUpdate matches and localPosts is empty WHEN getAllPosts is called and it fails to save the result THEN api response is returned normally`() {
                // GIVEN
                given(mockUserRepository.getLastUpdate())
                    .willReturn(mockTime)
                given(mockApi.update())
                    .willReturnDeferred(UpdateDto(mockTime))
                given(mockDao.getAllPosts())
                    .willReturn(emptyList())
                given(mockApi.getAllPosts(mockTagsRequest))
                    .willReturnDeferred(mockListPostDto)
                given(mockDao.savePosts(mockListPostDto))
                    .willAnswer { throw Exception() }
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost

                verify(mockUserRepository).setLastUpdate(mockTime)
                verify(mockDao).deleteAllPosts()
                verify(mockDao).savePosts(mockListPostDto)
            }

            @Test
            fun `GIVEN lastUpdate matches and localPosts is not empty WHEN getAllPosts is called THEN local posts are returned`() {
                // GIVEN
                given(mockUserRepository.getLastUpdate())
                    .willReturn(mockTime)
                given(mockApi.update())
                    .willReturnDeferred(UpdateDto(mockTime))
                given(mockDao.getAllPosts())
                    .willReturn(mockListPostDto)
                given(mockApi.getAllPosts(mockTagsRequest))
                    .willReturnFailedDeferred(Exception())
                given(mockPostDtoMapper.mapList(mockListPostDto))
                    .willReturn(mockListPost)

                // WHEN
                val result = callSuspend { dataSource.getAllPosts(mockTags) }

                // THEN
                result.shouldBeAnInstanceOf<Success<List<Post>>>()
                result.getOrNull() shouldBe mockListPost

                verify(mockApi, never()).getAllPosts(safeAny())
            }
        }
    }

    @Nested
    inner class GetSuggestedTagsForUrlTests {

        @Test
        fun `GIVEN that the api returns an error WHEN getSuggestedTagsForUrl is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.getSuggestedTagsForUrl(mockUrlValid))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.getSuggestedTagsForUrl(mockUrlValid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getSuggestedTagsForUrl is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getSuggestedTagsForUrl(mockUrlValid))
                .willReturnDeferred(mockSuggestedTagsDto)
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
