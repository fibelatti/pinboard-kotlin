package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.times

internal class NavigationActionHandlerTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockPostsRepository = mock<PostsRepository>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()
    private val mockIoScope = spy(CoroutineScope(Dispatchers.Unconfined))

    private val navigationActionHandler = spy(
        NavigationActionHandler(
            mockUserRepository,
            mockPostsRepository,
            mockConnectivityInfoProvider,
            mockIoScope
        )
    )

    private val previousContent = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage
    )

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class NavigateBackTests {

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() {
            // GIVEN
            val content = mock<ExternalContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, content)
            }

            // THEN
            result shouldBe content
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN currentContent is ContentWithHistory THEN previousContent is returned`(
            contentWithHistory: ContentWithHistory
        ) {
            // GIVEN
            val returnedContent = when (contentWithHistory) {
                is NoteDetailContent -> mock<NoteListContent>()
                is PopularPostDetailContent -> mock<PopularPostsContent>()
                else -> previousContent
            }

            given(contentWithHistory.previousContent)
                .willReturn(returnedContent)

            val randomBoolean = randomBoolean()
            given(mockUserRepository.getShowDescriptionInLists())
                .willReturn(randomBoolean)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, contentWithHistory)
            }

            // THEN
            if (contentWithHistory is UserPreferencesContent) {
                result shouldBe previousContent.copy(showDescription = randomBoolean)
            } else {
                result shouldBe returnedContent
            }
        }

        fun testCases(): List<ContentWithHistory> =
            mutableListOf<ContentWithHistory>().apply {
                ContentWithHistory::class.allSealedSubclasses
                    .map {
                        add(it.objectInstance ?: Mockito.mock(it.javaObjectType))
                    }
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
        fun `WHEN action is ViewCategory THEN a PostListContent is returned`(category: ViewCategory) {
            // GIVEN
            val randomBoolean = randomBoolean()
            given(mockUserRepository.getShowDescriptionInLists())
                .willReturn(randomBoolean)
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(category, mock()) }

            // THEN
            result shouldBe PostListContent(
                category = category,
                posts = null,
                showDescription = randomBoolean,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = false
            )

            verify(mockConnectivityInfoProvider).isConnected()
        }

        fun testCases(): List<ViewCategory> = ViewCategory::class.sealedSubclasses.map {
            it.objectInstance as ViewCategory
        }
    }

    @Nested
    inner class ViewPostTests {

        private val randomBoolean = randomBoolean()
        private val mockShouldLoad = mock<ShouldLoad>()

        @BeforeEach
        fun setup() {
            willReturn(mockShouldLoad)
                .given(navigationActionHandler).markAsRead(safeAny())
        }

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
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is InAppBrowser THEN PostDetailContent is returned`() {
            // GIVEN
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.InAppBrowser(randomBoolean))

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            verify(navigationActionHandler).markAsRead(safeAny())
            result shouldBe PostDetailContent(
                post = createPost(),
                previousContent = previousContent.copy(shouldLoad = mockShouldLoad)
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() {
            // GIVEN
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.ExternalBrowser(randomBoolean))

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            verify(navigationActionHandler).markAsRead(safeAny())
            result shouldBe ExternalBrowserContent(
                post = createPost(),
                previousContent = previousContent.copy(shouldLoad = mockShouldLoad)
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is Edit THEN EditPostContent is returned`() {
            // GIVEN
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.Edit)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            result shouldBe EditPostContent(
                post = createPost(),
                previousContent = previousContent
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is InAppBrowser THEN PopularPostDetailContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mock<PopularPostsContent>()
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.InAppBrowser(randomBoolean))

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            result shouldBe PopularPostDetailContent(
                post = createPost(),
                previousContent = mockPopularPostsContent
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mock<PopularPostsContent>()
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.ExternalBrowser(randomBoolean))

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            result shouldBe ExternalBrowserContent(
                post = createPost(),
                previousContent = mockPopularPostsContent
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is Edit THEN PopularPostDetailContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mock<PopularPostsContent>()
            given(mockUserRepository.getPreferredDetailsView())
                .willReturn(PreferredDetailsView.Edit)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            result shouldBe PopularPostDetailContent(
                post = createPost(),
                previousContent = mockPopularPostsContent
            )
        }
    }

    @Nested
    inner class MarkAsReadTests {

        private val post = createPost()

        @Test
        fun `WHEN post readLater is false THEN Loaded is returned`() {
            // GIVEN
            val notReadLater = post.copy(readLater = false)

            // WHEN
            val result = navigationActionHandler.markAsRead(notReadLater)

            // THEN
            result shouldBe Loaded
        }

        @Test
        fun `WHEN user repository getMarkAsReadOnOpen returns false THEN Loaded is returned`() {
            // GIVEN
            val readLater = post.copy(readLater = true)
            given(mockUserRepository.getMarkAsReadOnOpen()).willReturn(false)

            // WHEN
            val result = navigationActionHandler.markAsRead(readLater)

            // THEN
            result shouldBe Loaded
        }

        @Test
        fun `WHEN post readLater is false AND user repository getMarkAsReadOnOpen returns true THEN posts repository add is called`() {
            // GIVEN
            val readLater = post.copy(readLater = true)
            given(mockUserRepository.getMarkAsReadOnOpen()).willReturn(true)

            // WHEN
            navigationActionHandler.markAsRead(readLater)

            // THEN
            verifySuspend(mockPostsRepository) {
                add(
                    url = readLater.url,
                    title = readLater.title,
                    description = readLater.description,
                    private = readLater.private,
                    readLater = false,
                    tags = readLater.tags,
                    replace = true
                )
            }
        }

        @Test
        fun `WHEN post readLater is false AND user repository getMarkAsReadOnOpen returns true THEN ShouldLoadFirstPage is returned`() {
            // GIVEN
            val readLater = post.copy(readLater = true)
            given(mockUserRepository.getMarkAsReadOnOpen()).willReturn(true)

            // WHEN
            val result = navigationActionHandler.markAsRead(readLater)

            // THEN
            result shouldBe ShouldLoadFirstPage
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
            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewSearch, previousContent)
            }

            // THEN
            result shouldBe SearchContent(
                previousContent.searchParameters,
                shouldLoadTags = true,
                previousContent = previousContent
            )
        }
    }

    @Nested
    inner class AddPostTests {

        @BeforeEach
        fun setup() {
            given(mockUserRepository.getDefaultPrivate()).willReturn(true)
            given(mockUserRepository.getDefaultReadLater()).willReturn(true)
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
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = true,
                defaultReadLater = true,
                previousContent = previousContent
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            given(mockUserRepository.getDefaultPrivate())
                .willReturn(null)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = false,
                defaultReadLater = true,
                previousContent = previousContent
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            given(mockUserRepository.getDefaultReadLater())
                .willReturn(null)

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            result shouldBe AddPostContent(
                defaultPrivate = true,
                defaultReadLater = false,
                previousContent = previousContent
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

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewTags, previousContent)
            }

            // THEN
            result shouldBe TagListContent(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = previousContent
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

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNotes, previousContent)
            }

            // THEN
            result shouldBe NoteListContent(
                notes = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = previousContent
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
    inner class ViewPopularTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPopular, content)
            }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN PopularPostsContent is returned`() {
            // GIVEN
            val mockCurrentContent = mock<PostListContent>()
            val mockBoolean = randomBoolean()
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(mockBoolean)

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPopular, mockCurrentContent)
            }

            // THEN
            result shouldBe PopularPostsContent(
                posts = emptyList(),
                shouldLoad = mockBoolean,
                isConnected = mockBoolean,
                previousContent = mockCurrentContent
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class ViewPreferencesTests {

        private val mockAppearance = mock<Appearance>()
        private val mockPreferredDetailsView = mock<PreferredDetailsView>()
        private val mockRandomBoolean = randomBoolean()
        private val mockEditAfterSharing = mock<EditAfterSharing>()

        @BeforeEach
        fun setup() {
            given(mockUserRepository.getAppearance()).willReturn(mockAppearance)
            given(mockUserRepository.getPreferredDetailsView()).willReturn(mockPreferredDetailsView)
            given(mockUserRepository.getAutoFillDescription()).willReturn(mockRandomBoolean)
            given(mockUserRepository.getShowDescriptionInLists()).willReturn(mockRandomBoolean)
            given(mockUserRepository.getDefaultPrivate()).willReturn(mockRandomBoolean)
            given(mockUserRepository.getDefaultReadLater()).willReturn(mockRandomBoolean)
            given(mockUserRepository.getEditAfterSharing()).willReturn(mockEditAfterSharing)
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
                runBlocking { navigationActionHandler.runAction(ViewPreferences, previousContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                preferredDetailsView = mockPreferredDetailsView,
                defaultPrivate = mockRandomBoolean,
                autoFillDescription = mockRandomBoolean,
                showDescriptionInLists = mockRandomBoolean,
                defaultReadLater = mockRandomBoolean,
                editAfterSharing = mockEditAfterSharing,
                previousContent = previousContent
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            given(mockUserRepository.getDefaultPrivate())
                .willReturn(null)

            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, previousContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                preferredDetailsView = mockPreferredDetailsView,
                autoFillDescription = mockRandomBoolean,
                showDescriptionInLists = mockRandomBoolean,
                defaultPrivate = false,
                defaultReadLater = mockRandomBoolean,
                editAfterSharing = mockEditAfterSharing,
                previousContent = previousContent
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            given(mockUserRepository.getDefaultReadLater())
                .willReturn(null)

            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, previousContent) }

            // THEN
            result shouldBe UserPreferencesContent(
                appearance = mockAppearance,
                preferredDetailsView = mockPreferredDetailsView,
                autoFillDescription = mockRandomBoolean,
                showDescriptionInLists = mockRandomBoolean,
                defaultPrivate = mockRandomBoolean,
                defaultReadLater = false,
                editAfterSharing = mockEditAfterSharing,
                previousContent = previousContent
            )
        }
    }
}
