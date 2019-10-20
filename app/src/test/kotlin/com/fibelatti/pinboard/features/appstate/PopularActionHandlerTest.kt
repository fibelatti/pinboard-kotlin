package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.randomBoolean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

internal class PopularActionHandlerTest {

    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val popularActionHandler = PopularActionHandler(
        mockConnectivityInfoProvider
    )

    private val mockBoolean = randomBoolean()
    private val initialContent = PopularPostsContent(
        posts = mock(),
        shouldLoad = mockBoolean,
        isConnected = mockBoolean,
        previousContent = mock()
    )

    @Nested
    inner class RefreshPopularTests {

        @Test
        fun `WHEN currentContent is not PopularPostsContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { popularActionHandler.runAction(RefreshPopular, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking { popularActionHandler.runAction(RefreshPopular, initialContent) }

            // THEN
            result shouldBe PopularPostsContent(
                posts = initialContent.posts,
                shouldLoad = true,
                isConnected = true,
                previousContent = initialContent.previousContent
            )
            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class SetPopularPostsTests {

        @Test
        fun `WHEN currentContent is not PopularPostsContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { popularActionHandler.runAction(mock<SetPopularPosts>(), content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val newPosts: List<Post> = mock()
            val result = runBlocking {
                popularActionHandler.runAction(SetPopularPosts(newPosts), initialContent)
            }

            // THEN
            result shouldBe PopularPostsContent(
                posts = newPosts,
                shouldLoad = false,
                isConnected = initialContent.isConnected,
                previousContent = initialContent.previousContent
            )
        }
    }
}
