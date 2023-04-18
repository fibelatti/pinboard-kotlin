package com.fibelatti.pinboard.features

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class MainViewModelTest : BaseViewModelTest() {

    private val viewModel = MainViewModel()

    @Test
    fun `state emits updates sent via updateState`() = runUnconfinedTest {
        val newState = MainState(
            title = mockk(),
            subtitle = mockk(),
            navigation = mockk(),
            actionButton = mockk(),
            bottomAppBar = mockk(),
            floatingActionButton = mockk(),
        )

        val states = viewModel.state.collectIn(this)

        viewModel.updateState { newState }

        assertThat(states).containsExactly(
            MainState(),
            newState,
        )
    }

    @Test
    fun `navigationClicks emits only values with matching ids`() = runUnconfinedTest {
        val values = viewModel.navigationClicks("id").collectIn(this)

        viewModel.navigationClicked(id = "id")
        viewModel.navigationClicked(id = "another-id")
        viewModel.navigationClicked(id = "id")

        assertThat(values).containsExactly("id", "id")
    }

    @Test
    fun `actionButtonClicked emits only values with matching ids`() = runUnconfinedTest {
        val data: Any = mockk()

        val values = viewModel.actionButtonClicks("id").collectIn(this)

        viewModel.actionButtonClicked(id = "id", data = data)
        viewModel.actionButtonClicked(id = "another-id", data = data)
        viewModel.actionButtonClicked(id = "id", data = null)

        assertThat(values).containsExactly(data, null)
    }

    @Test
    fun `menuItemClicks emits only values with matching ids`() = runUnconfinedTest {
        val data: Any = mockk()

        val values = viewModel.menuItemClicks("id").collectIn(this)

        viewModel.menuItemClicked(id = "id", menuItemId = 1, data = data)
        viewModel.menuItemClicked(id = "another-id", menuItemId = 2, data = data)
        viewModel.menuItemClicked(id = "id", menuItemId = 3, data = null)

        assertThat(values).containsExactly(
            1 to data,
            3 to null,
        )
    }

    @Test
    fun `fabClicks emits only values with matching ids`() = runUnconfinedTest {
        val data: Any = mockk()

        val values = viewModel.fabClicks("id").collectIn(this)

        viewModel.fabClicked(id = "id", data = data)
        viewModel.fabClicked(id = "another-id", data = data)
        viewModel.fabClicked(id = "id", data = null)

        assertThat(values).containsExactly(data, null)
    }
}
