package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.test.extension.mock
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mock<UserRepository>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val userPreferencesViewModel = UserPreferencesViewModel(
        mockUserRepository,
        mockAppStateRepository
    )

    @Test
    fun `WHEN saveAppearance is called THEN repository is updated`() {
        // GIVEN
        val mockAppearance = mock<Appearance>()

        // WHEN
        userPreferencesViewModel.saveAppearance(mockAppearance)

        // THEN
        verify(mockUserRepository).setAppearance(mockAppearance)
        verify(mockAppStateRepository).reset()
        userPreferencesViewModel.appearanceChanged.currentEventShouldBe(mockAppearance)
    }

    @Test
    fun `WHEN savePreferredDetailsView is called THEN repository is updated`() {
        // GIVEN
        val preferredDetailsView = mock<PreferredDetailsView>()

        // WHEN
        userPreferencesViewModel.savePreferredDetailsView(preferredDetailsView)

        // THEN
        verify(mockUserRepository).setPreferredDetailsView(preferredDetailsView)
    }

    @Test
    fun `WHEN saveAutoFillDescription is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveAutoFillDescription(value)

        // THEN
        verify(mockUserRepository).setAutoFillDescription(value)
    }

    @Test
    fun `WHEN saveShowDescriptionInLists is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveShowDescriptionInLists(value)

        // THEN
        verify(mockUserRepository).setShowDescriptionInLists(value)
    }

    @Test
    fun `WHEN saveShowDescriptionInDetails is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveShowDescriptionInDetails(value)

        // THEN
        verify(mockUserRepository).setShowDescriptionInDetails(value)
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveEditAfterSharing(value)

        // THEN
        verify(mockUserRepository).setEditAfterSharing(value)
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultPrivate(value)

        // THEN
        verify(mockUserRepository).setDefaultPrivate(value)
    }

    @Test
    fun `WHEN saveDefaultReadLater is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultReadLater(value)

        // THEN
        verify(mockUserRepository).setDefaultReadLater(value)
    }
}
