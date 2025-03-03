package com.fibelatti.pinboard.features

import app.cash.turbine.test
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Reset
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class MainViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository> {
        coJustRun { runAction(any()) }
    }

    private val viewModel = MainViewModel(
        scope = TestScope(dispatcher),
        sharingStarted = SharingStarted.Lazily,
        appStateRepository = mockAppStateRepository,
    )

    @Test
    fun `initial state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(MainState())
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
}
