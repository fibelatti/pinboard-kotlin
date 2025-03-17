package com.fibelatti.pinboard.features.main

import app.cash.turbine.test
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalBrowserContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Reset
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.main.reducer.MainStateReducer
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
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
                is AccountSwitcherContent -> subclass.java to expectedReducerMock()
                is UserPreferencesContent -> subclass.java to expectedReducerMock()
                is ExternalBrowserContent -> subclass.java to expectedReducerMock()
                is ExternalContent -> subclass.java to expectedReducerMock()
            }
        }

    private val viewModel = MainViewModel(
        scope = TestScope(dispatcher),
        sharingStarted = SharingStarted.Lazily,
        appStateRepository = mockAppStateRepository,
        mainStateReducers = reducers,
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

    private companion object {

        val reducedMainState = mockk<MainState>()

        fun expectedReducerMock(): MainStateReducer = mockk<MainStateReducer> {
            every { this@mockk.invoke(any(), any()) } returns reducedMainState
        }
    }
}
