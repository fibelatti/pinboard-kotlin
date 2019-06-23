package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.core.extension.isConnected
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.times

internal class PostActionHandlerTest {

    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockActiveNetworkInfo = mock<NetworkInfo>()

    private val postActionHandler = PostActionHandler(mockConnectivityManager)

    private val initialContent = PostList(
        category = All,
        title = mockTitle,
        posts = emptyList(),
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = false,
        isConnected = true
    )

    @Nested
    inner class RefreshTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is true WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(true)

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            // WHEN
            val result = postActionHandler.runAction(Refresh, initialContent)

            // THEN
            result shouldBe initialContent.copy(shouldLoad = false, isConnected = false)
            verify(mockConnectivityManager, times(2)).isConnected()
        }
    }

    @Nested
    inner class SetPostsTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(SetPosts(listOf(createPost())), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(SetPosts(listOf(createPost())), initialContent)

            // THEN
            result shouldBe initialContent.copy(posts = listOf(createPost()), shouldLoad = false)
        }
    }

    @Nested
    inner class ToggleSortingTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is true WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(true)

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN isConnected is false WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, initialContent)

            // THEN
            result shouldBe initialContent.copy(isConnected = false)
        }

        @Test
        fun `GIVEN sortType is NewestFirst WHEN currentContent is PostList THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(true)

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, initialContent.copy(sortType = NewestFirst))

            // THEN
            result shouldBe initialContent.copy(sortType = OldestFirst, shouldLoad = true)
        }

        @Test
        fun `GIVEN sortType is OldestFirst WHEN currentContent is PostList THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(true)

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, initialContent.copy(sortType = OldestFirst))

            // THEN
            result shouldBe initialContent.copy(sortType = NewestFirst, shouldLoad = true)
        }
    }
}
