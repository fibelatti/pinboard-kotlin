package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)

    private val userPreferencesViewModel = UserPreferencesViewModel(
        mockUserRepository,
        mockAppStateRepository
    )

    @Test
    fun `WHEN saveAppearance is called THEN repository is updated`() {
        // GIVEN
        val mockAppearance = mockk<Appearance>()

        // WHEN
        userPreferencesViewModel.saveAppearance(mockAppearance)

        // THEN
        verify { mockUserRepository.setAppearance(mockAppearance) }
        verify { mockAppStateRepository.reset() }
        runBlocking {
            assertThat(userPreferencesViewModel.appearanceChanged.first()).isEqualTo(mockAppearance)
        }
    }

    @Test
    fun `WHEN savePreferredDetailsView is called THEN repository is updated`() {
        // GIVEN
        val preferredDetailsView = mockk<PreferredDetailsView>()

        // WHEN
        userPreferencesViewModel.savePreferredDetailsView(preferredDetailsView)

        // THEN
        verify { mockUserRepository.setPreferredDetailsView(preferredDetailsView) }
    }

    @Test
    fun `WHEN saveMarkAsReadOnOpen is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveMarkAsReadOnOpen(value)

        // THEN
        verify { mockUserRepository.setMarkAsReadOnOpen(value) }
    }

    @Test
    fun `WHEN saveAutoFillDescription is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveAutoFillDescription(value)

        // THEN
        verify { mockUserRepository.setAutoFillDescription(value) }
    }

    @Test
    fun `WHEN saveShowDescriptionInLists is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveShowDescriptionInLists(value)

        // THEN
        verify { mockUserRepository.setShowDescriptionInLists(value) }
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN repository is updated`() {
        // GIVEN
        val value = mockk<EditAfterSharing>()

        // WHEN
        userPreferencesViewModel.saveEditAfterSharing(value)

        // THEN
        verify { mockUserRepository.setEditAfterSharing(value) }
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultPrivate(value)

        // THEN
        verify { mockUserRepository.setDefaultPrivate(value) }
    }

    @Test
    fun `WHEN saveDefaultReadLater is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultReadLater(value)

        // THEN
        verify { mockUserRepository.setDefaultReadLater(value) }
    }
}
