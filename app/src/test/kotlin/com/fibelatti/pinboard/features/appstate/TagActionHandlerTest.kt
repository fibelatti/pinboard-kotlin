package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isConnected
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.verify

internal class TagActionHandlerTest {

    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockActiveNetworkInfo = mock<NetworkInfo>()

    private val tagActionHandler = TagActionHandler(
        mockResourceProvider,
        mockConnectivityManager
    )

    val mockPreviousContent = PostList(
        category = All,
        title = mockTitle,
        posts = null,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = false
    )
    private val initialContent = TagList(
        tags = emptyList(),
        shouldLoad = true,
        isConnected = true,
        previousContent = mockPreviousContent
    )

    @Nested
    inner class RefreshTagsTests {
        @Test
        fun `WHEN currentContent is not TagList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()

            // WHEN
            val result = tagActionHandler.runAction(RefreshTags, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is TagList THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            // WHEN
            val result = tagActionHandler.runAction(RefreshTags, initialContent)

            // THEN
            result shouldBe TagList(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = mockPreviousContent
            )
            verify(mockConnectivityManager, Mockito.times(2)).isConnected()
        }
    }

    @Nested
    inner class SetTagsTests {

        @Test
        fun `WHEN currentContent is not TagList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()

            // WHEN
            val result = tagActionHandler.runAction(mock<SetTags>(), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is TagList THEN updated content is returned`() {
            // WHEN
            val result = tagActionHandler.runAction(SetTags(listOf(createTag())), initialContent)

            // THEN
            result shouldBe TagList(
                tags = listOf(createTag()),
                shouldLoad = false,
                isConnected = true,
                previousContent = mockPreviousContent
            )
        }
    }

    @Nested
    inner class PostsForTagTests {

        @Test
        fun `WHEN postsForTag is called THEN PostList is returned and search parameters contains the tag`() {
            // GIVEN
            given(mockResourceProvider.getString(R.string.posts_title_all))
                .willReturn(mockTitle)
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(true)

            // WHEN
            val result = tagActionHandler.runAction(PostsForTag(createTag()), initialContent)

            // THEN
            result shouldBe PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(tags = listOf(createTag())),
                shouldLoad = true,
                isConnected = true
            )
        }
    }
}
