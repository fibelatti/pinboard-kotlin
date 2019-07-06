package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.times

internal class PostActionHandlerTest {

    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockActiveNetworkInfo = mock<NetworkInfo>()

    private val mockPost = mock<Post>()

    private val postActionHandler = PostActionHandler(mockConnectivityManager)

    private val initialContent = PostList(
        category = All,
        title = mockTitle,
        posts = null,
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
            val result = postActionHandler.runAction(SetPosts(mock()), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val mockPosts = mock<Pair<Int, List<Post>>>()
            val result = postActionHandler.runAction(SetPosts(mockPosts), initialContent)

            // THEN
            result shouldBe initialContent.copy(posts = mockPosts, shouldLoad = false)
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

    @Nested
    inner class EditPostTests {

        @Test
        fun `WHEN currentContent is not PostDetail THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()

            // WHEN
            val result = postActionHandler.runAction(EditPost(mockPost), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetail THEN updated content is returned`() {
            // GIVEN
            val mockCurrentContent = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(EditPost(mockPost), mockCurrentContent)

            // THEN
            result shouldBe EditPostView(
                post = mockPost,
                previousContent = mockCurrentContent
            )
        }
    }

    @Nested
    inner class PostSavedTests {

        @Test
        fun `WHEN currentContent is not AddPostView or EditPostView THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(PostSaved(mockPost), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is AddPostView THEN updated content is returned`() {
            // GIVEN
            val currentContent = AddPostView(previousContent = initialContent)

            // WHEN
            val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

            // THEN
            result shouldBe PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true,
                isConnected = true
            )
        }

        @Test
        fun `WHEN currentContent is EditPostView THEN updated content is returned`() {
            // GIVEN
            val postDetail = PostDetail(
                post = mockPost,
                previousContent = initialContent
            )
            val currentContent = EditPostView(
                post = mockPost,
                previousContent = postDetail
            )

            // WHEN
            val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

            // THEN
            result shouldBe PostDetail(
                post = mockPost,
                previousContent = PostList(
                    category = All,
                    title = mockTitle,
                    posts = null,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = true,
                    isConnected = true
                )
            )
        }
    }

    @Nested
    inner class PostDeletedTests {

        @Test
        fun `WHEN currentContent is not PostDetail THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()

            // WHEN
            val result = postActionHandler.runAction(PostDeleted, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetail THEN updated content is returned`() {
            // GIVEN
            val currentContent = PostDetail(
                post = mockPost,
                previousContent = initialContent
            )

            // WHEN
            val result = postActionHandler.runAction(PostDeleted, currentContent)

            // THEN
            result shouldBe PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true,
                isConnected = true
            )
        }
    }
}
