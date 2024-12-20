package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PopularActionHandlerTest {

    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val popularActionHandler = PopularActionHandler(
        mockConnectivityInfoProvider,
    )

    private val mockBoolean = randomBoolean()
    private val initialContent = PopularPostsContent(
        posts = mockk(),
        shouldLoad = mockBoolean,
        isConnected = mockBoolean,
        previousContent = mockk(),
    )

    @Nested
    inner class RefreshPopularTests {

        @Test
        fun `WHEN currentContent is not PopularPostsContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = popularActionHandler.runAction(RefreshPopular, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent THEN updated content is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            // WHEN
            val result = popularActionHandler.runAction(RefreshPopular, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                PopularPostsContent(
                    posts = initialContent.posts,
                    shouldLoad = true,
                    isConnected = true,
                    previousContent = initialContent.previousContent,
                ),
            )
            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class SetPopularPostsTests {

        @Test
        fun `WHEN currentContent is not PopularPostsContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = popularActionHandler.runAction(mockk<SetPopularPosts>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent THEN updated content is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            // WHEN
            val newPosts: Map<Post, Int> = mockk()
            val result = popularActionHandler.runAction(SetPopularPosts(newPosts), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                PopularPostsContent(
                    posts = newPosts,
                    shouldLoad = false,
                    isConnected = initialContent.isConnected,
                    previousContent = initialContent.previousContent,
                ),
            )
        }
    }
}
