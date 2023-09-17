package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AppStateDataSourceTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)

    private val mockNavigationActionHandler = mockk<NavigationActionHandler>()
    private val mockPostActionHandler = mockk<PostActionHandler>()
    private val mockSearchActionHandler = mockk<SearchActionHandler>()
    private val mockTagActionHandler = mockk<TagActionHandler>()
    private val mockNoteActionHandler = mockk<NoteActionHandler>()
    private val mockPopularActionHandler = mockk<PopularActionHandler>()

    private val handlers: Map<Class<out Action>, ActionHandler<*>> = mapOf(
        NavigationAction::class.java to mockNavigationActionHandler,
        PostAction::class.java to mockPostActionHandler,
        SearchAction::class.java to mockSearchActionHandler,
        TagAction::class.java to mockTagActionHandler,
        NoteAction::class.java to mockNoteActionHandler,
        PopularAction::class.java to mockPopularActionHandler,
    )

    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val appStateDataSource by lazy {
        AppStateDataSource(
            userRepository = mockUserRepository,
            actionHandlers = handlers,
            connectivityInfoProvider = mockConnectivityInfoProvider,
            scope = TestScope(UnconfinedTestDispatcher()),
            sharingStarted = SharingStarted.WhileSubscribed(),
        )
    }

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

    private val expectedPostActionValue = expectedInitialValue.copy(
        category = Recent,
    )

    @Test
    fun `AppStateDataSource initial value should be set`() = runTest {
        every { mockUserRepository.hasAuthToken() } returns true
        every { mockConnectivityInfoProvider.isConnected() } returns false

        assertThat(appStateDataSource.content.first()).isEqualTo(expectedInitialValue)
    }

    @Test
    fun `reset should set currentContent to the initial value`() = runTest {
        // GIVEN
        every { mockUserRepository.hasAuthToken() } returns true
        every { mockConnectivityInfoProvider.isConnected() } returns false
        appStateDataSource.runAction(mockk<NavigationAction>())

        // WHEN
        appStateDataSource.reset()

        // THEN
        assertThat(appStateDataSource.content.first()).isEqualTo(expectedInitialValue)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RunActionTests {

        @BeforeEach
        fun setup() = runTest {
            every { mockUserRepository.hasAuthToken() } returns true
            every { mockConnectivityInfoProvider.isConnected() } returns false
            coEvery { mockNavigationActionHandler.runAction(any(), any()) } returns expectedPostActionValue
            coEvery { mockPostActionHandler.runAction(any(), any()) } returns expectedPostActionValue
            coEvery { mockSearchActionHandler.runAction(any(), any()) } returns expectedPostActionValue
            coEvery { mockTagActionHandler.runAction(any(), any()) } returns expectedPostActionValue
            coEvery { mockNoteActionHandler.runAction(any(), any()) } returns expectedPostActionValue
            coEvery { mockPopularActionHandler.runAction(any(), any()) } returns expectedPostActionValue
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN runAction is called THEN expected action handler is called`(
            testCase: Pair<Action, ExpectedHandler>,
        ) = runUnconfinedTest {
            // GIVEN
            val (action, expectedHandler) = testCase
            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(action)

            assertThat(result).isNotEmpty()

            // THEN
            when (expectedHandler) {
                ExpectedHandler.NONE -> {
                    when (action) {
                        is UserLoggedIn -> {
                            assertThat(result.last()).isEqualTo(expectedInitialValue)
                        }
                        is UserLoggedOut -> {
                            assertThat(result.last()).isEqualTo(expectedLoginInitialValue)
                            verify { mockUserRepository.clearAuthToken() }
                        }
                        is UserUnauthorized -> {
                            assertThat(result.last()).isEqualTo(LoginContent(isUnauthorized = true))
                            verify { mockUserRepository.clearAuthToken() }
                        }
                        else -> fail { "Action should be assigned to a handler" }
                    }
                }
                ExpectedHandler.NAVIGATION -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is NavigationAction)
                    coVerify { mockNavigationActionHandler.runAction(action, expectedInitialValue) }
                }
                ExpectedHandler.POST -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is PostAction)
                    coVerify { mockPostActionHandler.runAction(action, expectedInitialValue) }
                }
                ExpectedHandler.SEARCH -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is SearchAction)
                    coVerify { mockSearchActionHandler.runAction(action, expectedInitialValue) }
                }
                ExpectedHandler.TAG -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is TagAction)
                    coVerify { mockTagActionHandler.runAction(action, expectedInitialValue) }
                }
                ExpectedHandler.NOTE -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is NoteAction)
                    coVerify { mockNoteActionHandler.runAction(action, expectedInitialValue) }
                }
                ExpectedHandler.POPULAR -> {
                    assertThat(result.last()).isEqualTo(expectedPostActionValue)
                    require(action is PopularAction)
                    coVerify { mockPopularActionHandler.runAction(action, expectedInitialValue) }
                }
            }
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
                ViewSavedFilters -> ViewSavedFilters to ExpectedHandler.NAVIGATION
                ViewNotes -> ViewNotes to ExpectedHandler.NAVIGATION
                is ViewNote -> mockk<ViewNote>() to ExpectedHandler.NAVIGATION
                ViewPopular -> ViewPopular to ExpectedHandler.NAVIGATION
                ViewPreferences -> ViewPreferences to ExpectedHandler.NAVIGATION

                // Post
                is Refresh -> mockk<Refresh>() to ExpectedHandler.POST
                is SetPosts -> mockk<SetPosts>() to ExpectedHandler.POST
                GetNextPostPage -> GetNextPostPage to ExpectedHandler.POST
                is SetNextPostPage -> mockk<SetNextPostPage>() to ExpectedHandler.POST
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
                is ViewSavedFilter -> mockk<ViewSavedFilter>() to ExpectedHandler.SEARCH

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
        fun `WHEN NavigationActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<NavigationAction>()
            coEvery {
                mockNavigationActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }

        @Test
        fun `WHEN PostActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<PostAction>()
            coEvery {
                mockPostActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }

        @Test
        fun `WHEN SearchActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<SearchAction>()
            coEvery {
                mockSearchActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }

        @Test
        fun `WHEN TagActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<TagAction>()
            coEvery {
                mockTagActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }

        @Test
        fun `WHEN NoteActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<NoteAction>()
            coEvery {
                mockNoteActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }

        @Test
        fun `WHEN PopularActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<PopularAction>()
            coEvery {
                mockPopularActionHandler.runAction(mockAction, expectedInitialValue)
            } returns expectedInitialValue

            val result = appStateDataSource.content.collectIn(this)

            // WHEN
            appStateDataSource.runAction(mockAction)

            // THEN
            assertThat(result).hasSize(0)
        }
    }

    @Test
    fun `GIVEN hasAuthToken is false WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            every { mockUserRepository.hasAuthToken() } returns false

            assertThat(appStateDataSource.content.first()).isEqualTo(expectedLoginInitialValue)
        }

    @Test
    fun `GIVEN hasAuthToken is true WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            every { mockUserRepository.hasAuthToken() } returns true
            every { mockConnectivityInfoProvider.isConnected() } returns false

            assertThat(appStateDataSource.content.first()).isEqualTo(expectedInitialValue)
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
