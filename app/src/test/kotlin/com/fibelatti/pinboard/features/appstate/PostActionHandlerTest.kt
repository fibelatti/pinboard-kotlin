package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtil
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtilFactory
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.times

internal class PostActionHandlerTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()
    private val mockPostListDiffUtilFactory = mock<PostListDiffUtilFactory>()

    private val mockPost = mock<Post>()

    private val postActionHandler = PostActionHandler(
        mockUserRepository,
        mockConnectivityInfoProvider,
        mockPostListDiffUtilFactory
    )

    private val initialContent = PostListContent(
        category = All,
        title = mockTitle,
        posts = null,
        showDescription = false,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = Loaded,
        isConnected = true
    )

    @Nested
    inner class RefreshTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadFirstPage WHEN currentContent is PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()
            given(content.shouldLoad).willReturn(ShouldLoadFirstPage)

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadNextPage WHEN currentContent is PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()
            given(content.shouldLoad).willReturn(ShouldLoadNextPage(0))

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and is connected is false THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, initialContent) }

            // THEN
            result shouldBe initialContent.copy(shouldLoad = Loaded, isConnected = false)
            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }

        @Test
        fun `WHEN currentContent is PostListContent and is connected is true THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(Refresh, initialContent)
            }

            // THEN
            result shouldBe initialContent.copy(
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )
            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class SetPostsTests {
        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetPosts(mock()), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts is null THEN updated content is returned`() {
            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetPosts(null), initialContent)
            }

            // THEN
            result shouldBe initialContent.copy(posts = null, shouldLoad = Loaded)
        }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts is not null THEN updated content is returned`() {
            // GIVEN
            val mockDiffUtil = mock<PostListDiffUtil>()
            given(mockPostListDiffUtilFactory.create(emptyList(), listOf(createPost())))
                .willReturn(mockDiffUtil)

            val currentContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetPosts(1 to listOf(createPost())), currentContent)
            }

            // THEN
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = PostList(1, listOf(createPost()), mockDiffUtil),
                showDescription = false,
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
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(GetNextPostPage, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and posts is null THEN same content is returned `() {
            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(GetNextPostPage, initialContent)
            }

            // THEN
            result shouldBe initialContent
        }

        @Test
        fun `WHEN currentContent is PostListContent and posts is not null THEN updated content is returned`() {
            // GIVEN
            val currentContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = PostList(1, listOf(mock()), mock()),
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(GetNextPostPage, currentContent)
            }

            // THEN
            result shouldBe currentContent.copy(shouldLoad = ShouldLoadNextPage(offset = 1))
        }
    }

    @Nested
    inner class SetNextPostPageTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetNextPostPage(mock()), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and current content posts is null THEN same content is returned`() {
            // GIVEN
            val content = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(
                    SetNextPostPage(posts = 1 to listOf(createPost())),
                    content
                )
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and action posts is null THEN same content is returned`() {
            // GIVEN
            val content = PostListContent(
                category = All,
                title = mockTitle,
                posts = PostList(1, listOf(createPost()), mock()),
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetNextPostPage(posts = null), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent and posts are not null THEN updated content is returned`() {
            // GIVEN
            val mockCurrentList = listOf(mock<Post>())
            val mockNewList = listOf(mock<Post>())
            val mockDiffUtil = mock<PostListDiffUtil>()

            val currentContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = PostList(1, mockCurrentList, mock()),
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = true
            )

            given(
                mockPostListDiffUtilFactory.create(
                    mockCurrentList,
                    mockCurrentList.plus(mockNewList)
                )
            ).willReturn(mockDiffUtil)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(SetNextPostPage(2 to mockNewList), currentContent)
            }

            // THEN
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = PostList(2, mockCurrentList.plus(mockNewList), mockDiffUtil),
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )
        }
    }

    @Nested
    inner class PostsDisplayedTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(PostsDisplayed, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN the updated content is returned`() {
            // GIVEN
            val mockPostList = PostList(1, mock(), mock(), alreadyDisplayed = false)

            val currentContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = mockPostList,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostsDisplayed, currentContent)
            }

            // THEN
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = mockPostList.copy(alreadyDisplayed = true),
                showDescription = false,
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
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(ToggleSorting, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadFirstPage WHEN currentContent is PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()
            given(content.shouldLoad).willReturn(ShouldLoadFirstPage)

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN shouldLoad is ShouldLoadNextPage WHEN currentContent is PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()
            given(content.shouldLoad).willReturn(ShouldLoadNextPage(0))

            // WHEN
            val result = runBlocking { postActionHandler.runAction(Refresh, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN isConnected is false WHEN currentContent is PostListContent THEN same content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = runBlocking { postActionHandler.runAction(ToggleSorting, initialContent) }

            // THEN
            result shouldBe initialContent.copy(isConnected = false)
        }

        @Test
        fun `GIVEN sortType is NewestFirst WHEN currentContent is PostListContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(
                    ToggleSorting,
                    initialContent.copy(sortType = NewestFirst)
                )
            }

            // THEN
            result shouldBe initialContent.copy(
                sortType = OldestFirst,
                shouldLoad = ShouldLoadFirstPage
            )
        }

        @Test
        fun `GIVEN sortType is OldestFirst WHEN currentContent is PostListContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(
                    ToggleSorting,
                    initialContent.copy(sortType = OldestFirst)
                )
            }

            // THEN
            result shouldBe initialContent.copy(
                sortType = NewestFirst,
                shouldLoad = ShouldLoadFirstPage
            )
        }
    }

    @Nested
    inner class EditPostTests {

        @Test
        fun `WHEN currentContent is not PostDetailContent or PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<Content>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(EditPost(mockPost), content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN updated content is returned`() {
            // GIVEN
            val mockCurrentContent = mock<PostDetailContent>()
            val randomBoolean = randomBoolean()
            given(mockUserRepository.getShowDescriptionInDetails())
                .willReturn(randomBoolean)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(EditPost(mockPost), mockCurrentContent)
            }

            // THEN
            result shouldBe EditPostContent(
                post = mockPost,
                showDescription = randomBoolean,
                previousContent = mockCurrentContent
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN updated content is returned`() {
            // GIVEN
            val mockCurrentContent = mock<PostListContent>()
            val randomBoolean = randomBoolean()
            given(mockUserRepository.getShowDescriptionInDetails())
                .willReturn(randomBoolean)

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(EditPost(mockPost), mockCurrentContent)
            }

            // THEN
            result shouldBe EditPostContent(
                post = mockPost,
                showDescription = randomBoolean,
                previousContent = mockCurrentContent
            )
        }
    }

    @Test
    fun `WHEN editPostFromShare is called THEN EditPostContent is returned`() {
        // GIVEN
        val mockCurrentContent = mock<PostDetailContent>()
        val randomBoolean = randomBoolean()
        given(mockUserRepository.getShowDescriptionInDetails())
            .willReturn(randomBoolean)

        // WHEN
        val result = runBlocking {
            postActionHandler.runAction(EditPostFromShare(mockPost), mockCurrentContent)
        }

        // THEN
        result shouldBe EditPostContent(
            post = mockPost,
            showDescription = randomBoolean,
            previousContent = ExternalContent
        )
    }

    @Nested
    inner class PostSavedTests {

        @Test
        fun `WHEN currentContent is not AddPostContent or EditPostContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostSaved(mockPost), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is AddPostContent THEN updated content is returned`() {
            // GIVEN
            val randomBoolean = randomBoolean()
            val currentContent = AddPostContent(
                showDescription = randomBoolean,
                defaultPrivate = randomBoolean,
                defaultReadLater = randomBoolean,
                previousContent = initialContent
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostSaved(mockPost), currentContent)
            }

            // THEN
            result shouldBe initialContent.copy(shouldLoad = ShouldLoadFirstPage)
        }

        @Test
        fun `GIVEN previousContent is PostDetailContent WHEN currentContent is EditPostContent THEN updated content is returned`() {
            // GIVEN
            val randomBoolean = randomBoolean()
            val postDetail = PostDetailContent(
                post = mockPost,
                previousContent = initialContent
            )
            val currentContent = EditPostContent(
                post = mockPost,
                showDescription = randomBoolean,
                previousContent = postDetail
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostSaved(mockPost), currentContent)
            }

            // THEN
            result shouldBe PostDetailContent(
                post = mockPost,
                previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage)
            )
        }

        @Test
        fun `GIVEN previousContent is not PostDetailContent WHEN currentContent is EditPostContent THEN updated content is returned`() {
            // GIVEN
            val randomBoolean = randomBoolean()
            val currentContent = EditPostContent(
                post = mockPost,
                showDescription = randomBoolean,
                previousContent = ExternalContent
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostSaved(mockPost), currentContent)
            }

            // THEN
            result shouldBe ExternalContent
        }
    }

    @Nested
    inner class PostDeletedTests {

        @Test
        fun `WHEN currentContent is not PostDetailContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { postActionHandler.runAction(PostDeleted, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN updated content is returned`() {
            // GIVEN
            val currentContent = PostDetailContent(
                post = mockPost,
                previousContent = initialContent
            )

            // WHEN
            val result = runBlocking {
                postActionHandler.runAction(PostDeleted, currentContent)
            }

            // THEN
            result shouldBe initialContent.copy(shouldLoad = ShouldLoadFirstPage)
        }
    }
}
