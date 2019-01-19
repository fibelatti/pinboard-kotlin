package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.core.test.extension.willReturnDeferred
import com.fibelatti.core.test.extension.willReturnFailedDeferred
import com.fibelatti.pinboard.MockDataProvider.createGenericResponse
import com.fibelatti.pinboard.MockDataProvider.mockTag
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlInvalid
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import kotlinx.coroutines.CompletableDeferred
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given

class PostsDataSourceTest {

    private val mockApi = mock<PostsApi>()
    private val mockPostDtoMapper = mock<PostDtoMapper>()
    private val mockSuggestedTagsDtoMapper = mock<SuggestedTagDtoMapper>()

    private val mockListPostDto = listOf(mock<PostDto>())
    private val mockListPost = listOf(mock<Post>())
    private val mockSuggestedTagsDto = mock<SuggestedTagsDto>()
    private val mockSuggestedTags = mock<SuggestedTags>()

    private val dataSource: PostsRepository = PostsDataSource(
        mockApi,
        mockPostDtoMapper,
        mockSuggestedTagsDtoMapper
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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AddTests {
        fun validUrls(): Array<String> = UrlValidSchemes.allSchemes()
            .map { "$it://$mockUrlInvalid" }.toTypedArray()

        @ParameterizedTest
        @MethodSource("validUrls")
        fun `GIVEN that a valid url is received WHEN add is called THEN Success is returned`(url: String) {
            // GIVEN
            given(mockApi.add(url, mockUrlDescription))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend { dataSource.add(url, mockUrlDescription) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }

        @Test
        fun `GIVEN that an invalid url is received WHEN add is called THEN Failure is returned`() {
            // WHEN
            val result = callSuspend { dataSource.add(mockUrlInvalid, mockUrlDescription) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidRequestException>()
        }

        @Test
        fun `GIVEN that the api returns an error WHEN add is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN that the api returns 200 but the result code is not DONE WHEN add is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.MISSING_URL))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN that the api returns 200 and the result code is DONE WHEN add is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.add(mockUrlValid, mockUrlDescription))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend { dataSource.add(mockUrlValid, mockUrlDescription) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteTests {
        fun validUrls(): Array<String> = UrlValidSchemes.allSchemes()
            .map { "$it://$mockUrlInvalid" }.toTypedArray()

        @ParameterizedTest
        @MethodSource("validUrls")
        fun `GIVEN that a valid url is received WHEN delete is called THEN Success is returned`(url: String) {
            // GIVEN
            given(mockApi.delete(url))
                .willReturnDeferred(createGenericResponse(ApiResultCodes.DONE))

            // WHEN
            val result = callSuspend { dataSource.delete(url) }

            // THEN
            result.shouldBeAnInstanceOf<Success<Unit>>()
        }

        @Test
        fun `GIVEN that an invalid url is received WHEN delete is called THEN Failure is returned`() {
            // WHEN
            val result = callSuspend { dataSource.delete(mockUrlInvalid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidRequestException>()
        }

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
            given(mockApi.getRecentPosts(mockTag))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.getRecentPosts(mockTag) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getRecentPosts is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getRecentPosts(mockTag))
                .willReturn(CompletableDeferred(mockListPostDto))
            given(mockPostDtoMapper.mapList(mockListPostDto))
                .willReturn(mockListPost)

            // WHEN
            val result = callSuspend { dataSource.getRecentPosts(mockTag) }

            // THEN
            result.shouldBeAnInstanceOf<Success<List<Post>>>()
            result.getOrNull() shouldBe mockListPost
        }
    }

    @Nested
    inner class GetAllPostsTests {
        @Test
        fun `GIVEN that the api returns an error WHEN getAllPosts is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.getAllPosts(mockTag))
                .willReturnFailedDeferred(Exception())

            // WHEN
            val result = callSuspend { dataSource.getAllPosts(mockTag) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `WHEN getAllPosts is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getAllPosts(mockTag))
                .willReturnDeferred(mockListPostDto)
            given(mockPostDtoMapper.mapList(mockListPostDto))
                .willReturn(mockListPost)

            // WHEN
            val result = callSuspend { dataSource.getAllPosts(mockTag) }

            // THEN
            result.shouldBeAnInstanceOf<Success<List<Post>>>()
            result.getOrNull() shouldBe mockListPost
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetSuggestedTagsForUrlTests {
        fun validUrls(): Array<String> = UrlValidSchemes.allSchemes()
            .map { "$it://$mockUrlInvalid" }.toTypedArray()

        @ParameterizedTest
        @MethodSource("validUrls")
        fun `GIVEN that a valid url is received WHEN getSuggestedTagsForUrl is called THEN Success is returned`(url: String) {
            // GIVEN
            given(mockApi.getSuggestedTagsForUrl(url))
                .willReturnDeferred(mockSuggestedTagsDto)
            given(mockSuggestedTagsDtoMapper.map(mockSuggestedTagsDto))
                .willReturn(mockSuggestedTags)

            // WHEN
            val result = callSuspend { dataSource.getSuggestedTagsForUrl(url) }

            // THEN
            result.shouldBeAnInstanceOf<Success<SuggestedTags>>()
            result.getOrNull() shouldBe mockSuggestedTags
        }

        @Test
        fun `GIVEN that an invalid url is received WHEN getSuggestedTagsForUrl is called THEN Failure is returned`() {
            // WHEN
            val result = callSuspend { dataSource.getSuggestedTagsForUrl(mockUrlInvalid) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidRequestException>()
        }

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
