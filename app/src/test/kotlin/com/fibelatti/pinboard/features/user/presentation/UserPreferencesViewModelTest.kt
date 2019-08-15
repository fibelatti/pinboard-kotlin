package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mock<UserRepository>()

    private val userPreferencesViewModel = UserPreferencesViewModel(
        mockUserRepository
    )

    @Test
    fun `WHEN saveAppearance is called THEN repository is updated`() {
        // GIVEN
        val mockAppearance = mock<Appearance>()

        // WHEN
        userPreferencesViewModel.saveAppearance(mockAppearance)

        // THEN
        verifySuspend(mockUserRepository) { setAppearance(mockAppearance) }
        userPreferencesViewModel.appearanceChanged.currentEventShouldBe(mockAppearance)
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultPrivate(value)

        // THEN
        verifySuspend(mockUserRepository) { setDefaultPrivate(value) }
    }

    @Test
    fun `WHEN saveDefaultReadLater is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultReadLater(value)

        // THEN
        verifySuspend(mockUserRepository) { setDefaultReadLater(value) }
    }
}
