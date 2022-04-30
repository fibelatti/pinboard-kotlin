package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.core.test.extension.mock
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AppStateDataSourceTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true) {
        every { hasAuthToken() } returns true
    }
    private val mockNavigationActionHandler = mockk<NavigationActionHandler>()
    private val mockPostActionHandler = mockk<PostActionHandler>()
    private val mockSearchActionHandler = mockk<SearchActionHandler>()
    private val mockTagActionHandler = mockk<TagActionHandler>()
    private val mockNoteActionHandler = mockk<NoteActionHandler>()
    private val mockPopularActionHandler = mockk<PopularActionHandler>()
    private val singleRunner = SingleRunner()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns false
    }

    private val appStateDataSource: AppStateDataSource = spyk(
        AppStateDataSource(
            mockUserRepository,
            mockNavigationActionHandler,
            mockPostActionHandler,
            mockSearchActionHandler,
            mockTagActionHandler,
            mockNoteActionHandler,
            mockPopularActionHandler,
            singleRunner,
            mockConnectivityInfoProvider,
        )
    )

    private val expectedLoginInitialValue = LoginContent()

    private val expectedInitialValue = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = false,
    )

    @Test
    fun `AppStateDataSource initial value should be set`() = runTest {
        assertThat(appStateDataSource.getContent().first()).isEqualTo(expectedInitialValue)
    }

    @Test
    fun `reset should set currentContent to the initial value`() = runTest {
        // GIVEN
        appStateDataSource.updateContent(mock())

        // WHEN
        appStateDataSource.reset()

        // THEN
        assertThat(appStateDataSource.getContent().first()).isEqualTo(expectedInitialValue)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RunActionTests {

        @BeforeEach
        fun setup() {
            appStateDataSource.reset()
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN runAction is called THEN expected action handler is called`(testCase: Pair<Action, ExpectedHandler>) =
            runTest {
                // GIVEN
                coEvery { mockNavigationActionHandler.runAction(any(), any()) } returns expectedInitialValue
                coEvery { mockPostActionHandler.runAction(any(), any()) } returns expectedInitialValue
                coEvery { mockSearchActionHandler.runAction(any(), any()) } returns expectedInitialValue
                coEvery { mockTagActionHandler.runAction(any(), any()) } returns expectedInitialValue
                coEvery { mockNoteActionHandler.runAction(any(), any()) } returns expectedInitialValue
                coEvery { mockPopularActionHandler.runAction(any(), any()) } returns expectedInitialValue

                val (action, expectedHandler) = testCase

                // WHEN
                appStateDataSource.runAction(action)

                // THEN
                when (expectedHandler) {
                    ExpectedHandler.NONE -> {
                        when (action) {
                            is UserLoggedIn -> {
                                assertThat(appStateDataSource.getContent().first()).isEqualTo(expectedInitialValue)
                            }
                            is UserLoggedOut -> {
                                assertThat(appStateDataSource.getContent().first()).isEqualTo(expectedLoginInitialValue)
                                verify { mockUserRepository.clearAuthToken() }
                            }
                            is UserUnauthorized -> {
                                assertThat(appStateDataSource.getContent().first())
                                    .isEqualTo(LoginContent(isUnauthorized = true))
                                verify { mockUserRepository.clearAuthToken() }
                            }
                            else -> fail { "Action should be assigned to a handler" }
                        }
                    }
                    ExpectedHandler.NAVIGATION -> {
                        if (action is NavigationAction) {
                            coVerify { mockNavigationActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.POST -> {
                        if (action is PostAction) {
                            coVerify { mockPostActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.SEARCH -> {
                        if (action is SearchAction) {
                            coVerify { mockSearchActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.TAG -> {
                        if (action is TagAction) {
                            coVerify { mockTagActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.NOTE -> {
                        if (action is NoteAction) {
                            coVerify { mockNoteActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.POPULAR -> {
                        if (action is PopularAction) {
                            coVerify { mockPopularActionHandler.runAction(action, expectedInitialValue) }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                }.let { } // to make it exhaustive
            }

        fun testCases(): List<Pair<Action, ExpectedHandler>> = Action::class.allSealedSubclasses.map {
            when (it.objectInstance ?: mockkClass(it)) {
                // Auth
                UserLoggedIn -> UserLoggedIn to ExpectedHandler.NONE
                UserLoggedOut -> UserLoggedOut to ExpectedHandler.NONE
                UserUnauthorized -> UserUnauthorized to ExpectedHandler.NONE

                // Navigation
                NavigateBack -> NavigateBack to ExpectedHandler.NAVIGATION
                All -> All to ExpectedHandler.NAVIGATION
                Recent -> Recent to ExpectedHandler.NAVIGATION
                Public -> Public to ExpectedHandler.NAVIGATION
                Private -> Private to ExpectedHandler.NAVIGATION
                Unread -> Unread to ExpectedHandler.NAVIGATION
                Untagged -> Untagged to ExpectedHandler.NAVIGATION
                is ViewPost -> mockk<ViewPost>() to ExpectedHandler.NAVIGATION
                ViewSearch -> ViewSearch to ExpectedHandler.NAVIGATION
                AddPost -> AddPost to ExpectedHandler.NAVIGATION
                ViewTags -> ViewTags to ExpectedHandler.NAVIGATION
                ViewNotes -> ViewNotes to ExpectedHandler.NAVIGATION
                is ViewNote -> mockk<ViewNote>() to ExpectedHandler.NAVIGATION
                ViewPopular -> ViewPopular to ExpectedHandler.NAVIGATION
                ViewPreferences -> ViewPreferences to ExpectedHandler.NAVIGATION

                // Post
                is Refresh -> mockk<Refresh>() to ExpectedHandler.POST
                is SetPosts -> mockk<SetPosts>() to ExpectedHandler.POST
                GetNextPostPage -> GetNextPostPage to ExpectedHandler.POST
                is SetNextPostPage -> mockk<SetNextPostPage>() to ExpectedHandler.POST
                PostsDisplayed -> PostsDisplayed to ExpectedHandler.POST
                ToggleSorting -> ToggleSorting to ExpectedHandler.POST
                is EditPost -> mockk<EditPost>() to ExpectedHandler.POST
                is EditPostFromShare -> mockk<EditPostFromShare>() to ExpectedHandler.POST
                is PostSaved -> mockk<PostSaved>() to ExpectedHandler.POST
                PostDeleted -> PostDeleted to ExpectedHandler.POST

                // Search
                RefreshSearchTags -> RefreshSearchTags to ExpectedHandler.SEARCH
                is SetTerm -> mockk<SetTerm>() to ExpectedHandler.SEARCH
                is SetSearchTags -> mockk<SetSearchTags>() to ExpectedHandler.SEARCH
                is AddSearchTag -> mockk<AddSearchTag>() to ExpectedHandler.SEARCH
                is RemoveSearchTag -> mockk<RemoveSearchTag>() to ExpectedHandler.SEARCH
                is Search -> mockk<Search>() to ExpectedHandler.SEARCH
                ClearSearch -> ClearSearch to ExpectedHandler.SEARCH

                // Tag
                RefreshTags -> RefreshTags to ExpectedHandler.TAG
                is SetTags -> mockk<SetTags>() to ExpectedHandler.TAG
                is PostsForTag -> mockk<PostsForTag>() to ExpectedHandler.TAG

                // Notes
                RefreshNotes -> RefreshNotes to ExpectedHandler.NOTE
                is SetNotes -> mockk<SetNotes>() to ExpectedHandler.NOTE
                is SetNote -> mockk<SetNote>() to ExpectedHandler.NOTE

                // Popular
                RefreshPopular -> RefreshPopular to ExpectedHandler.POPULAR
                is SetPopularPosts -> mockk<SetPopularPosts>() to ExpectedHandler.POPULAR
            }
        }
    }

    @Nested
    inner class DuplicateContentTests {

        @Test
        fun `WHEN NavigationActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<NavigationAction>()
            coEvery {
                mockNavigationActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN PostActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<PostAction>()
            coEvery {
                mockPostActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN SearchActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<SearchAction>()
            coEvery {
                mockSearchActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN TagActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<TagAction>()
            coEvery {
                mockTagActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN NoteActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<NoteAction>()
            coEvery {
                mockNoteActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN PopularActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<PopularAction>()
            coEvery {
                mockPopularActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }
    }

    @Test
    fun `GIVEN hasAuthToken is false WHEN getInitialContent is called THEN expected initial content is returned`() {
        coEvery { mockUserRepository.hasAuthToken() } returns false

        assertThat(appStateDataSource.getInitialContent()).isEqualTo(expectedLoginInitialValue)
    }

    @Test
    fun `GIVEN hasAuthToken is true WHEN getInitialContent is called THEN expected initial content is returned`() {
        coEvery { mockUserRepository.hasAuthToken() } returns true

        assertThat(appStateDataSource.getInitialContent()).isEqualTo(expectedInitialValue)
    }

    @Test
    fun `GIVEN updateContent is called THEN getContent should return that value`() = runTest {
        // GIVEN
        val mockContent = mockk<Content>()

        // WHEN
        appStateDataSource.updateContent(mockContent)

        // THEN
        assertThat(appStateDataSource.getContent().first()).isEqualTo(mockContent)
    }

    internal enum class ExpectedHandler {
        NONE,
        NAVIGATION,
        POST,
        SEARCH,
        TAG,
        NOTE,
        POPULAR,
    }
}
