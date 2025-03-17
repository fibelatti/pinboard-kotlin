package com.fibelatti.pinboard

import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.ShouldLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.GetPostDto
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDto
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import java.net.URLEncoder

object MockDataProvider {

    // region Properties
    private const val SAMPLE_USERNAME = "user"
    const val SAMPLE_API_TOKEN = "user:00000000000"
    const val SAMPLE_INSTANCE_URL = "https://www.linkding-instance.com/"
    const val SAMPLE_DATE_TIME = "2019-01-10T08:20:10Z"
    const val SAMPLE_FUTURE_DATE_TIME = "2019-01-20T08:20:10Z"
    const val SAMPLE_URL_VALID = "https://www.url.com"
    const val SAMPLE_URL_INVALID = "www.url.com"
    const val SAMPLE_URL_TITLE = "Some url title"
    const val SAMPLE_URL_DESCRIPTION = "What the url is all about"
    const val SAMPLE_URL_NOTES = "Some notes about this bookmark"
    const val SAMPLE_HASH = "7b7cc6c90a84124026569c84f2236ecb"

    const val SAMPLE_TAG_VALUE_1 = "tag-1"
    const val SAMPLE_TAG_VALUE_2 = "tag2"
    const val SAMPLE_TAG_VALUE_3 = "tag3"
    private const val SAMPLE_TAG_VALUE_4 = "tag4"
    const val SAMPLE_TAG_VALUE_SYMBOLS = "tag<>\"&"
    const val SAMPLE_TAG_VALUE_ESCAPED = "tag&lt;&gt;&quot;&amp;"

    val SAMPLE_TAG_1 = Tag(name = SAMPLE_TAG_VALUE_1)
    val SAMPLE_TAG_2 = Tag(name = SAMPLE_TAG_VALUE_2)
    val SAMPLE_TAG_3 = Tag(name = SAMPLE_TAG_VALUE_3)
    val SAMPLE_TAG_4 = Tag(name = SAMPLE_TAG_VALUE_4)

    const val SAMPLE_DATE_TIME_1 = "2019-01-10T08:20:10Z"
    const val SAMPLE_DATE_TIME_2 = "2019-01-11T08:20:10Z"
    const val SAMPLE_DATE_TIME_3 = "2019-01-12T08:20:10Z"
    const val SAMPLE_DATE_TIME_4 = "2019-01-13T08:20:10Z"
    const val SAMPLE_DATE_TIME_5 = "2019-01-14T08:20:10Z"

    val SAMPLE_TAG_VALUES = listOf(SAMPLE_TAG_VALUE_1, SAMPLE_TAG_VALUE_2, SAMPLE_TAG_VALUE_3, SAMPLE_TAG_VALUE_4)
    val SAMPLE_TAGS = SAMPLE_TAG_VALUES.map(::Tag)

    val SAMPLE_TAGS_RESPONSE = SAMPLE_TAG_VALUES.joinToString(PinboardApiLiterals.TAG_SEPARATOR)

    const val SAMPLE_NOTE_ID = "some-id"
    // endregion

    // region Data classes
    fun createAppState(
        appMode: AppMode = AppMode.PINBOARD,
        content: Content = createPostListContent(),
        multiPanelAvailable: Boolean = false,
    ): AppState = AppState(
        appMode = appMode,
        content = content,
        multiPanelAvailable = multiPanelAvailable,
    )

    fun createPostListContent(
        category: ViewCategory = All,
        shouldLoad: ShouldLoad = ShouldLoadFirstPage,
        sortType: SortType = ByDateAddedNewestFirst,
        searchParameters: SearchParameters = SearchParameters(),
    ): PostListContent = PostListContent(
        category = category,
        posts = null,
        showDescription = false,
        sortType = sortType,
        searchParameters = searchParameters,
        shouldLoad = shouldLoad,
        isConnected = false,
    )

    fun createLoginContent(): LoginContent = LoginContent()

    fun createGenericResponse(responseCode: ApiResultCodes): GenericResponseDto = GenericResponseDto(responseCode.code)

    fun createPostDto(
        href: String = URLEncoder.encode(SAMPLE_URL_VALID, AppConfig.API_ENCODING),
        description: String? = SAMPLE_URL_TITLE,
        extended: String? = SAMPLE_URL_DESCRIPTION,
        hash: String = SAMPLE_HASH,
        time: String = SAMPLE_DATE_TIME,
        shared: String = PinboardApiLiterals.YES,
        toread: String = PinboardApiLiterals.NO,
        tags: String = SAMPLE_TAGS_RESPONSE,
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
        href: String = URLEncoder.encode(SAMPLE_URL_VALID, AppConfig.API_ENCODING),
        description: String? = SAMPLE_URL_TITLE,
        extended: String? = SAMPLE_URL_DESCRIPTION,
        hash: String = SAMPLE_HASH,
        time: String = SAMPLE_DATE_TIME,
        shared: String = PinboardApiLiterals.YES,
        toread: String = PinboardApiLiterals.NO,
        tags: String = SAMPLE_TAGS_RESPONSE,
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
        id: String = SAMPLE_HASH,
        time: String = SAMPLE_DATE_TIME,
        url: String = SAMPLE_URL_VALID,
        title: String = SAMPLE_URL_TITLE,
        description: String = SAMPLE_URL_DESCRIPTION,
        private: Boolean = false,
        readLater: Boolean = false,
        tags: List<Tag>? = SAMPLE_TAGS,
        pendingSync: PendingSync? = null,
    ): Post = Post(
        url = url,
        title = title,
        description = description,
        id = id,
        dateAdded = time,
        private = private,
        readLater = readLater,
        tags = tags,
        pendingSync = pendingSync,
    )

    fun createGetPostDto(
        posts: List<PostRemoteDto> = listOf(createPostRemoteDto()),
    ): GetPostDto = GetPostDto(
        date = SAMPLE_DATE_TIME,
        user = SAMPLE_USERNAME,
        posts = posts,
    )

    fun createTag(
        name: String = SAMPLE_TAG_VALUE_1,
    ): Tag = Tag(name)
    // endregion

    // region Linking
    fun createBookmarkLocal(
        id: String = SAMPLE_HASH,
        url: String = SAMPLE_URL_VALID,
        title: String = SAMPLE_URL_TITLE,
        description: String = SAMPLE_URL_DESCRIPTION,
        notes: String? = null,
        websiteTitle: String? = null,
        websiteDescription: String? = null,
        isArchived: Boolean? = null,
        unread: Boolean? = false,
        shared: Boolean? = true,
        tagNames: String? = SAMPLE_TAGS_RESPONSE,
        dateAdded: String = SAMPLE_DATE_TIME,
        dateModified: String = SAMPLE_DATE_TIME,
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
        dateAdded = dateAdded,
        dateModified = dateModified,
        pendingSync = pendingSync,
    )
    // endregion
}
