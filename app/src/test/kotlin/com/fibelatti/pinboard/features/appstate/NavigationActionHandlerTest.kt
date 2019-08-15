package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.reset
import org.mockito.Mockito.times

internal class NavigationActionHandlerTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val navigationActionHandler = NavigationActionHandler(
        mockUserRepository,
        mockResourceProvider,
        mockConnectivityInfoProvider
    )

    @Nested
    inner class NavigateBackTests {

        private val previousContent = PostListContent(
            category = All,
            title = mockTitle,
            posts = null,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage
        )

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN previousContent is returned`() {
            // GIVEN
            val mockPostDetail = mock<PostDetailContent>()
            given(mockPostDetail.previousContent).willReturn(previousContent)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, mockPostDetail)
            }

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN previousContent is returned`() {
            // GIVEN
            val mockSearchView = mock<SearchContent>()
            given(mockSearchView.previousContent).willReturn(previousContent)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, mockSearchView)
            }

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is AddPostContent THEN previousContent is returned`() {
            // GIVEN
            val mockAddPostContent = mock<AddPostContent>()
            given(mockAddPostContent.previousContent).willReturn(previousContent)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, mockAddPostContent)
            }

            // THEN
            result shouldBe previousContent
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ViewCategoryTest {

        @BeforeEach
        fun setup() {
            reset(mockConnectivityInfoProvider)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN action is ViewCategory THEN a PostListContent is returned`(testCase: Triple<ViewCategory, Int, String>) {
            // GIVEN
            val (category, stringId, resolvedString) = testCase
            given(mockResourceProvider.getString(stringId))
                .willReturn(resolvedString)
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(category, mock()) }

            // THEN
            result shouldBe PostListContent(
                category = category,
                title = resolvedString,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = false
            )

            verify(mockConnectivityInfoProvider).isConnected()
        }

        fun testCases(): List<Triple<ViewCategory, Int, String>> =
            mutableListOf<Triple<ViewCategory, Int, String>>().apply {
                ViewCategory::class.sealedSubclasses.map { it.objectInstance as ViewCategory }
                    .forEach { category ->
                        when (category) {
                            All -> add(
                                Triple(
                                    category,
                                    R.string.posts_title_all,
                                    "R.string.posts_title_all"
                                )
                            )
                            Recent -> add(
                                Triple(
                                    category,
                                    R.string.posts_title_recent,
                                    "R.string.posts_title_recent"
                                )
                            )
                            Public -> add(
                                Triple(
                                    category,
                                    R.string.posts_title_public,
                                    "R.string.posts_title_public"
                                )
                            )
                            Private -> add(
                                Triple(
                                    category,
                                    R.string.posts_title_private,
                                    "R.string.posts_title_private"
                                )
                            )
                            Unread -> add(
                                Triple(
                                    category,
                                    R.string.posts_title_unread,
                                    "R.string.posts_title_unread"
                                )
                            )
                            Untagged -> {
                                add(
                                    Triple(
                                        category,
                                        R.string.posts_title_untagged,
                                        "R.string.posts_title_untagged"
                                    )
                                )
                            }
                        }.let { }
                    }
            }
    }

    @Nested
    inner class ViewPostTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN PostDetailContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), initialContent)
            }

            // THEN
            result shouldBe PostDetailContent(post = createPost(), previousContent = initialContent)
        }
    }

    @Nested
    inner class ViewSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewSearch, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN SearchContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewSearch, initialContent)
            }

            // THEN
            result shouldBe SearchContent(
                initialContent.searchParameters,
                shouldLoadTags = true,
                previousContent = initialContent
            )
        }
    }

    @Nested
    inner class AddPostTests {

        private val initialContent = PostListContent(
            category = All,
            title = mockTitle,
            posts = null,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage
        )

        @BeforeEach
        fun setup() {
            givenSuspend { mockUserRepository.getDefaultPrivate() }.willReturn(true)
            givenSuspend { mockUserRepository.getDefaultReadLater() }.willReturn(true)
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN AddPostContent is returned`() {
            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, initialContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = true,
                defaultReadLater = true,
                previousContent = initialContent
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            givenSuspend { mockUserRepository.getDefaultPrivate() }
                .willReturn(null)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, initialContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = false,
                defaultReadLater = true,
                previousContent = initialContent
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            givenSuspend { mockUserRepository.getDefaultReadLater() }
                .willReturn(null)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, initialContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = true,
                defaultReadLater = false,
                previousContent = initialContent
            )
        }
    }

    @Nested
    inner class ViewTagsTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewTags, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN TagListContent is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewTags, initialContent)
            }

            // THEN
            result shouldBe TagListContent(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = initialContent
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class ViewNotesTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewNotes, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN NoteListContent is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNotes, initialContent)
            }

            // THEN
            result shouldBe NoteListContent(
                notes = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = initialContent
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class ViewNoteTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNote("some-id"), content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN NoteDetailContent is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            val initialContent = NoteListContent(
                notes = emptyList(),
                shouldLoad = false,
                isConnected = true,
                previousContent = mock()
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNote("some-id"), initialContent)
            }

            // THEN
            result shouldBe NoteDetailContent(
                id = "some-id",
                note = Either.Left(false),
                isConnected = false,
                previousContent = initialContent
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class ViewPreferencesTests {

        private val initialContent = PostListContent(
            category = All,
            title = mockTitle,
            posts = null,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage
        )
        private val mockAppearance = mock<Appearance>()

        @BeforeEach
        fun setup() {
            givenSuspend { mockUserRepository.getAppearance() }.willReturn(mockAppearance)
            givenSuspend { mockUserRepository.getDefaultPrivate() }.willReturn(true)
            givenSuspend { mockUserRepository.getDefaultReadLater() }.willReturn(true)
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewPreferences, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN UserPreferencesContent is returned`() {
            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, initialContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                defaultPrivate = true,
                defaultReadLater = true,
                previousContent = initialContent
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            givenSuspend { mockUserRepository.getDefaultPrivate() }
                .willReturn(null)

            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, initialContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                defaultPrivate = false,
                defaultReadLater = true,
                previousContent = initialContent
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            givenSuspend { mockUserRepository.getDefaultReadLater() }
                .willReturn(null)

            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, initialContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                defaultPrivate = true,
                defaultReadLater = false,
                previousContent = initialContent
            )
        }
    }
}
