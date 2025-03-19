package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class NavigationActionHandlerTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val mockSortType = mockk<SortType>()
    private val mockGetPreferredSortType = mockk<GetPreferredSortType> {
        every { this@mockk.invoke() } returns mockSortType
    }

    private val navigationActionHandler = spyk(
        NavigationActionHandler(
            userRepository = mockUserRepository,
            postsRepository = mockPostsRepository,
            connectivityInfoProvider = mockConnectivityInfoProvider,
            getPreferredSortType = mockGetPreferredSortType,
        ),
    )

    private val postListContent = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = mockSortType,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
    )

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class NavigateBackTests {

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN currentContent is ContentWithHistory THEN previousContent is returned`(
            contentWithHistory: ContentWithHistory,
        ) = runTest {
            // GIVEN
            val returnedContent = when (contentWithHistory) {
                is NoteDetailContent -> mockk<NoteListContent>()
                is PopularPostDetailContent -> mockk<PopularPostsContent>()
                else -> postListContent
            }

            every { contentWithHistory.previousContent } returns returnedContent

            val randomBoolean = randomBoolean()
            every { mockUserRepository.showDescriptionInLists } returns randomBoolean

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, contentWithHistory)

            // THEN
            if (contentWithHistory is UserPreferencesContent) {
                assertThat(result).isEqualTo(postListContent.copy(showDescription = randomBoolean))
            } else {
                assertThat(result).isEqualTo(returnedContent)
            }
        }

        fun testCases(): List<ContentWithHistory> = ContentWithHistory::class.allSealedSubclasses
            .map { it.objectInstance ?: mockkClass(it) }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ViewCategoryTest {

        @BeforeEach
        fun setup() {
            clearMocks(mockConnectivityInfoProvider)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN action is ViewCategory THEN a PostListContent is returned`(category: ViewCategory) = runTest {
            // GIVEN
            val randomBoolean = randomBoolean()
            every { mockUserRepository.showDescriptionInLists } returns randomBoolean
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = navigationActionHandler.runAction(category, mockk())

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = category,
                    posts = null,
                    showDescription = randomBoolean,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = false,
                ),
            )

            verify { mockConnectivityInfoProvider.isConnected() }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN previousContent is PostListContent THEN a PostListContent is returned with posts`(
            category: ViewCategory,
        ) = runTest {
            // GIVEN
            val randomBoolean = randomBoolean()
            every { mockUserRepository.showDescriptionInLists } returns randomBoolean
            every { mockConnectivityInfoProvider.isConnected() } returns false

            val postList = mockk<PostList>()
            val previousContent = mockk<PostListContent> {
                every { posts } returns postList
            }

            // WHEN
            val result = navigationActionHandler.runAction(category, previousContent)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = category,
                    posts = postList,
                    showDescription = randomBoolean,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = false,
                ),
            )

            verify { mockConnectivityInfoProvider.isConnected() }
        }

        fun testCases(): List<ViewCategory> = ViewCategory::class.sealedSubclasses.map {
            it.objectInstance as ViewCategory
        }
    }

    @Nested
    inner class ViewPostTests {

        private val markAsReadOnOpen = randomBoolean()
        private val isConnected = randomBoolean()
        private val mockShouldLoad = mockk<ShouldLoad>()

        @BeforeEach
        fun setup() {
            coEvery { navigationActionHandler.markAsRead(any()) } returns mockShouldLoad
            coEvery { mockConnectivityInfoProvider.isConnected() } returns isConnected
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewPost(createPost()), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is InAppBrowser THEN PostDetailContent is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.InAppBrowser(
                    markAsReadOnOpen = markAsReadOnOpen,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), postListContent)

                // THEN
                coVerify { navigationActionHandler.markAsRead(any()) }
                assertThat(result).isEqualTo(
                    PostDetailContent(
                        post = createPost(),
                        isConnected = isConnected,
                        previousContent = postListContent.copy(shouldLoad = mockShouldLoad),
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.ExternalBrowser(
                    markAsReadOnOpen = markAsReadOnOpen,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), postListContent)

                // THEN
                coVerify { navigationActionHandler.markAsRead(any()) }
                assertThat(result).isEqualTo(
                    ExternalBrowserContent(
                        post = createPost(),
                        previousContent = postListContent.copy(shouldLoad = mockShouldLoad),
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is Edit THEN EditPostContent is returned`() =
            runTest {
                // GIVEN
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.Edit

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), postListContent)

                // THEN
                assertThat(result).isEqualTo(
                    EditPostContent(
                        post = createPost(),
                        previousContent = postListContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is InAppBrowser THEN PopularPostDetailContent is returned`() =
            runTest {
                // GIVEN
                val mockPopularPostsContent = mockk<PopularPostsContent>()
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.InAppBrowser(
                    markAsReadOnOpen = markAsReadOnOpen,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)

                // THEN
                assertThat(result).isEqualTo(
                    PopularPostDetailContent(
                        post = createPost(),
                        isConnected = isConnected,
                        previousContent = mockPopularPostsContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() =
            runTest {
                // GIVEN
                val mockPopularPostsContent = mockk<PopularPostsContent>()
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.ExternalBrowser(
                    markAsReadOnOpen = markAsReadOnOpen,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)

                // THEN
                assertThat(result).isEqualTo(
                    ExternalBrowserContent(
                        post = createPost(),
                        previousContent = mockPopularPostsContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is Edit THEN PopularPostDetailContent is returned`() =
            runTest {
                // GIVEN
                val mockPopularPostsContent = mockk<PopularPostsContent>()
                every { mockUserRepository.preferredDetailsView } returns PreferredDetailsView.Edit

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)

                // THEN
                assertThat(result).isEqualTo(
                    PopularPostDetailContent(
                        post = createPost(),
                        isConnected = isConnected,
                        previousContent = mockPopularPostsContent,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN PostDetailContent is returned AND the post is updated`() =
            runTest {
                // GIVEN
                val currentPost = createPost(id = "current")
                val otherPost = createPost(id = "other")

                val content = PostDetailContent(
                    post = currentPost,
                    previousContent = postListContent,
                    isConnected = isConnected,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(otherPost), content)

                // THEN
                assertThat(result).isEqualTo(
                    PostDetailContent(
                        post = otherPost,
                        previousContent = postListContent,
                        isConnected = isConnected,
                    ),
                )
            }

        @Test
        fun `WHEN currentContent is PopularPostDetailContent THEN PopularPostDetailContent is returned AND the post is updated`() =
            runTest {
                // GIVEN
                val currentPost = createPost(id = "current")
                val otherPost = createPost(id = "other")
                val previousContent = mockk<PopularPostsContent>()

                val content = PopularPostDetailContent(
                    post = currentPost,
                    previousContent = previousContent,
                    isConnected = isConnected,
                )

                // WHEN
                val result = navigationActionHandler.runAction(ViewPost(otherPost), content)

                // THEN
                assertThat(result).isEqualTo(
                    PopularPostDetailContent(
                        post = otherPost,
                        previousContent = previousContent,
                        isConnected = isConnected,
                    ),
                )
            }
    }

    @Nested
    inner class MarkAsReadTests {

        private val post = createPost()

        @Test
        fun `WHEN post readLater is false THEN Loaded is returned`() = runTest {
            // GIVEN
            val notReadLater = post.copy(readLater = false)

            // WHEN
            val result = navigationActionHandler.markAsRead(notReadLater)

            // THEN
            assertThat(result).isEqualTo(Loaded)
        }

        @Test
        fun `WHEN user repository getMarkAsReadOnOpen returns false THEN Loaded is returned`() = runTest {
            // GIVEN
            val readLater = post.copy(readLater = true)
            every { mockUserRepository.markAsReadOnOpen } returns false

            // WHEN
            val result = navigationActionHandler.markAsRead(readLater)

            // THEN
            assertThat(result).isEqualTo(Loaded)
        }

        @Test
        fun `WHEN post readLater is false AND user repository getMarkAsReadOnOpen returns true THEN posts repository add is called`() =
            runTest {
                // GIVEN
                val readLater = post.copy(readLater = true)
                every { mockUserRepository.markAsReadOnOpen } returns true

                // WHEN
                navigationActionHandler.markAsRead(readLater)

                // THEN
                coVerify { mockPostsRepository.add(readLater.copy(readLater = false)) }
            }

        @Test
        fun `WHEN post readLater is false AND user repository getMarkAsReadOnOpen returns true THEN ShouldLoadFirstPage is returned`() =
            runTest {
                // GIVEN
                val readLater = post.copy(readLater = true)
                every { mockUserRepository.markAsReadOnOpen } returns true

                // WHEN
                val result = navigationActionHandler.markAsRead(readLater)

                // THEN
                assertThat(result).isEqualTo(ShouldLoadFirstPage)
            }
    }

    @Nested
    inner class ViewSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN SearchContent is returned`() = runTest {
            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    postListContent.searchParameters,
                    shouldLoadTags = true,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN SearchContent is returned`() = runTest {
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, content)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    postListContent.searchParameters,
                    shouldLoadTags = true,
                    previousContent = postListContent,
                ),
            )
        }
    }

    @Nested
    inner class AddPostTests {

        private val mockTags = mockk<List<Tag>>()

        @BeforeEach
        fun setup() {
            every { mockUserRepository.defaultPrivate } returns true
            every { mockUserRepository.defaultReadLater } returns true
            every { mockUserRepository.defaultTags } returns mockTags
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN AddPostContent is returned`() = runTest {
            // WHEN
            val result = navigationActionHandler.runAction(AddPost, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = true,
                    defaultTags = mockTags,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() = runTest {
            // GIVEN
            every { mockUserRepository.defaultPrivate } returns null

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = false,
                    defaultReadLater = true,
                    defaultTags = mockTags,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() = runTest {
            // GIVEN
            every { mockUserRepository.defaultReadLater } returns null

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = false,
                    defaultTags = mockTags,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN AddPostContent is returned`() = runTest {
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, content)

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = true,
                    defaultTags = mockTags,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `GIVEN getDefaultPrivate returns null WHEN currentContent is PostDetailContent THEN defaultPrivate is set to false`() =
            runTest {
                // GIVEN
                every { mockUserRepository.defaultPrivate } returns null

                val content = mockk<PostDetailContent> {
                    every { previousContent } returns postListContent
                }

                // WHEN
                val result = navigationActionHandler.runAction(AddPost, content)

                // THEN
                assertThat(result).isEqualTo(
                    AddPostContent(
                        defaultPrivate = false,
                        defaultReadLater = true,
                        defaultTags = mockTags,
                        previousContent = postListContent,
                    ),
                )
            }

        @Test
        fun `GIVEN getDefaultReadLater returns null WHEN currentContent is PostDetailContent THEN defaultReadLater is set to false`() =
            runTest {
                // GIVEN
                every { mockUserRepository.defaultReadLater } returns null

                val content = mockk<PostDetailContent> {
                    every { previousContent } returns postListContent
                }

                // WHEN
                val result = navigationActionHandler.runAction(AddPost, content)

                // THEN
                assertThat(result).isEqualTo(
                    AddPostContent(
                        defaultPrivate = true,
                        defaultReadLater = false,
                        defaultTags = mockTags,
                        previousContent = postListContent,
                    ),
                )
            }
    }

    @Nested
    inner class ViewTagsTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN TagListContent is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = postListContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN TagListContent is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, content)

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = postListContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewSavedFiltersTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewSavedFilters, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN TagListContent is returned`() = runTest {
            // WHEN
            val result = navigationActionHandler.runAction(ViewSavedFilters, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                SavedFiltersContent(
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN TagListContent is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewSavedFilters, content)

            // THEN
            assertThat(result).isEqualTo(
                SavedFiltersContent(
                    previousContent = postListContent,
                ),
            )
        }
    }

    @Nested
    inner class ViewNotesTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewNotes, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN NoteListContent is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = navigationActionHandler.runAction(ViewNotes, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                NoteListContent(
                    notes = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = postListContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN NoteListContent is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewNotes, content)

            // THEN
            assertThat(result).isEqualTo(
                NoteListContent(
                    notes = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = postListContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewNoteTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewNote("some-id"), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN NoteDetailContent is returned`() = runTest {
            // GIVEN
            val isConnected = randomBoolean()
            every { mockConnectivityInfoProvider.isConnected() } returns isConnected

            val initialContent = NoteListContent(
                notes = emptyList(),
                shouldLoad = false,
                isConnected = true,
                previousContent = mockk(),
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewNote("some-id"), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                NoteDetailContent(
                    id = "some-id",
                    note = Either.Left(isConnected),
                    isConnected = isConnected,
                    previousContent = initialContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is NoteDetailContent THEN NoteDetailContent is returned AND the id is updated`() =
            runTest {
                val isConnected = randomBoolean()
                every { mockConnectivityInfoProvider.isConnected() } returns isConnected

                val noteId = "note-id"
                val otherId = "other-id"
                val noteListContent = mockk<NoteListContent>()

                val content = NoteDetailContent(
                    id = noteId,
                    note = mockk(),
                    previousContent = noteListContent,
                    isConnected = isConnected,
                )

                val result = navigationActionHandler.runAction(ViewNote(otherId), content)

                assertThat(result).isEqualTo(
                    NoteDetailContent(
                        id = otherId,
                        note = Either.Left(isConnected),
                        isConnected = isConnected,
                        previousContent = noteListContent,
                    ),
                )
            }
    }

    @Nested
    inner class ViewPopularTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewPopular, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN PopularPostsContent is returned`() = runTest {
            // GIVEN
            val mockCurrentContent = mockk<PostListContent>()
            val mockBoolean = randomBoolean()
            every { mockConnectivityInfoProvider.isConnected() } returns mockBoolean

            // WHEN
            val result = navigationActionHandler.runAction(ViewPopular, mockCurrentContent)

            // THEN
            assertThat(result).isEqualTo(
                PopularPostsContent(
                    posts = emptyMap(),
                    shouldLoad = mockBoolean,
                    isConnected = mockBoolean,
                    previousContent = mockCurrentContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN PopularPostsContent is returned`() = runTest {
            // GIVEN
            val mockBoolean = randomBoolean()
            every { mockConnectivityInfoProvider.isConnected() } returns mockBoolean

            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewPopular, content)

            // THEN
            assertThat(result).isEqualTo(
                PopularPostsContent(
                    posts = emptyMap(),
                    shouldLoad = mockBoolean,
                    isConnected = mockBoolean,
                    previousContent = postListContent,
                ),
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewAccountSwitcherTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewAccountSwitcher, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN AccountSwitcherContent is returned`() = runTest {
            // GIVEN
            val mockCurrentContent = mockk<PostListContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewAccountSwitcher, mockCurrentContent)

            // THEN
            assertThat(result).isEqualTo(
                AccountSwitcherContent(
                    previousContent = mockCurrentContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN AccountSwitcherContent is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewAccountSwitcher, content)

            // THEN
            assertThat(result).isEqualTo(
                AccountSwitcherContent(
                    previousContent = postListContent,
                ),
            )
        }
    }

    @Nested
    inner class AddAccountTests {

        @Test
        fun `WHEN currentContent is not AccountSwitcherContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()
            val appMode = mockk<AppMode>()

            // WHEN
            val result = navigationActionHandler.runAction(AddAccount(appMode = appMode), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is AccountSwitcherContent THEN LoginContent is returned`() = runTest {
            // GIVEN
            val content = mockk<AccountSwitcherContent>()
            val appMode = mockk<AppMode>()

            // WHEN
            val result = navigationActionHandler.runAction(AddAccount(appMode = appMode), content)

            // THEN
            assertThat(result).isEqualTo(
                LoginContent(
                    appMode = appMode,
                    previousContent = content,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN AccountSwitcherContent is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // WHEN
            val result = navigationActionHandler.runAction(ViewAccountSwitcher, content)

            // THEN
            assertThat(result).isEqualTo(
                AccountSwitcherContent(
                    previousContent = postListContent,
                ),
            )
        }
    }

    @Nested
    inner class ViewPreferencesTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewPreferences, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN UserPreferencesContent is returned`() = runTest {
            // GIVEN
            val currentPreferences = mockk<UserPreferences>()
            every { mockUserRepository.currentPreferences } returns MutableStateFlow(currentPreferences)

            // WHEN
            val result = navigationActionHandler.runAction(ViewPreferences, postListContent)

            // THEN
            assertThat(result).isEqualTo(
                UserPreferencesContent(
                    userPreferences = currentPreferences,
                    previousContent = postListContent,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN UserPreferencesContent is returned`() = runTest {
            val content = mockk<PostDetailContent> {
                every { previousContent } returns postListContent
            }

            // GIVEN
            val currentPreferences = mockk<UserPreferences>()
            every { mockUserRepository.currentPreferences } returns MutableStateFlow(currentPreferences)

            // WHEN
            val result = navigationActionHandler.runAction(ViewPreferences, content)

            // THEN
            assertThat(result).isEqualTo(
                UserPreferencesContent(
                    userPreferences = currentPreferences,
                    previousContent = postListContent,
                ),
            )
        }
    }
}
