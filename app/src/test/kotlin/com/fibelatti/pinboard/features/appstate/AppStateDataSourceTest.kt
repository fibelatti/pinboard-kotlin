package com.fibelatti.pinboard.features.appstate

import app.cash.turbine.test
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createLoginContent
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private val mockUserRepository = mockk<UserRepository> {
        every { hasAuthToken() } returns true
        every { showDescriptionInLists } returns false
        coJustRun { clearAuthToken() }
    }

    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns false
    }

    private val appModeFlow: MutableStateFlow<AppMode> = MutableStateFlow(AppMode.PINBOARD)
    private val mockkAppModeProvider = mockk<AppModeProvider> {
        every { appMode } returns appModeFlow
    }

    private val unauthorizedFlow = MutableSharedFlow<Unit>()
    private val mockUnauthorizedPluginProvider = mockk<UnauthorizedPluginProvider> {
        every { unauthorized } returns unauthorizedFlow
    }

    private val dispatcher = UnconfinedTestDispatcher()

    private val appStateDataSource by lazy {
        AppStateDataSource(
            scope = TestScope(dispatcher),
            dispatcher = Dispatchers.Unconfined,
            sharingStarted = SharingStarted.Lazily,
            actionHandlers = handlers,
            userRepository = mockUserRepository,
            connectivityInfoProvider = mockConnectivityInfoProvider,
            appModeProvider = mockkAppModeProvider,
            unauthorizedPluginProvider = mockUnauthorizedPluginProvider,
        )
    }

    private val expectedInitialLoginContent = createLoginContent()

    private val expectedInitialContent = createPostListContent()

    private val expectedInitialState = createAppState()

    private val expectedPostActionValue = createPostListContent(category = Recent)

    @Test
    fun `app state should have the initial value set`() = runTest {
        assertThat(appStateDataSource.appState.first()).isEqualTo(expectedInitialState)
    }

    @Test
    fun `app mode changes should update the state`() = runTest {
        appStateDataSource.appState.test {
            skipItems(1)

            appModeFlow.value = AppMode.LINKDING

            assertThat(awaitItem()).isEqualTo(expectedInitialState.copy(appMode = AppMode.LINKDING))
        }
    }

    @Test
    fun `WHEN unauthorized emits THEN UserUnauthorized runs`() = runTest {
        appStateDataSource.appState.test {
            skipItems(1)

            unauthorizedFlow.emit(Unit)

            assertThat(awaitItem()).isEqualTo(expectedInitialState.copy(content = LoginContent(isUnauthorized = true)))
        }

        coVerify {
            mockUserRepository.clearAuthToken()
        }
    }

    @Test
    fun `WHEN multi panel availability changed THEN the app state should be updated`() = runTest {
        appStateDataSource.appState.test {
            val value = randomBoolean()

            appStateDataSource.runAction(MultiPanelAvailabilityChanged(available = value))

            assertThat(expectMostRecentItem()).isEqualTo(expectedInitialState.copy(multiPanelAvailable = value))
        }
    }

    @Test
    fun `reset should set currentContent to the initial value`() = runTest {
        // GIVEN
        appStateDataSource.runAction(mockk<NavigationAction>())

        appStateDataSource.appState.test {
            // WHEN
            appStateDataSource.runAction(Reset)

            // THEN
            assertThat(awaitItem()).isEqualTo(expectedInitialState)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RunActionTests {

        @BeforeEach
        fun setup() {
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
        ) = runTest {
            val appStateDataSource = AppStateDataSource(
                scope = TestScope(dispatcher),
                dispatcher = Dispatchers.Unconfined,
                sharingStarted = SharingStarted.Lazily,
                actionHandlers = handlers,
                userRepository = mockUserRepository,
                connectivityInfoProvider = mockConnectivityInfoProvider,
                appModeProvider = mockkAppModeProvider,
                unauthorizedPluginProvider = mockUnauthorizedPluginProvider,
            )

            appStateDataSource.appState.test {
                // GIVEN
                val (action, expectedHandler) = testCase

                // WHEN
                appStateDataSource.runAction(action)

                // THEN
                val state = expectMostRecentItem()

                when (expectedHandler) {
                    ExpectedHandler.NONE -> {
                        when (action) {
                            is MultiPanelAvailabilityChanged -> {
                                assertThat(state).isEqualTo(
                                    expectedInitialState.copy(multiPanelAvailable = action.available),
                                )
                            }

                            is Reset -> {
                                assertThat(state).isEqualTo(expectedInitialState)
                            }

                            is UserLoggedIn -> {
                                assertThat(state).isEqualTo(expectedInitialState)
                            }

                            is UserLoggedOut -> {
                                assertThat(state).isEqualTo(
                                    expectedInitialState.copy(content = expectedInitialLoginContent),
                                )
                                verify { mockUserRepository.clearAuthToken() }
                            }

                            is UserUnauthorized -> {
                                assertThat(state).isEqualTo(
                                    expectedInitialState.copy(content = LoginContent(isUnauthorized = true)),
                                )
                                verify { mockUserRepository.clearAuthToken() }
                            }

                            else -> fail { "Action should be assigned to a handler" }
                        }
                    }

                    ExpectedHandler.NAVIGATION -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is NavigationAction)
                        coVerify { mockNavigationActionHandler.runAction(action, expectedInitialContent) }
                    }

                    ExpectedHandler.POST -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is PostAction)
                        coVerify { mockPostActionHandler.runAction(action, expectedInitialContent) }
                    }

                    ExpectedHandler.SEARCH -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is SearchAction)
                        coVerify { mockSearchActionHandler.runAction(action, expectedInitialContent) }
                    }

                    ExpectedHandler.TAG -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is TagAction)
                        coVerify { mockTagActionHandler.runAction(action, expectedInitialContent) }
                    }

                    ExpectedHandler.NOTE -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is NoteAction)
                        coVerify { mockNoteActionHandler.runAction(action, expectedInitialContent) }
                    }

                    ExpectedHandler.POPULAR -> {
                        assertThat(state).isEqualTo(expectedInitialState.copy(content = expectedPostActionValue))
                        require(action is PopularAction)
                        coVerify { mockPopularActionHandler.runAction(action, expectedInitialContent) }
                    }
                }
            }
        }

        fun testCases(): List<Pair<Action, ExpectedHandler>> = Action::class.allSealedSubclasses.map { subclass ->
            when (subclass.objectInstance ?: mockkClass(subclass)) {
                // App
                is MultiPanelAvailabilityChanged -> MultiPanelAvailabilityChanged(randomBoolean()) to
                    ExpectedHandler.NONE
                is Reset -> Reset to ExpectedHandler.NONE

                // Auth
                is UserLoggedIn -> UserLoggedIn to ExpectedHandler.NONE
                is UserLoggedOut -> UserLoggedOut to ExpectedHandler.NONE
                is UserUnauthorized -> UserUnauthorized to ExpectedHandler.NONE

                // Navigation
                is NavigateBack -> NavigateBack to ExpectedHandler.NAVIGATION
                is All -> All to ExpectedHandler.NAVIGATION
                is Recent -> Recent to ExpectedHandler.NAVIGATION
                is Public -> Public to ExpectedHandler.NAVIGATION
                is Private -> Private to ExpectedHandler.NAVIGATION
                is Unread -> Unread to ExpectedHandler.NAVIGATION
                is Untagged -> Untagged to ExpectedHandler.NAVIGATION
                is ViewPost -> mockk<ViewPost>() to ExpectedHandler.NAVIGATION
                is ViewSearch -> ViewSearch to ExpectedHandler.NAVIGATION
                is AddPost -> AddPost to ExpectedHandler.NAVIGATION
                is ViewTags -> ViewTags to ExpectedHandler.NAVIGATION
                is ViewSavedFilters -> ViewSavedFilters to ExpectedHandler.NAVIGATION
                is ViewNotes -> ViewNotes to ExpectedHandler.NAVIGATION
                is ViewNote -> mockk<ViewNote>() to ExpectedHandler.NAVIGATION
                is ViewPopular -> ViewPopular to ExpectedHandler.NAVIGATION
                is ViewPreferences -> ViewPreferences to ExpectedHandler.NAVIGATION

                // Post
                is Refresh -> mockk<Refresh>() to ExpectedHandler.POST
                is SetPosts -> mockk<SetPosts>() to ExpectedHandler.POST
                is GetNextPostPage -> GetNextPostPage to ExpectedHandler.POST
                is SetNextPostPage -> mockk<SetNextPostPage>() to ExpectedHandler.POST
                is SetSorting -> mockk<SetSorting>() to ExpectedHandler.POST
                is EditPost -> mockk<EditPost>() to ExpectedHandler.POST
                is EditPostFromShare -> mockk<EditPostFromShare>() to ExpectedHandler.POST
                is PostSaved -> mockk<PostSaved>() to ExpectedHandler.POST
                is PostDeleted -> PostDeleted to ExpectedHandler.POST

                // Search
                is RefreshSearchTags -> RefreshSearchTags to ExpectedHandler.SEARCH
                is SetTerm -> mockk<SetTerm>() to ExpectedHandler.SEARCH
                is SetSearchTags -> mockk<SetSearchTags>() to ExpectedHandler.SEARCH
                is AddSearchTag -> mockk<AddSearchTag>() to ExpectedHandler.SEARCH
                is RemoveSearchTag -> mockk<RemoveSearchTag>() to ExpectedHandler.SEARCH
                is SetResultSize -> mockk<SetResultSize>() to ExpectedHandler.SEARCH
                is Search -> mockk<Search>() to ExpectedHandler.SEARCH
                is ClearSearch -> ClearSearch to ExpectedHandler.SEARCH
                is ViewSavedFilter -> mockk<ViewSavedFilter>() to ExpectedHandler.SEARCH

                // Tag
                is RefreshTags -> RefreshTags to ExpectedHandler.TAG
                is SetTags -> mockk<SetTags>() to ExpectedHandler.TAG
                is PostsForTag -> mockk<PostsForTag>() to ExpectedHandler.TAG

                // Notes
                is RefreshNotes -> RefreshNotes to ExpectedHandler.NOTE
                is SetNotes -> mockk<SetNotes>() to ExpectedHandler.NOTE
                is SetNote -> mockk<SetNote>() to ExpectedHandler.NOTE

                // Popular
                is RefreshPopular -> RefreshPopular to ExpectedHandler.POPULAR
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
                mockNavigationActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }

        @Test
        fun `WHEN PostActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<PostAction>()
            coEvery {
                mockPostActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }

        @Test
        fun `WHEN SearchActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<SearchAction>()
            coEvery {
                mockSearchActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }

        @Test
        fun `WHEN TagActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<TagAction>()
            coEvery {
                mockTagActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }

        @Test
        fun `WHEN NoteActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<NoteAction>()
            coEvery {
                mockNoteActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }

        @Test
        fun `WHEN PopularActionHandler return the same content THEN value is never updated`() = runTest {
            // GIVEN
            val mockAction = mockk<PopularAction>()
            coEvery {
                mockPopularActionHandler.runAction(mockAction, expectedInitialContent)
            } returns expectedInitialContent

            appStateDataSource.appState.map { it.content }.test {
                // WHEN
                appStateDataSource.runAction(mockAction)

                // THEN
                assertThat(receivedItems()).containsExactly(expectedInitialContent)
            }
        }
    }

    @Test
    fun `GIVEN hasAuthToken is false WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            every { mockUserRepository.hasAuthToken() } returns false

            assertThat(appStateDataSource.appState.first().content).isEqualTo(expectedInitialLoginContent)
        }

    @Test
    fun `GIVEN hasAuthToken is true WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            every { mockUserRepository.hasAuthToken() } returns true

            assertThat(appStateDataSource.appState.first().content).isEqualTo(expectedInitialContent)
        }

    @Test
    fun `GIVEN appMode is NO_API WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            appModeFlow.value = AppMode.NO_API

            every { mockUserRepository.hasAuthToken() } returns false

            assertThat(appStateDataSource.appState.first().content).isEqualTo(expectedInitialContent)
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
