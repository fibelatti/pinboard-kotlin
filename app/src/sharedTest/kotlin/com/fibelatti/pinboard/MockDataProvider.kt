package com.fibelatti.pinboard

import com.fibelatti.core.functional.Failure
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.RecentDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URLEncoder

object MockDataProvider {

    // region Properties
    const val mockUser = "user"
    const val mockApiToken = "user:00000000000"
    const val mockTime = "2019-01-10T08:20:10Z"
    const val mockFutureTime = "2019-01-20T08:20:10Z"
    const val mockUrlValid = "https://www.url.com"
    const val mockUrlInvalid = "www.url.com"
    const val mockUrlDescription = "Some url description"
    const val mockHash = "7b7cc6c90a84124026569c84f2236ecb"

    const val mockTag1 = "tag1"
    const val mockTag2 = "tag2"
    const val mockTag3 = "tag3"
    const val mockTag4 = "tag4"

    const val mockTime1 = "2019-01-10T08:20:10Z"
    const val mockTime2 = "2019-01-11T08:20:10Z"
    const val mockTime3 = "2019-01-12T08:20:10Z"
    const val mockTime4 = "2019-01-13T08:20:10Z"

    val mockTags = listOf(mockTag1, mockTag2, mockTag3, mockTag4)
    val mockTagsTrimmed = listOf(mockTag1, mockTag2, mockTag3)

    val mockTagsResponse = mockTags.joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
    val mockTagsRequest = mockTags.joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST)
    // endregion

    // region Data classes
    fun createGenericResponse(responseCode: ApiResultCodes): GenericResponseDto =
        GenericResponseDto(responseCode.code)

    fun createPostDto(
        hash: String = mockHash,
        shared: String = PinboardApiLiterals.YES,
        toread: String = PinboardApiLiterals.YES,
        tags: String = mockTagsResponse
    ): PostDto =
        PostDto(
            href = URLEncoder.encode(mockUrlValid, AppConfig.API_ENCODING),
            description = mockUrlDescription,
            extended = mockUrlDescription,
            hash = hash,
            time = mockTime,
            shared = shared,
            toread = toread,
            tags = tags
        )

    fun createPost(
        hash: String = mockHash,
        time: String = mockTime,
        public: Boolean = true,
        unread: Boolean = true,
        tags: List<String> = mockTags
    ): Post =
        Post(
            url = mockUrlValid,
            description = mockUrlDescription,
            extendedDescription = mockUrlDescription,
            hash = hash,
            time = time,
            public = public,
            unread = unread,
            tags = tags
        )

    fun createRecentDto(
        posts: List<PostDto> = listOf(createPostDto())
    ): RecentDto =
        RecentDto(
            date = mockTime,
            user = mockUser,
            posts = posts
        )

    fun createSuggestedTagsDto(
        popular: List<String> = mockTags,
        recommended: List<String> = mockTags
    ): SuggestedTagsDto = SuggestedTagsDto(popular, recommended)

    fun createSuggestedTags(
        popular: List<String> = mockTags,
        recommended: List<String> = mockTags
    ): SuggestedTags = SuggestedTags(popular, recommended)
    // endregion

    fun UnauthorizedFailure(): Failure = Failure(
        HttpException(
            Response.error<GenericResponseDto>(
                HttpURLConnection.HTTP_UNAUTHORIZED,
                ResponseBody.create(MediaType.parse("application/json"), "{}")
            )
        )
    )
}
