package com.fibelatti.pinboard

import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import java.net.URLEncoder

object MockDataProvider {

    // region Properties
    const val mockTime = "2019-01-10T08:20:10Z"
    const val mockUrlValid = "https://www.url.com"
    const val mockUrlInvalid = "www.url.com"
    const val mockUrlDescription = "Some url description"
    const val mockTag = "tag"
    const val mockTagsResponse = "tag1 tag2 tag3"
    val mockTags = listOf("tag1", "tag2", "tag3")
    // endregion

    // region Data classes
    fun createGenericResponse(responseCode: ApiResultCodes): GenericResponseDto =
        GenericResponseDto(responseCode.code)

    fun createPostDto(
        shared: String = PinboardApiLiterals.YES,
        toread: String = PinboardApiLiterals.YES,
        tags: String = mockTagsResponse
    ): PostDto =
        PostDto(
            href = URLEncoder.encode(mockUrlValid, AppConfig.API_ENCODING),
            description = mockUrlDescription,
            extended = mockUrlDescription,
            time = mockTime,
            shared = shared,
            toread = toread,
            tags = tags
        )

    fun createPost(
        public: Boolean = true,
        unread: Boolean = true,
        tags: List<String> = mockTags
    ): Post =
        Post(
            url = mockUrlValid,
            description = mockUrlDescription,
            extendedDescription = mockUrlDescription,
            time = mockTime,
            public = public,
            unread = unread,
            tags = tags
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
}
