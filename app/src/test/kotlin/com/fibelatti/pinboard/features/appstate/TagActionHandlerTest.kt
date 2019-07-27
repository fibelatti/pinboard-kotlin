package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

internal class TagActionHandlerTest {

    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val tagActionHandler = TagActionHandler(
        mockResourceProvider,
        mockConnectivityInfoProvider
    )

    val mockPreviousContent = PostListContent(
        category = All,
        title = mockTitle,
        posts = null,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = Loaded
    )
    private val initialContent = TagListContent(
        tags = emptyList(),
        shouldLoad = true,
        isConnected = true,
        previousContent = mockPreviousContent
    )

    @Nested
    inner class RefreshTagsTests {
        @Test
        fun `WHEN currentContent is not TagListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(RefreshTags, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(RefreshTags, initialContent) }

            // THEN
            result shouldBe TagListContent(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = mockPreviousContent
            )
            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class SetTagsTests {

        @Test
        fun `WHEN currentContent is not TagListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(mock<SetTags>(), content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() {
            // WHEN
            val result = runBlocking {
                tagActionHandler.runAction(SetTags(listOf(createTag())), initialContent)
            }

            // THEN
            result shouldBe TagListContent(
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
        fun `WHEN postsForTag is called THEN PostListContent is returned and search parameters contains the tag`() {
            // GIVEN
            given(mockResourceProvider.getString(R.string.posts_title_all))
                .willReturn(mockTitle)
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking {
                tagActionHandler.runAction(PostsForTag(createTag()), initialContent)
            }

            // THEN
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(tags = listOf(createTag())),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )
        }
    }
}
