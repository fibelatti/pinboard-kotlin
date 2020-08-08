package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

internal class TagActionHandlerTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val tagActionHandler = TagActionHandler(
        mockUserRepository,
        mockConnectivityInfoProvider
    )

    val mockPreviousContent = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
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
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            val randomBoolean = randomBoolean()
            given(mockUserRepository.getShowDescriptionInLists())
                .willReturn(randomBoolean)

            // WHEN
            val result = runBlocking {
                tagActionHandler.runAction(PostsForTag(createTag()), initialContent)
            }

            // THEN
            result shouldBe PostListContent(
                category = All,
                posts = null,
                showDescription = randomBoolean,
                sortType = NewestFirst,
                searchParameters = SearchParameters(tags = listOf(createTag())),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )
        }
    }
}
