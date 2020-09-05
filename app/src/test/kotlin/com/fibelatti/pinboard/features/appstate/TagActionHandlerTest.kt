package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TagActionHandlerTest {

    private val mockUserRepository = mockk<UserRepository>()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

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
            val content = mockk<PostListContent>()

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(RefreshTags, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(RefreshTags, initialContent) }

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = mockPreviousContent
                )
            )
            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class SetTagsTests {

        @Test
        fun `WHEN currentContent is not TagListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = runBlocking { tagActionHandler.runAction(mockk<SetTags>(), content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() {
            // WHEN
            val result = runBlocking {
                tagActionHandler.runAction(SetTags(listOf(createTag())), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = listOf(createTag()),
                    shouldLoad = false,
                    isConnected = true,
                    previousContent = mockPreviousContent
                )
            )
        }
    }

    @Nested
    inner class PostsForTagTests {

        @Test
        fun `WHEN postsForTag is called THEN PostListContent is returned and search parameters contains the tag`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            val randomBoolean = randomBoolean()
            every { mockUserRepository.getShowDescriptionInLists() } returns randomBoolean

            // WHEN
            val result = runBlocking {
                tagActionHandler.runAction(PostsForTag(createTag()), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = randomBoolean,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(tags = listOf(createTag())),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = true
                )
            )
        }
    }
}
