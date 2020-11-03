package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.isEmpty
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockGetSuggestedTags = mockk<GetSuggestedTags>()

    private val userPreferencesViewModel = UserPreferencesViewModel(
        mockUserRepository,
        mockAppStateRepository,
        mockGetSuggestedTags,
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

    @Test
    fun `WHEN saveDefaultTags is called THEN repository is updated`() {
        // GIVEN
        val value = mockk<List<Tag>>()

        // WHEN
        userPreferencesViewModel.saveDefaultTags(value)

        // THEN
        verify { mockUserRepository.setDefaultTags(value) }
    }

    @Test
    fun `GIVEN getSuggestedTags will fail WHEN searchForTag is called THEN suggestedTags should never receive values`() {
        // GIVEN
        coEvery { mockGetSuggestedTags(any()) } returns Failure(Exception())

        // WHEN
        userPreferencesViewModel.searchForTag(MockDataProvider.mockTagString1, mockk())

        // THEN
        runBlocking {
            assertThat(userPreferencesViewModel.suggestedTags.isEmpty()).isTrue()
        }
    }

    @Test
    fun `GIVEN getSuggestedTags will succeed WHEN searchForTag is called THEN suggestedTags should receive its response`() {
        // GIVEN
        val result = listOf(MockDataProvider.mockTagString1, MockDataProvider.mockTagString2)
        coEvery { mockGetSuggestedTags(any()) } returns Success(result)

        // WHEN
        userPreferencesViewModel.searchForTag(MockDataProvider.mockTagString1, mockk())

        // THEN
        runBlocking {
            assertThat(userPreferencesViewModel.suggestedTags.first()).isEqualTo(result)
        }
    }
}
