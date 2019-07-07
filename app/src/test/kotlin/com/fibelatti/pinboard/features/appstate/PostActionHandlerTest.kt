package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtil
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtilFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.times

internal class PostActionHandlerTest {

    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockActiveNetworkInfo = mock<NetworkInfo>()

    private val mockPostListDiffUtilFactory = mock< PostListDiffUtilFactory>()

    private val mockPost = mock<Post>()

    private val postActionHandler = PostActionHandler(mockConnectivityManager, mockPostListDiffUtilFactory)

    private val initialContent = PostList(
        category = All,
        title = mockTitle,
        posts = null,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = Loaded,
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
        fun `GIVEN shouldLoad is ShouldLoadFirstPage WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(ShouldLoadFirstPage)

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadNextPage WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(ShouldLoadNextPage(0))

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList and is connected is false THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            // WHEN
            val result = postActionHandler.runAction(Refresh, initialContent)

            // THEN
            result shouldBe initialContent.copy(shouldLoad = Loaded, isConnected = false)
            verify(mockConnectivityManager, times(2)).isConnected()
        }

        @Test
        fun `WHEN currentContent is PostList and is connected is true THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(true)

            // WHEN
            val result = postActionHandler.runAction(Refresh, initialContent)

            // THEN
            result shouldBe initialContent.copy(shouldLoad = ShouldLoadFirstPage, isConnected = true)
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
        fun `WHEN currentContent is PostList and actions posts is null THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(SetPosts(null), initialContent)

            // THEN
            result shouldBe initialContent.copy(posts = null, shouldLoad = Loaded)
        }

        @Test
        fun `WHEN currentContent is PostList and actions posts is not null THEN updated content is returned`() {
            // GIVEN
            val mockDiffUtil = mock<PostListDiffUtil>()
            given(mockPostListDiffUtilFactory.create(emptyList(), listOf(createPost())))
                .willReturn(mockDiffUtil)

            val currentContent = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )

            // WHEN
            val result = postActionHandler.runAction(SetPosts(1 to listOf(createPost())), currentContent)

            // THEN
            result shouldBe PostList(
                category = All,
                title = mockTitle,
                posts = Triple(1, listOf(createPost()), mockDiffUtil),
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )
        }
    }

    @Nested
    inner class GetNextPostPageTests {

        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(GetNextPostPage, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList and posts is null THEN same content is returned `() {
            // WHEN
            val result = postActionHandler.runAction(GetNextPostPage, initialContent)

            // THEN
            result shouldBe initialContent
        }

        @Test
        fun `WHEN currentContent is PostList and posts is not null THEN updated content is returned`() {
            // GIVEN
            val currentContent = PostList(
                category = All,
                title = mockTitle,
                posts = Triple(1, listOf(mock()), mock()),
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = postActionHandler.runAction(GetNextPostPage, currentContent)

            // THEN
            result shouldBe currentContent.copy(shouldLoad = ShouldLoadNextPage(offset = 1))
        }
    }

    @Nested
    inner class SetNextPostPageTests {

        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(SetNextPostPage(mock()), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList and current content posts is null THEN same content is returned`() {
            // GIVEN
            val content = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = postActionHandler.runAction(SetNextPostPage(posts = 1 to listOf(createPost())), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList and action posts is null THEN same content is returned`() {
            // GIVEN
            val content = PostList(
                category = All,
                title = mockTitle,
                posts = Triple(1, listOf(createPost()), mock()),
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = postActionHandler.runAction(SetNextPostPage(posts = null), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList and posts are not null THEN updated content is returned`() {
            // GIVEN
            val mockCurrentList = listOf(mock<Post>())
            val mockNewList = listOf(mock<Post>())
            val mockDiffUtil = mock<PostListDiffUtil>()

            val currentContent = PostList(
                category = All,
                title = mockTitle,
                posts = Triple(1, mockCurrentList, mock()),
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )

            given(mockPostListDiffUtilFactory.create(mockCurrentList, mockCurrentList.plus(mockNewList)))
                .willReturn(mockDiffUtil)

            // WHEN
            val result = postActionHandler.runAction(SetNextPostPage(2 to mockNewList), currentContent)

            // THEN
            result shouldBe PostList(
                category = All,
                title = mockTitle,
                posts = Triple(2, mockCurrentList.plus(mockNewList), mockDiffUtil),
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )
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
        fun `GIVEN shouldLoad is ShouldLoadFirstPage WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(ShouldLoadFirstPage)

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadNextPage WHEN currentContent is PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()
            given(content.shouldLoad).willReturn(ShouldLoadNextPage(0))

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
            result shouldBe initialContent.copy(sortType = OldestFirst, shouldLoad = ShouldLoadFirstPage)
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
            result shouldBe initialContent.copy(sortType = NewestFirst, shouldLoad = ShouldLoadFirstPage)
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
                shouldLoad = ShouldLoadFirstPage,
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
                    shouldLoad = ShouldLoadFirstPage,
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
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )
        }
    }
}
