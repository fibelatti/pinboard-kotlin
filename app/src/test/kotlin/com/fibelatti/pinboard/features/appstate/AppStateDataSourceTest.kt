package com.fibelatti.pinboard.features.appstate

import app.cash.turbine.test
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
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

    private val mockUserCredentials = mockk<UserCredentials> {
        every { hasAuthToken() } returns true
    }

    private val mockUserRepository = mockk<UserRepository> {
        every { userCredentials } returns MutableStateFlow(mockUserCredentials)
        every { showDescriptionInLists } returns false
        every { useSplitNav } returns true
        coJustRun { clearAuthToken(appMode = any()) }
    }

    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider> {
        every { isConnected() } returns false
    }

    private val mockAppMode = mockk<AppMode>()

    private val appModeFlow: MutableStateFlow<AppMode> = MutableStateFlow(AppMode.PINBOARD)
    private val mockAppModeProvider = mockk<AppModeProvider> {
        every { appMode } returns appModeFlow
        coJustRun { setSelection(any()) }
    }

    private val unauthorizedFlow = MutableSharedFlow<AppMode>()
    private val mockUnauthorizedPluginProvider = mockk<UnauthorizedPluginProvider> {
        every { unauthorized } returns unauthorizedFlow
        coJustRun { disable(any()) }
    }

    private val mockSortType = mockk<SortType>()
    private val mockGetPreferredSortType = mockk<GetPreferredSortType> {
        every { this@mockk.invoke() } returns mockSortType
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
            appModeProvider = mockAppModeProvider,
            unauthorizedPluginProvider = mockUnauthorizedPluginProvider,
            getPreferredSortType = mockGetPreferredSortType,
        )
    }

    private val expectedInitialContent = createPostListContent(
        sortType = mockSortType,
    )
    private val expectedInitialState = createAppState(
        content = expectedInitialContent,
    )

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
    fun `WHEN unauthorized emits THEN UserUnauthorized runs - one account connected`() = runTest {
        val appMode = mockk<AppMode>()

        every { mockUserCredentials.hasAuthToken() } returns true andThen false

        appStateDataSource.appState.test {
            skipItems(1)

            unauthorizedFlow.emit(appMode)

            assertThat(expectMostRecentItem()).isEqualTo(expectedInitialState.copy(content = LoginContent()))
        }

        coVerify {
            mockAppModeProvider.setSelection(appMode = null)
            mockUnauthorizedPluginProvider.disable(appMode = appMode)
            mockUserRepository.clearAuthToken(appMode = appMode)
        }
    }

    @Test
    fun `WHEN unauthorized emits THEN UserUnauthorized runs - multi account connected`() = runTest {
        appStateDataSource.appState.test {
            unauthorizedFlow.emit(mockAppMode)

            assertThat(expectMostRecentItem()).isEqualTo(expectedInitialState)
        }

        coVerify {
            mockAppModeProvider.setSelection(appMode = null)
            mockUnauthorizedPluginProvider.disable(appMode = mockAppMode)
            mockUserRepository.clearAuthToken(appMode = mockAppMode)
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
                appModeProvider = mockAppModeProvider,
                unauthorizedPluginProvider = mockUnauthorizedPluginProvider,
                getPreferredSortType = mockGetPreferredSortType,
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
                                coVerifyOrder {
                                    mockAppModeProvider.setSelection(appMode = mockAppMode)
                                    mockUnauthorizedPluginProvider.enable(appMode = mockAppMode)
                                }
                            }

                            is UserLoginFailed -> {
                                coVerifyOrder {
                                    mockAppModeProvider.setSelection(appMode = null)
                                    mockUnauthorizedPluginProvider.disable(appMode = mockAppMode)
                                    mockUserRepository.clearAuthToken(appMode = mockAppMode)
                                }
                            }

                            is UserLoggedOut -> {
                                coVerifyOrder {
                                    mockAppModeProvider.setSelection(appMode = null)
                                    mockUnauthorizedPluginProvider.disable(appMode = mockAppMode)
                                    mockUserRepository.clearAuthToken(appMode = mockAppMode)
                                    mockUserCredentials.hasAuthToken()
                                }
                            }

                            is UserUnauthorized -> {
                                coVerifyOrder {
                                    mockAppModeProvider.setSelection(appMode = null)
                                    mockUnauthorizedPluginProvider.disable(appMode = mockAppMode)
                                    mockUserRepository.clearAuthToken(appMode = mockAppMode)
                                    mockUserCredentials.hasAuthToken()
                                }
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
            val value = subclass.objectInstance ?: mockkClass(subclass) {
                every { prettyPrint() } returns ""

                if (this is AuthAction) {
                    every { appMode } returns mockAppMode
                }
            }

            when (value) {
                // App
                is MultiPanelAvailabilityChanged -> {
                    MultiPanelAvailabilityChanged(randomBoolean()) to ExpectedHandler.NONE
                }

                is Reset -> value to ExpectedHandler.NONE

                // Auth
                is UserLoggedIn -> value to ExpectedHandler.NONE
                is UserLoginFailed -> value to ExpectedHandler.NONE
                is UserLoggedOut -> value to ExpectedHandler.NONE
                is UserUnauthorized -> value to ExpectedHandler.NONE

                // Navigation
                is NavigateBack -> value to ExpectedHandler.NAVIGATION
                is All -> value to ExpectedHandler.NAVIGATION
                is Recent -> value to ExpectedHandler.NAVIGATION
                is Public -> value to ExpectedHandler.NAVIGATION
                is Private -> value to ExpectedHandler.NAVIGATION
                is Unread -> value to ExpectedHandler.NAVIGATION
                is Untagged -> value to ExpectedHandler.NAVIGATION
                is ViewPost -> value to ExpectedHandler.NAVIGATION
                is ViewRandomPost -> value to ExpectedHandler.NAVIGATION
                is ViewSearch -> value to ExpectedHandler.NAVIGATION
                is AddPost -> value to ExpectedHandler.NAVIGATION
                is ViewTags -> value to ExpectedHandler.NAVIGATION
                is ViewSavedFilters -> value to ExpectedHandler.NAVIGATION
                is ViewNotes -> value to ExpectedHandler.NAVIGATION
                is ViewNote -> value to ExpectedHandler.NAVIGATION
                is ViewPopular -> value to ExpectedHandler.NAVIGATION
                is ViewAccountSwitcher -> value to ExpectedHandler.NAVIGATION
                is AddAccount -> value to ExpectedHandler.NAVIGATION
                is ViewPreferences -> value to ExpectedHandler.NAVIGATION

                // Post
                is Refresh -> value to ExpectedHandler.POST
                is SetPosts -> value to ExpectedHandler.POST
                is GetNextPostPage -> value to ExpectedHandler.POST
                is SetNextPostPage -> value to ExpectedHandler.POST
                is SetSorting -> value to ExpectedHandler.POST
                is EditPost -> value to ExpectedHandler.POST
                is EditPostFromShare -> value to ExpectedHandler.POST
                is PostSaved -> value to ExpectedHandler.POST
                is PostDeleted -> value to ExpectedHandler.POST

                // Search
                is RefreshSearchTags -> value to ExpectedHandler.SEARCH
                is SetTerm -> value to ExpectedHandler.SEARCH
                is SetSearchTags -> value to ExpectedHandler.SEARCH
                is AddSearchTag -> value to ExpectedHandler.SEARCH
                is RemoveSearchTag -> value to ExpectedHandler.SEARCH
                is SetResultSize -> value to ExpectedHandler.SEARCH
                is ViewRandomSearch -> value to ExpectedHandler.SEARCH
                is Search -> value to ExpectedHandler.SEARCH
                is ClearSearch -> value to ExpectedHandler.SEARCH
                is ViewSavedFilter -> value to ExpectedHandler.SEARCH

                // Tag
                is RefreshTags -> value to ExpectedHandler.TAG
                is SetTags -> value to ExpectedHandler.TAG
                is PostsForTag -> value to ExpectedHandler.TAG

                // Notes
                is RefreshNotes -> value to ExpectedHandler.NOTE
                is SetNotes -> value to ExpectedHandler.NOTE
                is SetNote -> value to ExpectedHandler.NOTE

                // Popular
                is RefreshPopular -> value to ExpectedHandler.POPULAR
                is SetPopularPosts -> value to ExpectedHandler.POPULAR
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
            every { mockUserCredentials.hasAuthToken() } returns false

            assertThat(appStateDataSource.appState.first().content).isEqualTo(LoginContent())
        }

    @Test
    fun `GIVEN hasAuthToken is true WHEN getInitialContent is called THEN expected initial content is returned`() =
        runTest {
            every { mockUserCredentials.hasAuthToken() } returns true

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
