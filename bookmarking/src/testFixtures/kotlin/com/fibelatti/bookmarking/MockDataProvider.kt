package com.fibelatti.bookmarking

import com.fibelatti.bookmarking.core.Config.Pinboard
import com.fibelatti.bookmarking.core.network.PinboardApiResultCode
import com.fibelatti.bookmarking.features.posts.data.model.PendingSyncDto
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.linkding.data.BookmarkLocal
import com.fibelatti.bookmarking.pinboard.data.GenericResponseDto
import com.fibelatti.bookmarking.pinboard.data.GetPostDto
import com.fibelatti.bookmarking.pinboard.data.PostDto
import com.fibelatti.bookmarking.pinboard.data.PostRemoteDto
import net.thauvin.erik.urlencoder.UrlEncoderUtil

@Suppress("MemberVisibilityCanBePrivate")
public object MockDataProvider {

    // region Properties
    public const val MOCK_USER: String = "user"
    public const val MOCK_API_TOKEN: String = "user:00000000000"
    public const val MOCK_INSTANCE_URL: String = "https://www.linkding-instance.com/"
    public const val MOCK_TIME: String = "2019-01-10T08:20:10Z"
    public const val MOCK_FUTURE_TIME: String = "2019-01-20T08:20:10Z"
    public const val MOCK_URL_VALID: String = "https://www.url.com"
    public const val MOCK_URL_INVALID: String = "www.url.com"
    public const val MOCK_URL_TITLE: String = "Some url title"
    public const val MOCK_URL_DESCRIPTION: String = "What the url is all about"
    public const val MOCK_URL_NOTES: String = "Some notes about this bookmark"
    public const val MOCK_HASH: String = "7b7cc6c90a84124026569c84f2236ecb"
    public const val MOCK_SHARED: String = "yes"
    public const val MOCK_TO_READ: String = "no"

    public const val MOCK_TAG_STRING_1: String = "tag-1"
    public const val MOCK_TAG_STRING_2: String = "tag2"
    public const val MOCK_TAG_STRING_3: String = "tag3"
    public const val MOCK_TAG_STRING_4: String = "tag4"
    public const val MOCK_TAG_STRING_HTML: String = "tag<>\"&"
    public const val MOCK_TAG_STRING_HTML_ESCAPED: String = "tag&lt;&gt;&quot;&amp;"

    public val MOCK_TAG_1: Tag = Tag(name = MOCK_TAG_STRING_1)
    public val MOCK_TAG_2: Tag = Tag(name = MOCK_TAG_STRING_2)
    public val MOCK_TAG_3: Tag = Tag(name = MOCK_TAG_STRING_3)
    public val MOCK_TAG_4: Tag = Tag(name = MOCK_TAG_STRING_4)

    public const val MOCK_TIME_1: String = "2019-01-10T08:20:10Z"
    public const val MOCK_TIME_2: String = "2019-01-11T08:20:10Z"
    public const val MOCK_TIME_3: String = "2019-01-12T08:20:10Z"
    public const val MOCK_TIME_4: String = "2019-01-13T08:20:10Z"
    public const val MOCK_TIME_5: String = "2019-01-14T08:20:10Z"

    public val MOCK_TAGS_STRING: List<String> = listOf(
        MOCK_TAG_STRING_1,
        MOCK_TAG_STRING_2,
        MOCK_TAG_STRING_3,
        MOCK_TAG_STRING_4,
    )
    public val MOCK_TAGS: List<Tag> = MOCK_TAGS_STRING.map(::Tag)

    public val MOCK_TAGS_RESPONSE: String = MOCK_TAGS_STRING.joinToString(Pinboard.TAG_SEPARATOR)

    public const val MOCK_TITLE: String = "All"

    public const val MOCK_NOTE_ID: String = "some-id"
    // endregion

    // region Pinboard
    internal fun createGenericResponse(
        responseCode: PinboardApiResultCode,
    ): GenericResponseDto = GenericResponseDto(
        resultCode = responseCode.value,
    )

    internal fun createPostDto(
        href: String = UrlEncoderUtil.encode(MOCK_URL_VALID),
        description: String? = MOCK_URL_TITLE,
        extended: String? = MOCK_URL_DESCRIPTION,
        hash: String = MOCK_HASH,
        time: String = MOCK_TIME,
        shared: String = Pinboard.LITERAL_YES,
        toread: String = Pinboard.LITERAL_NO,
        tags: String = MOCK_TAGS_RESPONSE,
        pendingSync: PendingSyncDto? = null,
    ): PostDto = PostDto(
        href = href,
        description = description,
        extended = extended,
        hash = hash,
        time = time,
        shared = shared,
        toread = toread,
        tags = tags,
        pendingSync = pendingSync,
    )

    internal fun createPostRemoteDto(
        href: String = UrlEncoderUtil.encode(MOCK_URL_VALID),
        description: String? = MOCK_URL_TITLE,
        extended: String? = MOCK_URL_DESCRIPTION,
        hash: String = MOCK_HASH,
        time: String = MOCK_TIME,
        shared: String = Pinboard.LITERAL_YES,
        toread: String = Pinboard.LITERAL_NO,
        tags: String = MOCK_TAGS_RESPONSE,
    ): PostRemoteDto = PostRemoteDto(
        href = href,
        description = description,
        extended = extended,
        hash = hash,
        time = time,
        shared = shared,
        toread = toread,
        tags = tags,
    )

    internal fun createGetPostDto(
        posts: List<PostRemoteDto> = listOf(createPostRemoteDto()),
    ): GetPostDto = GetPostDto(
        date = MOCK_TIME,
        user = MOCK_USER,
        posts = posts,
    )
    // endregion Pinboard

    // region Linking
    internal fun createBookmarkLocal(
        id: String = MOCK_HASH,
        url: String = MOCK_URL_VALID,
        title: String = MOCK_URL_TITLE,
        description: String = MOCK_URL_DESCRIPTION,
        notes: String? = null,
        websiteTitle: String? = null,
        websiteDescription: String? = null,
        isArchived: Boolean? = null,
        unread: Boolean? = false,
        shared: Boolean? = true,
        tagNames: String? = MOCK_TAGS_RESPONSE,
        dateModified: String = MOCK_TIME,
        pendingSync: PendingSyncDto? = null,
    ): BookmarkLocal = BookmarkLocal(
        id = id,
        url = url,
        title = title,
        description = description,
        notes = notes,
        websiteTitle = websiteTitle,
        websiteDescription = websiteDescription,
        isArchived = isArchived,
        unread = unread,
        shared = shared,
        tagNames = tagNames,
        dateModified = dateModified,
        pendingSync = pendingSync,
    )
    // endregion Linking

    // region Domain
    public fun createPost(
        id: String = MOCK_HASH,
        time: String = MOCK_TIME,
        url: String = MOCK_URL_VALID,
        title: String = MOCK_URL_TITLE,
        description: String = MOCK_URL_DESCRIPTION,
        private: Boolean = false,
        readLater: Boolean = false,
        tags: List<Tag>? = MOCK_TAGS,
        pendingSync: PendingSync? = null,
    ): Post = Post(
        url = url,
        title = title,
        description = description,
        id = id,
        time = time,
        private = private,
        readLater = readLater,
        tags = tags,
        pendingSync = pendingSync,
    )

    public fun createTag(
        name: String = MOCK_TAG_STRING_1,
    ): Tag = Tag(name)
    // endregion Domain
}
