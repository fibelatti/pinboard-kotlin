package com.fibelatti.pinboard.features.main

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_INSTANCE_URL
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.LocalNetworkAccessProvider
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalBrowserContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.OfflineCopyDetailContent
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Reset
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.main.reducer.MainStateReducer
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MainViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val reducers: Map<Class<out Content>, MainStateReducer> = Content::class.allSealedSubclasses
        .associate { subclass ->
            when (subclass.objectInstance ?: mockkClass(subclass)) {
                is LoginContent -> subclass.java to expectedReducerMock()
                is PostListContent -> subclass.java to expectedReducerMock()
                is PostDetailContent -> subclass.java to expectedReducerMock()
                is AddPostContent -> subclass.java to expectedReducerMock()
                is EditPostContent -> subclass.java to expectedReducerMock()
                is SearchContent -> subclass.java to expectedReducerMock()
                is SavedFiltersContent -> subclass.java to expectedReducerMock()
                is TagListContent -> subclass.java to expectedReducerMock()
                is PopularPostsContent -> subclass.java to expectedReducerMock()
                is PopularPostDetailContent -> subclass.java to expectedReducerMock()
                is NoteListContent -> subclass.java to expectedReducerMock()
                is NoteDetailContent -> subclass.java to expectedReducerMock()
                is OfflineCopyListContent -> subclass.java to expectedReducerMock()
                is OfflineCopyDetailContent -> subclass.java to expectedReducerMock()
                is AccountSwitcherContent -> subclass.java to expectedReducerMock()
                is UserPreferencesContent -> subclass.java to expectedReducerMock()
                is ExternalBrowserContent -> subclass.java to expectedReducerMock()
                is ExternalContent -> subclass.java to expectedReducerMock()
            }
        }

    private val mockUserRepository = mockk<UserRepository> {
        every { linkdingInstanceUrl } returns null
    }
    private val mockLocalNetworkAccessProvider = mockk<LocalNetworkAccessProvider> {
        coEvery { isPermissionRequired(any()) } returns false
    }

    private val viewModel = MainViewModel(
        savedStateHandle = SavedStateHandle(),
        dispatcher = dispatcher,
        sharingStarted = SharingStarted.Lazily,
        appStateRepository = mockAppStateRepository,
        mainStateReducers = reducers,
        userRepository = mockUserRepository,
        localNetworkAccessProvider = mockLocalNetworkAccessProvider,
    )

    @Test
    fun `initial state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(MainState())
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class MainStateReducerTests {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `app state emissions trigger the corresponding reducer`(testCase: Content) = runTest {
            viewModel.state.test {
                appStateFlow.update { it.copy(content = testCase) }

                assertThat(expectMostRecentItem()).isEqualTo(reducedMainState)

                val expectedReducer = reducers[testCase::class.java]

                assertThat(expectedReducer).isNotNull()
                verify { expectedReducer?.invoke(any(), any()) }
            }
        }

        fun testCases(): List<Content> = Content::class.allSealedSubclasses.map { subclass ->
            subclass.objectInstance ?: mockkClass(subclass)
        }
    }

    @Test
    fun `state emits updates sent via updateState`() = runTest {
        viewModel.state.test {
            val newState = mockk<MainState>()

            viewModel.updateState { newState }

            assertThat(expectMostRecentItem()).isEqualTo(newState)
        }
    }

    @Test
    fun `setMultiPanelAvailable runs the corresponding action`() = runTest {
        val value = randomBoolean()

        viewModel.setMultiPanelAvailable(value = value)

        coVerify {
            mockAppStateRepository.runAction(MultiPanelAvailabilityChanged(value))
        }
    }

    @Test
    fun `setCurrentScrollDirection updates the state`() = runTest {
        val direction = mockk<ScrollDirection>()

        viewModel.state.test {
            viewModel.setCurrentScrollDirection(direction)

            assertThat(expectMostRecentItem()).isEqualTo(
                MainState(scrollDirection = direction),
            )
        }
    }

    @Test
    fun `navigateBack runs a navigate back action`() = runTest {
        viewModel.navigateBack()

        coVerify {
            mockAppStateRepository.runAction(NavigateBack)
        }
    }

    @Test
    fun `resetAppNavigation runs a reset action`() = runTest {
        viewModel.resetAppNavigation()

        coVerify {
            mockAppStateRepository.runAction(Reset)
        }
    }

    @Test
    fun `actionButtonClicked emits only values with matching ids`() = runTest {
        viewModel.actionButtonClicks(contentType = PostListContent::class).test {
            val data: Any = mockk()

            viewModel.actionButtonClicked(contentType = PostListContent::class, data = data)
            viewModel.actionButtonClicked(contentType = PostDetailContent::class, data = data)
            viewModel.actionButtonClicked(contentType = PostListContent::class, data = null)

            assertThat(receivedItems()).containsExactly(data, null)
        }
    }

    @Test
    fun `menuItemClicks emits only values with matching ids`() = runTest {
        viewModel.menuItemClicks(contentType = PostListContent::class).test {
            val data: Any = mockk()

            val menuItemComponent1 = mockk<MainState.MenuItemComponent>()
            val menuItemComponent2 = mockk<MainState.MenuItemComponent>()
            val menuItemComponent3 = mockk<MainState.MenuItemComponent>()

            viewModel.menuItemClicked(contentType = PostListContent::class, menuItem = menuItemComponent1, data = data)
            viewModel.menuItemClicked(
                contentType = PostDetailContent::class,
                menuItem = menuItemComponent2,
                data = data,
            )
            viewModel.menuItemClicked(contentType = PostListContent::class, menuItem = menuItemComponent3, data = null)

            assertThat(receivedItems()).containsExactly(
                menuItemComponent1 to data,
                menuItemComponent3 to null,
            )
        }
    }

    @Test
    fun `fabClicks emits only values with matching ids`() = runTest {
        viewModel.fabClicks(contentType = PostListContent::class).test {
            val data: Any = mockk()

            viewModel.fabClicked(contentType = PostListContent::class, data = data)
            viewModel.fabClicked(contentType = PostDetailContent::class, data = data)
            viewModel.fabClicked(contentType = PostListContent::class, data = null)

            assertThat(receivedItems()).containsExactly(data, null)
        }
    }

    @Nested
    inner class HandleDeeplinkTests {

        private val post = createPost(id = "deeplink-post-id")
        private val postListContent = createPostListContent().copy(
            posts = PostList(list = listOf(post), totalCount = 1, canPaginate = false, alphabetizeTags = false),
        )

        @Test
        fun `handleDeeplink dispatches All action`() = runTest {
            viewModel.handleDeeplink(postId = "deeplink-post-id", openEditor = false)

            coVerify { mockAppStateRepository.runAction(All) }
        }

        @Test
        fun `consumeDeepLink dispatches ViewPost when post is found and openEditor is false`() = runTest {
            viewModel.handleDeeplink(postId = "deeplink-post-id", openEditor = false)

            appStateFlow.update { it.copy(content = postListContent) }

            coVerify { mockAppStateRepository.runAction(ViewPost(post)) }
        }

        @Test
        fun `consumeDeepLink dispatches EditPost when post is found and openEditor is true`() = runTest {
            viewModel.handleDeeplink(postId = "deeplink-post-id", openEditor = true)

            appStateFlow.update { it.copy(content = postListContent) }

            coVerify { mockAppStateRepository.runAction(EditPost(post = post)) }
        }

        @Test
        fun `consumeDeepLink does nothing when no post matches the pending deeplink id`() = runTest {
            viewModel.handleDeeplink(postId = "non-existent-id", openEditor = false)

            appStateFlow.update { it.copy(content = postListContent) }

            coVerify(exactly = 0) { mockAppStateRepository.runAction(any<ViewPost>()) }
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any<EditPost>()) }
        }

        @Test
        fun `consumeDeepLink does nothing when posts are not yet loaded`() = runTest {
            viewModel.handleDeeplink(postId = "deeplink-post-id", openEditor = false)

            appStateFlow.update { it.copy(content = createPostListContent()) }

            coVerify(exactly = 0) { mockAppStateRepository.runAction(any<ViewPost>()) }
            coVerify(exactly = 0) { mockAppStateRepository.runAction(any<EditPost>()) }
        }
    }

    @Nested
    inner class LocalNetworkPermissionTests {

        @Test
        fun `GIVEN linkding is used AND the permission is required THEN it is flagged as required`() = runTest {
            // GIVEN
            every { mockUserRepository.linkdingInstanceUrl } returns SAMPLE_INSTANCE_URL
            coEvery { mockLocalNetworkAccessProvider.isPermissionRequired(SAMPLE_INSTANCE_URL) } returns true

            // WHEN
            appStateFlow.update { it.copy(appMode = AppMode.LINKDING, content = createPostListContent()) }

            // THEN
            assertThat(viewModel.localNetworkPermissionRequired.first()).isTrue()
        }

        @Test
        fun `GIVEN linkding is used AND the permission is not required THEN it is not flagged`() = runTest {
            // GIVEN
            every { mockUserRepository.linkdingInstanceUrl } returns SAMPLE_INSTANCE_URL
            coEvery { mockLocalNetworkAccessProvider.isPermissionRequired(SAMPLE_INSTANCE_URL) } returns false

            // WHEN
            appStateFlow.update { it.copy(appMode = AppMode.LINKDING, content = createPostListContent()) }

            // THEN
            assertThat(viewModel.localNetworkPermissionRequired.first()).isFalse()
        }

        @Test
        fun `GIVEN pinboard is used THEN the permission is never checked`() = runTest {
            // WHEN
            appStateFlow.update { it.copy(appMode = AppMode.PINBOARD, content = createPostListContent()) }

            // THEN
            coVerify(exactly = 0) { mockLocalNetworkAccessProvider.isPermissionRequired(any()) }

            assertThat(viewModel.localNetworkPermissionRequired.first()).isFalse()
        }

        @Test
        fun `GIVEN the login screen is shown THEN the permission is never checked`() = runTest {
            // WHEN
            appStateFlow.update { it.copy(appMode = AppMode.LINKDING, content = LoginContent()) }

            // THEN
            coVerify(exactly = 0) { mockLocalNetworkAccessProvider.isPermissionRequired(any()) }

            assertThat(viewModel.localNetworkPermissionRequired.first()).isFalse()
        }

        @Test
        fun `WHEN localNetworkPermissionHandled is called THEN the flag is cleared`() = runTest {
            // GIVEN
            every { mockUserRepository.linkdingInstanceUrl } returns SAMPLE_INSTANCE_URL
            coEvery { mockLocalNetworkAccessProvider.isPermissionRequired(SAMPLE_INSTANCE_URL) } returns true
            appStateFlow.update { it.copy(appMode = AppMode.LINKDING, content = createPostListContent()) }

            // WHEN
            viewModel.localNetworkPermissionHandled()

            // THEN
            assertThat(viewModel.localNetworkPermissionRequired.first()).isFalse()
        }
    }

    private companion object {

        val reducedMainState = mockk<MainState>()

        fun expectedReducerMock(): MainStateReducer = mockk<MainStateReducer> {
            every { this@mockk.invoke(any(), any()) } returns reducedMainState
        }
    }
}
