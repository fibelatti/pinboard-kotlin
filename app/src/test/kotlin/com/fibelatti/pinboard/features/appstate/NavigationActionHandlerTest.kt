package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    private val mockIoScope = spyk(CoroutineScope(Dispatchers.Unconfined))

    private val navigationActionHandler = spyk(
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
            val content = mockk<ExternalContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN currentContent is ContentWithHistory THEN previousContent is returned`(
            contentWithHistory: ContentWithHistory
        ) {
            // GIVEN
            val returnedContent = when (contentWithHistory) {
                is NoteDetailContent -> mockk<NoteListContent>()
                is PopularPostDetailContent -> mockk<PopularPostsContent>()
                else -> previousContent
            }

            every { contentWithHistory.previousContent } returns returnedContent

            val randomBoolean = randomBoolean()
            every { mockUserRepository.getShowDescriptionInLists() } returns randomBoolean

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(NavigateBack, contentWithHistory)
            }

            // THEN
            if (contentWithHistory is UserPreferencesContent) {
                assertThat(result).isEqualTo(previousContent.copy(showDescription = randomBoolean))
            } else {
                assertThat(result).isEqualTo(returnedContent)
            }
        }

        fun testCases(): List<ContentWithHistory> =
            mutableListOf<ContentWithHistory>().apply {
                ContentWithHistory::class.allSealedSubclasses
                    .map { add(it.objectInstance ?: mockk()) }
            }
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
        fun `WHEN action is ViewCategory THEN a PostListContent is returned`(category: ViewCategory) {
            // GIVEN
            val randomBoolean = randomBoolean()
            every { mockUserRepository.getShowDescriptionInLists() } returns randomBoolean
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(category, mockk()) }

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = category,
                    posts = null,
                    showDescription = randomBoolean,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = false
                )
            )

            verify { mockConnectivityInfoProvider.isConnected() }
        }

        fun testCases(): List<ViewCategory> = ViewCategory::class.sealedSubclasses.map {
            it.objectInstance as ViewCategory
        }
    }

    @Nested
    inner class ViewPostTests {

        private val randomBoolean = randomBoolean()
        private val mockShouldLoad = mockk<ShouldLoad>()

        @BeforeEach
        fun setup() {
            every { navigationActionHandler.markAsRead(any()) } returns mockShouldLoad
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is InAppBrowser THEN PostDetailContent is returned`() {
            // GIVEN
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.InAppBrowser(
                randomBoolean
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            verify { navigationActionHandler.markAsRead(any()) }
            assertThat(result).isEqualTo(
                PostDetailContent(
                    post = createPost(),
                    previousContent = previousContent.copy(shouldLoad = mockShouldLoad)
                )
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() {
            // GIVEN
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.ExternalBrowser(
                randomBoolean
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            verify { navigationActionHandler.markAsRead(any()) }
            assertThat(result).isEqualTo(
                ExternalBrowserContent(
                    post = createPost(),
                    previousContent = previousContent.copy(shouldLoad = mockShouldLoad)
                )
            )
        }

        @Test
        fun `WHEN currentContent is PostListContent and PreferredDetailsView is Edit THEN EditPostContent is returned`() {
            // GIVEN
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.Edit

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                EditPostContent(
                    post = createPost(),
                    previousContent = previousContent
                )
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is InAppBrowser THEN PopularPostDetailContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mockk<PopularPostsContent>()
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.InAppBrowser(
                randomBoolean
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PopularPostDetailContent(
                    post = createPost(),
                    previousContent = mockPopularPostsContent
                )
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is ExternalBrowser THEN ExternalBrowserContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mockk<PopularPostsContent>()
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.ExternalBrowser(
                randomBoolean
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                ExternalBrowserContent(
                    post = createPost(),
                    previousContent = mockPopularPostsContent
                )
            )
        }

        @Test
        fun `WHEN currentContent is PopularPostsContent and PreferredDetailsView is Edit THEN PopularPostDetailContent is returned`() {
            // GIVEN
            val mockPopularPostsContent = mockk<PopularPostsContent>()
            every { mockUserRepository.getPreferredDetailsView() } returns PreferredDetailsView.Edit

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPost(createPost()), mockPopularPostsContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PopularPostDetailContent(
                    post = createPost(),
                    previousContent = mockPopularPostsContent
                )
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
            assertThat(result).isEqualTo(Loaded)
        }

        @Test
        fun `WHEN user repository getMarkAsReadOnOpen returns false THEN Loaded is returned`() {
            // GIVEN
            val readLater = post.copy(readLater = true)
            every { mockUserRepository.getMarkAsReadOnOpen() } returns false

            // WHEN
            val result = navigationActionHandler.markAsRead(readLater)

            // THEN
            assertThat(result).isEqualTo(Loaded)
        }

        @Test
        fun `WHEN post readLater is false AND user repository getMarkAsReadOnOpen returns true THEN posts repository add is called`() {
            // GIVEN
            val readLater = post.copy(readLater = true)
            every { mockUserRepository.getMarkAsReadOnOpen() } returns true

            // WHEN
            navigationActionHandler.markAsRead(readLater)

            // THEN
            coVerify {
                mockPostsRepository.add(
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
            every { mockUserRepository.getMarkAsReadOnOpen() } returns true

            // WHEN
            val result = navigationActionHandler.markAsRead(readLater)

            // THEN
            assertThat(result).isEqualTo(ShouldLoadFirstPage)
        }
    }

    @Nested
    inner class ViewSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewSearch, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN SearchContent is returned`() {
            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewSearch, previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    previousContent.searchParameters,
                    shouldLoadTags = true,
                    previousContent = previousContent
                )
            )
        }
    }

    @Nested
    inner class AddPostTests {

        @BeforeEach
        fun setup() {
            every { mockUserRepository.getDefaultPrivate() } returns true
            every { mockUserRepository.getDefaultReadLater() } returns true
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN AddPostContent is returned`() {
            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = true,
                    previousContent = previousContent
                )
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            every { mockUserRepository.getDefaultPrivate() } returns null

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = false,
                    defaultReadLater = true,
                    previousContent = previousContent
                )
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            every { mockUserRepository.getDefaultReadLater() } returns null

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(AddPost, previousContent) }

            // THEN
            assertThat(result).isEqualTo(
                AddPostContent(
                    defaultPrivate = true,
                    defaultReadLater = false,
                    previousContent = previousContent
                )
            )
        }
    }

    @Nested
    inner class ViewTagsTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewTags, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN TagListContent is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewTags, previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = previousContent
                )
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewNotesTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewNotes, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN NoteListContent is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNotes, previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                NoteListContent(
                    notes = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = previousContent
                )
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewNoteTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNote("some-id"), content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN NoteDetailContent is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            val initialContent = NoteListContent(
                notes = emptyList(),
                shouldLoad = false,
                isConnected = true,
                previousContent = mockk()
            )

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewNote("some-id"), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                NoteDetailContent(
                    id = "some-id",
                    note = Either.Left(false),
                    isConnected = false,
                    previousContent = initialContent
                )
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewPopularTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPopular, content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN PopularPostsContent is returned`() {
            // GIVEN
            val mockCurrentContent = mockk<PostListContent>()
            val mockBoolean = randomBoolean()
            every { mockConnectivityInfoProvider.isConnected() } returns mockBoolean

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPopular, mockCurrentContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PopularPostsContent(
                    posts = emptyList(),
                    shouldLoad = mockBoolean,
                    isConnected = mockBoolean,
                    previousContent = mockCurrentContent
                )
            )

            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class ViewPreferencesTests {

        private val mockAppearance = mockk<Appearance>()
        private val mockPreferredDetailsView = mockk<PreferredDetailsView>()
        private val mockRandomBoolean = randomBoolean()
        private val mockEditAfterSharing = mockk<EditAfterSharing>()

        @BeforeEach
        fun setup() {
            every { mockUserRepository.getAppearance() } returns mockAppearance
            every { mockUserRepository.getPreferredDetailsView() } returns mockPreferredDetailsView
            every { mockUserRepository.getAutoFillDescription() } returns mockRandomBoolean
            every { mockUserRepository.getShowDescriptionInLists() } returns mockRandomBoolean
            every { mockUserRepository.getDefaultPrivate() } returns mockRandomBoolean
            every { mockUserRepository.getDefaultReadLater() } returns mockRandomBoolean
            every { mockUserRepository.getEditAfterSharing() } returns mockEditAfterSharing
        }

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { navigationActionHandler.runAction(ViewPreferences, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN UserPreferencesContent is returned`() {
            // WHEN
            val result =
                runBlocking { navigationActionHandler.runAction(ViewPreferences, previousContent) }

            // THEN
            assertThat(result).isEqualTo(
                UserPreferencesContent(
                    appearance = mockAppearance,
                    preferredDetailsView = mockPreferredDetailsView,
                    defaultPrivate = mockRandomBoolean,
                    autoFillDescription = mockRandomBoolean,
                    showDescriptionInLists = mockRandomBoolean,
                    defaultReadLater = mockRandomBoolean,
                    editAfterSharing = mockEditAfterSharing,
                    previousContent = previousContent
                )
            )
        }

        @Test
        fun `WHEN getDefaultPrivate returns null THEN defaultPrivate is set to false`() {
            // GIVEN
            every { mockUserRepository.getDefaultPrivate() } returns null

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPreferences, previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                UserPreferencesContent(
                    appearance = mockAppearance,
                    preferredDetailsView = mockPreferredDetailsView,
                    autoFillDescription = mockRandomBoolean,
                    showDescriptionInLists = mockRandomBoolean,
                    defaultPrivate = false,
                    defaultReadLater = mockRandomBoolean,
                    editAfterSharing = mockEditAfterSharing,
                    previousContent = previousContent
                )
            )
        }

        @Test
        fun `WHEN getDefaultReadLater returns null THEN defaultReadLater is set to false`() {
            // GIVEN
            every { mockUserRepository.getDefaultReadLater() } returns null

            // WHEN
            val result = runBlocking {
                navigationActionHandler.runAction(ViewPreferences, previousContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                UserPreferencesContent(
                    appearance = mockAppearance,
                    preferredDetailsView = mockPreferredDetailsView,
                    autoFillDescription = mockRandomBoolean,
                    showDescriptionInLists = mockRandomBoolean,
                    defaultPrivate = mockRandomBoolean,
                    defaultReadLater = false,
                    editAfterSharing = mockEditAfterSharing,
                    previousContent = previousContent
                )
            )
        }
    }
}
