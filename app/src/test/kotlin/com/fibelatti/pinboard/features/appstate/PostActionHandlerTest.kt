package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PostActionHandlerTest {

    private val mockUserRepository = mockk<UserRepository>()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val mockPost = mockk<Post>()
    private val canPaginate = randomBoolean()

    private val postActionHandler = PostActionHandler(
        userRepository = mockUserRepository,
        connectivityInfoProvider = mockConnectivityInfoProvider,
    )

    private val initialContent = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = Loaded,
        isConnected = true,
    )

    @Nested
    inner class RefreshTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(Refresh(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and is connected is false THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns false

                // WHEN
                val result = postActionHandler.runAction(Refresh(), initialContent)

                // THEN
                assertThat(result).isEqualTo(
                    initialContent.copy(
                        shouldLoad = Loaded,
                        isConnected = false,
                    ),
                )
                verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
            }

        @Test
        fun `WHEN currentContent is PostListContent and is connected is true THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns true

                // WHEN
                val result = postActionHandler.runAction(Refresh(), initialContent)

                // THEN
                assertThat(result).isEqualTo(
                    initialContent.copy(
                        shouldLoad = ShouldLoadFirstPage,
                        isConnected = true,
                    ),
                )
                verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
            }

        @Test
        fun `WHEN currentContent is PostListContent and is connected is true and force is true THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns true

                // WHEN
                val result = postActionHandler.runAction(Refresh(force = true), initialContent)

                // THEN
                assertThat(result).isEqualTo(
                    initialContent.copy(
                        shouldLoad = ShouldForceLoad,
                        isConnected = true,
                        canForceSync = false,
                    ),
                )
                verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
            }
    }

    @Nested
    inner class SetPostsTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(SetPosts(mockk()), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts has an empty list is not up to date THEN updated content is returned`() =
            runTest {
                // WHEN
                val result = postActionHandler.runAction(
                    SetPosts(
                        PostListResult(
                            totalCount = 0,
                            posts = emptyList(),
                            upToDate = false,
                            canPaginate = canPaginate,
                        ),
                    ),
                    initialContent,
                )

                // THEN
                assertThat(result).isEqualTo(initialContent.copy(posts = null, shouldLoad = Syncing))
            }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts has an empty list is up to date THEN updated content is returned`() =
            runTest {
                // WHEN
                val result = postActionHandler.runAction(
                    SetPosts(
                        PostListResult(
                            totalCount = 0,
                            posts = emptyList(),
                            upToDate = true,
                            canPaginate = canPaginate,
                        ),
                    ),
                    initialContent,
                )

                // THEN
                assertThat(result).isEqualTo(initialContent.copy(posts = null, shouldLoad = Loaded))
            }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts is not null and content is up to date THEN updated content is returned`() =
            runTest {
                // GIVEN
                val currentContent = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = true,
                )

                // WHEN
                val result = postActionHandler.runAction(
                    SetPosts(
                        PostListResult(
                            totalCount = 1,
                            posts = listOf(createPost()),
                            upToDate = true,
                            canPaginate = canPaginate,
                        ),
                    ),
                    currentContent,
                )

                // THEN
                assertThat(result).isEqualTo(
                    PostListContent(
                        category = All,
                        posts = PostList(
                            list = listOf(createPost()),
                            totalCount = 1,
                            canPaginate = canPaginate,
                        ),
                        showDescription = false,
                        sortType = NewestFirst,
                        searchParameters = SearchParameters(),
                        shouldLoad = Loaded,
                        isConnected = true,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PostListContent and actions posts is not null and content is not up to date THEN updated content is returned with syncing`() =
            runTest {
                // GIVEN
                val currentContent = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = true,
                )

                // WHEN
                val result = postActionHandler.runAction(
                    SetPosts(
                        PostListResult(
                            totalCount = 1,
                            posts = listOf(createPost()),
                            upToDate = false,
                            canPaginate = canPaginate,
                        ),
                    ),
                    currentContent,
                )

                // THEN
                assertThat(result).isEqualTo(
                    PostListContent(
                        category = All,
                        posts = PostList(
                            list = listOf(createPost()),
                            totalCount = 1,
                            canPaginate = canPaginate,
                        ),
                        showDescription = false,
                        sortType = NewestFirst,
                        searchParameters = SearchParameters(),
                        shouldLoad = Syncing,
                        isConnected = true,
                    ),
                )
            }
    }

    @Nested
    inner class GetNextPostPageTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(GetNextPostPage, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and posts is null THEN same content is returned `() = runTest {
            // WHEN
            val result = postActionHandler.runAction(GetNextPostPage, initialContent)

            // THEN
            assertThat(result).isEqualTo(initialContent)
        }

        @Test
        fun `WHEN currentContent is PostListContent and posts is not null THEN updated content is returned`() =
            runTest {
                // GIVEN
                val currentContent = PostListContent(
                    category = All,
                    posts = PostList(
                        list = listOf(mockk()),
                        totalCount = 1,
                        canPaginate = canPaginate,
                    ),
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = Loaded,
                    isConnected = true,
                )

                // WHEN
                val result = postActionHandler.runAction(GetNextPostPage, currentContent)

                // THEN
                assertThat(result).isEqualTo(currentContent.copy(shouldLoad = ShouldLoadNextPage(offset = 1)))
            }
    }

    @Nested
    inner class SetNextPostPageTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(SetNextPostPage(mockk()), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and current content posts is null THEN same content is returned`() =
            runTest {
                // GIVEN
                val content = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = Loaded,
                    isConnected = true,
                )

                // WHEN
                val result = postActionHandler.runAction(
                    SetNextPostPage(
                        PostListResult(
                            totalCount = 1,
                            posts = listOf(createPost()),
                            upToDate = randomBoolean(),
                            canPaginate = canPaginate,
                        ),
                    ),
                    content,
                )

                // THEN
                assertThat(result).isEqualTo(content)
            }

        @Test
        fun `WHEN currentContent is PostListContent and posts are not null THEN updated content is returned`() =
            runTest {
                // GIVEN
                val mockCurrentList = listOf(mockk<Post>())
                val mockNewList = listOf(mockk<Post>())

                val currentContent = PostListContent(
                    category = All,
                    posts = PostList(
                        list = mockCurrentList,
                        totalCount = 1,
                        canPaginate = canPaginate,
                    ),
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = true,
                )

                // WHEN
                val result = postActionHandler.runAction(
                    SetNextPostPage(
                        PostListResult(
                            totalCount = 2,
                            posts = mockNewList,
                            upToDate = randomBoolean(),
                            canPaginate = canPaginate,
                        ),
                    ),
                    currentContent,
                )

                // THEN
                assertThat(result).isEqualTo(
                    PostListContent(
                        category = All,
                        posts = PostList(
                            list = mockCurrentList.plus(mockNewList),
                            totalCount = 2,
                            canPaginate = canPaginate,
                        ),
                        showDescription = false,
                        sortType = NewestFirst,
                        searchParameters = SearchParameters(),
                        shouldLoad = Loaded,
                        isConnected = true,
                    ),
                )
            }
    }

    @Nested
    inner class ToggleSortingTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `GIVEN isConnected is false WHEN currentContent is PostListContent THEN same content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns false

                // WHEN
                val result = postActionHandler.runAction(ToggleSorting, initialContent)

                // THEN
                assertThat(result).isEqualTo(initialContent.copy(isConnected = false))
            }

        @Test
        fun `GIVEN sortType is NewestFirst WHEN currentContent is PostListContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns true

                // WHEN
                val result = postActionHandler.runAction(
                    ToggleSorting,
                    initialContent.copy(sortType = NewestFirst),
                )

                // THEN
                assertThat(result).isEqualTo(
                    initialContent.copy(
                        sortType = OldestFirst,
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                )
            }

        @Test
        fun `GIVEN sortType is OldestFirst WHEN currentContent is PostListContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns true

                // WHEN
                val result = postActionHandler.runAction(
                    ToggleSorting,
                    initialContent.copy(sortType = OldestFirst),
                )

                // THEN
                assertThat(result).isEqualTo(
                    initialContent.copy(
                        sortType = NewestFirst,
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                )
            }
    }

    @Nested
    inner class EditPostTests {

        @Test
        fun `WHEN currentContent is not PostDetailContent or PostListContent THEN same content is returned`() =
            runTest {
                // GIVEN
                val content = mockk<Content>()

                // WHEN
                val result = postActionHandler.runAction(EditPost(mockPost), content)

                // THEN
                assertThat(result).isEqualTo(content)
            }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN updated content is returned`() = runTest {
            // GIVEN
            val mockCurrentContent = mockk<PostDetailContent>()

            // WHEN
            val result = postActionHandler.runAction(EditPost(mockPost), mockCurrentContent)

            // THEN
            assertThat(result).isEqualTo(
                EditPostContent(
                    post = mockPost,
                    previousContent = mockCurrentContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN updated content is returned`() = runTest {
            // GIVEN
            val mockCurrentContent = mockk<PostListContent>()

            // WHEN
            val result = postActionHandler.runAction(EditPost(mockPost), mockCurrentContent)

            // THEN
            assertThat(result).isEqualTo(
                EditPostContent(
                    post = mockPost,
                    previousContent = mockCurrentContent,
                ),
            )
        }
    }

    @Test
    fun `WHEN editPostFromShare is called THEN EditPostContent is returned`() = runTest {
        // GIVEN
        val mockCurrentContent = mockk<PostDetailContent>()

        // WHEN
        val result = postActionHandler.runAction(EditPostFromShare(mockPost), mockCurrentContent)

        // THEN
        assertThat(result).isEqualTo(
            EditPostContent(
                post = mockPost,
                previousContent = ExternalContent,
            ),
        )
    }

    @Nested
    inner class PostSavedTests {

        @Test
        fun `WHEN currentContent is not handled specifically THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalBrowserContent>()

            // WHEN
            val result = postActionHandler.runAction(PostSaved(mockPost), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN updated content is returned`() = runTest {
            // GIVEN
            val post = createPost()

            // WHEN
            val result = postActionHandler.runAction(PostSaved(post), initialContent)

            // THEN
            assertThat(result)
                .isEqualTo(initialContent.copy(shouldLoad = ShouldLoadFirstPage))
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN updated content is returned`() = runTest {
            // GIVEN
            val post = createPost()
            val currentContent = PostDetailContent(
                post = post,
                previousContent = initialContent,
            )

            // WHEN
            val result = postActionHandler.runAction(PostSaved(post), currentContent)

            // THEN
            assertThat(result)
                .isEqualTo(currentContent.copy(previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage)))
        }

        @Test
        fun `WHEN currentContent is AddPostContent THEN updated content is returned`() = runTest {
            // GIVEN
            val randomBoolean = randomBoolean()
            val currentContent = AddPostContent(
                defaultPrivate = randomBoolean,
                defaultReadLater = randomBoolean,
                defaultTags = emptyList(),
                previousContent = initialContent,
            )

            // WHEN
            val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

            // THEN
            assertThat(result).isEqualTo(initialContent.copy(shouldLoad = ShouldLoadFirstPage))
        }

        @Test
        fun `GIVEN previousContent is PostDetailContent WHEN currentContent is EditPostContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                val postDetail = PostDetailContent(
                    post = mockPost,
                    previousContent = initialContent,
                )
                val currentContent = EditPostContent(
                    post = mockPost,
                    previousContent = postDetail,
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(
                    PostDetailContent(
                        post = mockPost,
                        previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage),
                    ),
                )
            }

        @Test
        fun `GIVEN previousContent is PostListContent WHEN currentContent is EditPostContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                val currentContent = EditPostContent(
                    post = mockPost,
                    previousContent = initialContent,
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(initialContent.copy(shouldLoad = ShouldLoadFirstPage))
            }

        @Test
        fun `GIVEN previousContent is not PostDetailContent WHEN currentContent is EditPostContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                val currentContent = EditPostContent(
                    post = mockPost,
                    previousContent = ExternalContent,
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(ExternalContent)
            }

        @Test
        fun `WHEN currentContent is PopularPostDetailContent AND getEditAfterSharing is AfterSaving THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.editAfterSharing } returns EditAfterSharing.AfterSaving
                val currentContent = PopularPostDetailContent(
                    post = mockk(),
                    previousContent = PopularPostsContent(
                        posts = mockk(),
                        shouldLoad = false,
                        previousContent = initialContent,
                    ),
                )

                val expectedContent = currentContent.copy(
                    previousContent = currentContent.previousContent.copy(
                        previousContent = currentContent.previousContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
                    ),
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(
                    EditPostContent(
                        post = mockPost,
                        previousContent = expectedContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostDetailContent AND getEditAfterSharing is not AfterSaving THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.editAfterSharing } returns EditAfterSharing.BeforeSaving
                val currentContent = PopularPostDetailContent(
                    post = mockk(),
                    previousContent = PopularPostsContent(
                        posts = mockk(),
                        shouldLoad = false,
                        previousContent = initialContent,
                    ),
                )

                val expectedContent = currentContent.copy(
                    previousContent = currentContent.previousContent.copy(
                        previousContent = currentContent.previousContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
                    ),
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(expectedContent)
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent AND getEditAfterSharing is AfterSaving THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.editAfterSharing } returns EditAfterSharing.AfterSaving
                val currentContent = PopularPostsContent(
                    posts = mockk(),
                    shouldLoad = false,
                    previousContent = initialContent,
                )
                val expectedContent = currentContent.copy(
                    previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage),
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(
                    EditPostContent(
                        post = mockPost,
                        previousContent = expectedContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent AND getEditAfterSharing is BeforeSaving THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.editAfterSharing } returns EditAfterSharing.BeforeSaving
                val currentContent = PopularPostsContent(
                    posts = mockk(),
                    shouldLoad = false,
                    previousContent = initialContent,
                )
                val expectedContent = currentContent.copy(
                    previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage),
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(
                    EditPostContent(
                        post = mockPost,
                        previousContent = expectedContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent AND getEditAfterSharing is SkipEdit THEN updated content is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.editAfterSharing } returns EditAfterSharing.SkipEdit
                val currentContent = PopularPostsContent(
                    posts = mockk(),
                    shouldLoad = false,
                    previousContent = initialContent,
                )
                val expectedContent = currentContent.copy(
                    previousContent = initialContent.copy(shouldLoad = ShouldLoadFirstPage),
                )

                // WHEN
                val result = postActionHandler.runAction(PostSaved(mockPost), currentContent)

                // THEN
                assertThat(result).isEqualTo(expectedContent)
            }
    }

    @Nested
    inner class PostDeletedTests {

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = postActionHandler.runAction(PostDeleted, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN updated content is returned`() = runTest {
            // GIVEN
            val currentContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = true,
            )

            // WHEN
            val result = postActionHandler.runAction(PostDeleted, currentContent)

            // THEN
            assertThat(result).isEqualTo(currentContent.copy(shouldLoad = ShouldLoadFirstPage))
        }

        @Test
        fun `WHEN currentContent is ContentWithHistory and previous content is PostListContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                val previousContent = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = Loaded,
                    isConnected = true,
                )
                val currentContent = PostDetailContent(
                    post = mockPost,
                    previousContent = previousContent,
                )

                // WHEN
                val result = postActionHandler.runAction(PostDeleted, currentContent)

                // THEN
                assertThat(result).isEqualTo(previousContent.copy(shouldLoad = ShouldLoadFirstPage))
            }

        @Test
        fun `WHEN currentContent is ContentWithHistory and previous content is PostDetailContent THEN updated content is returned`() =
            runTest {
                // GIVEN
                val yetPreviousContent = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = Loaded,
                    isConnected = true,
                )
                val previousContent = mockk<PostDetailContent>().also {
                    every { it.previousContent } returns yetPreviousContent
                }
                val currentContent = mockk<EditPostContent>().also {
                    every { it.previousContent } returns previousContent
                }

                // WHEN
                val result = postActionHandler.runAction(PostDeleted, currentContent)

                // THEN
                assertThat(result).isEqualTo(yetPreviousContent.copy(shouldLoad = ShouldLoadFirstPage))
            }

        @Test
        fun `WHEN currentContent is ContentWithHistory and it is not specifically handled THEN previous content is returned`() =
            runTest {
                // GIVEN
                val previousContent = mockk<AddPostContent>()
                val currentContent = mockk<EditPostContent>().also {
                    every { it.previousContent } returns previousContent
                }

                // WHEN
                val result = postActionHandler.runAction(PostDeleted, currentContent)

                // THEN
                assertThat(result).isEqualTo(previousContent)
            }
    }
}
