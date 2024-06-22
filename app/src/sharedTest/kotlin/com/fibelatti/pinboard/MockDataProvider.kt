package com.fibelatti.pinboard

import com.fibelatti.bookmarking.core.Config.Pinboard
import com.fibelatti.bookmarking.core.network.PinboardApiResultCode
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.GetPostDto
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDto
import net.thauvin.erik.urlencoder.UrlEncoderUtil

object MockDataProvider {

    // region Properties
    const val mockUser = "user"
    const val mockApiToken = "user:00000000000"
    const val mockInstanceUrl = "https://www.linkding-instance.com/"
    const val mockTime = "2019-01-10T08:20:10Z"
    const val mockFutureTime = "2019-01-20T08:20:10Z"
    const val mockUrlValid = "https://www.url.com"
    const val mockUrlInvalid = "www.url.com"
    const val mockUrlTitle = "Some url title"
    const val mockUrlDescription = "What the url is all about"
    const val mockUrlNotes = "Some notes about this bookmark"
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

    val mockTagsResponse = mockTagsString.joinToString(Pinboard.TAG_SEPARATOR)

    const val mockTitle = "All"

    const val mockNoteId = "some-id"
    // endregion

    // region Data classes
    fun createGenericResponse(responseCode: PinboardApiResultCode): GenericResponseDto = GenericResponseDto(responseCode.value)

    fun createPostDto(
        href: String = UrlEncoderUtil.encode(mockUrlValid),
        description: String? = mockUrlTitle,
        extended: String? = mockUrlDescription,
        hash: String = mockHash,
        time: String = mockTime,
        shared: String = Pinboard.LITERAL_YES,
        toread: String = Pinboard.LITERAL_NO,
        tags: String = mockTagsResponse,
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

    fun createPostRemoteDto(
        href: String = UrlEncoderUtil.encode(mockUrlValid),
        description: String? = mockUrlTitle,
        extended: String? = mockUrlDescription,
        hash: String = mockHash,
        time: String = mockTime,
        shared: String = Pinboard.LITERAL_YES,
        toread: String = Pinboard.LITERAL_NO,
        tags: String = mockTagsResponse,
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

    fun createPost(
        id: String = mockHash,
        time: String = mockTime,
        url: String = mockUrlValid,
        title: String = mockUrlTitle,
        description: String = mockUrlDescription,
        private: Boolean = false,
        readLater: Boolean = false,
        tags: List<Tag>? = mockTags,
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

    fun createGetPostDto(
        posts: List<PostRemoteDto> = listOf(createPostRemoteDto()),
    ): GetPostDto = GetPostDto(
        date = mockTime,
        user = mockUser,
        posts = posts,
    )

    fun createTag(
        name: String = mockTagString1,
    ): Tag = Tag(name)
    // endregion

    // region Linking
    fun createBookmarkLocal(
        id: String = mockHash,
        url: String = mockUrlValid,
        title: String = mockUrlTitle,
        description: String = mockUrlDescription,
        notes: String? = null,
        websiteTitle: String? = null,
        websiteDescription: String? = null,
        isArchived: Boolean? = null,
        unread: Boolean? = false,
        shared: Boolean? = true,
        tagNames: String? = mockTagsResponse,
        dateModified: String = mockTime,
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
    // endregion
}
