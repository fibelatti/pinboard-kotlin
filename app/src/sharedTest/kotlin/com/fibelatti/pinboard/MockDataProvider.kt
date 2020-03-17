package com.fibelatti.pinboard

import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.GetPostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import java.net.URLEncoder

object MockDataProvider {

    // region Properties
    const val mockUser = "user"
    const val mockApiToken = "user:00000000000"
    const val mockTime = "2019-01-10T08:20:10Z"
    const val mockFutureTime = "2019-01-20T08:20:10Z"
    const val mockUrlValid = "https://www.url.com"
    const val mockUrlInvalid = "www.url.com"
    const val mockUrlTitle = "Some url title"
    const val mockUrlDescription = "What the url is all about"
    const val mockHash = "7b7cc6c90a84124026569c84f2236ecb"
    const val mockShared = "yes"
    const val mockToRead = "no"

    const val mockTagString1 = "tag-1"
    const val mockTagString2 = "tag2"
    const val mockTagString3 = "tag3"
    const val mockTagString4 = "tag4"
    const val mockTagStringHtml = "tag<>\"&"
    const val mockTagStringHtmlEscaped = "tag&lt;&gt;&quot;&amp;"

    val mockTag1 = Tag(name = mockTagString1)
    val mockTag2 = Tag(name = mockTagString2)
    val mockTag3 = Tag(name = mockTagString3)
    val mockTag4 = Tag(name = mockTagString4)

    const val mockTime1 = "2019-01-10T08:20:10Z"
    const val mockTime2 = "2019-01-11T08:20:10Z"
    const val mockTime3 = "2019-01-12T08:20:10Z"
    const val mockTime4 = "2019-01-13T08:20:10Z"
    const val mockTime5 = "2019-01-14T08:20:10Z"

    val mockTagsString = listOf(mockTagString1, mockTagString2, mockTagString3, mockTagString4)
    val mockTags = mockTagsString.map(::Tag)

    val mockTagsResponse = mockTagsString.joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
    val mockTagsRequest = mockTagsString.joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST)

    const val mockTitle = "All"

    const val mockNoteId = "some-id"
    // endregion

    // region Data classes
    fun createGenericResponse(responseCode: ApiResultCodes): GenericResponseDto =
        GenericResponseDto(responseCode.code)

    fun createPostDto(
        href: String = URLEncoder.encode(mockUrlValid, AppConfig.API_ENCODING),
        description: String = mockUrlTitle,
        extended: String = mockUrlDescription,
        hash: String = mockHash,
        time: String = mockTime,
        shared: String = PinboardApiLiterals.YES,
        toread: String = PinboardApiLiterals.NO,
        tags: String = mockTagsResponse
    ): PostDto =
        PostDto(
            href = href,
            description = description,
            extended = extended,
            hash = hash,
            time = time,
            shared = shared,
            toread = toread,
            tags = tags,
            imageUrl = null
        )

    fun createPost(
        hash: String = mockHash,
        time: String = mockTime,
        url: String = mockUrlValid,
        title: String = mockUrlTitle,
        description: String = mockUrlDescription,
        private: Boolean = false,
        readLater: Boolean = false,
        tags: List<Tag>? = mockTags
    ): Post =
        Post(
            url = url,
            title = title,
            description = description,
            hash = hash,
            time = time,
            private = private,
            readLater = readLater,
            tags = tags
        )

    fun createGetPostDto(
        posts: List<PostDto> = listOf(createPostDto())
    ): GetPostDto =
        GetPostDto(
            date = mockTime,
            user = mockUser,
            posts = posts
        )

    fun createSuggestedTagsDto(
        popular: List<String> = mockTagsString,
        recommended: List<String> = mockTagsString
    ): SuggestedTagsDto = SuggestedTagsDto(popular, recommended)

    fun createSuggestedTags(
        popular: List<Tag> = mockTags,
        recommended: List<Tag> = mockTags
    ): SuggestedTags = SuggestedTags(popular, recommended)

    fun createTag(
        name: String = mockTagString1
    ): Tag = Tag(name)
    // endregion
}
