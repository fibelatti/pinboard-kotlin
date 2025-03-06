package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.MainState
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class UserPreferencesReducerTest {

    private val resourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.user_preferences_title) } returns "R.string.user_preferences_title"
    }

    private val reducer = UserPreferencesReducer(
        resourceProvider = resourceProvider,
    )

    @Test
    fun `WHEN invoke is called THEN MainState is returned with expected values`() {
        val mainState = MainState()

        val result = reducer.invoke(
            mainState = mainState,
            appState = mockk(),
        )

        assertThat(result).isEqualTo(
            MainState(
                title = MainState.TitleComponent.Visible("R.string.user_preferences_title"),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(),
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            ),
        )
    }
}
