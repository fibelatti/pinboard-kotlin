package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.isEmpty
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockGetSuggestedTags = mockk<GetSuggestedTags>()
    private val mockPeriodicSyncManager = mockk<PeriodicSyncManager> {
        every { enqueueWork(any()) } just runs
    }

    private val userPreferencesViewModel = UserPreferencesViewModel(
        mockUserRepository,
        mockAppStateRepository,
        mockGetSuggestedTags,
        mockPeriodicSyncManager,
    )

    @Test
    fun `WHEN savePeriodicSync is called THEN repository is updated AND periodicSyncManager enqueues`() {
        // GIVEN
        val mockPeriodicSync = mockk<PeriodicSync>()

        // WHEN
        userPreferencesViewModel.savePeriodicSync(mockPeriodicSync)

        // THEN
        verify { mockUserRepository.periodicSync = mockPeriodicSync }
        verify { mockPeriodicSyncManager.enqueueWork(shouldReplace = true) }
    }

    @Test
    fun `WHEN saveAppearance is called THEN repository is updated`() {
        // GIVEN
        val mockAppearance = mockk<Appearance>()

        // WHEN
        userPreferencesViewModel.saveAppearance(mockAppearance)

        // THEN
        verify { mockUserRepository.appearance = mockAppearance }
        verify { mockAppStateRepository.reset() }
        runBlocking {
            assertThat(userPreferencesViewModel.appearanceChanged.first()).isEqualTo(mockAppearance)
        }
    }

    @Test
    fun `WHEN savePreferredDateFormat is called THEN repository is updated`() {
        // GIVEN
        val mockPreferredDateFormat = mockk<PreferredDateFormat>()

        // WHEN
        userPreferencesViewModel.savePreferredDateFormat(mockPreferredDateFormat)

        // THEN
        verify { mockUserRepository.preferredDateFormat = mockPreferredDateFormat }
    }

    @Test
    fun `WHEN savePreferredDetailsView is called THEN repository is updated`() {
        // GIVEN
        val preferredDetailsView = mockk<PreferredDetailsView>()

        // WHEN
        userPreferencesViewModel.savePreferredDetailsView(preferredDetailsView)

        // THEN
        verify { mockUserRepository.preferredDetailsView = preferredDetailsView }
    }

    @Test
    fun `WHEN saveMarkAsReadOnOpen is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveMarkAsReadOnOpen(value)

        // THEN
        verify { mockUserRepository.markAsReadOnOpen = value }
    }

    @Test
    fun `WHEN saveAutoFillDescription is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveAutoFillDescription(value)

        // THEN
        verify { mockUserRepository.autoFillDescription = value }
    }

    @Test
    fun `WHEN saveShowDescriptionInLists is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveShowDescriptionInLists(value)

        // THEN
        verify { mockUserRepository.showDescriptionInLists = value }
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN repository is updated`() {
        // GIVEN
        val value = mockk<EditAfterSharing>()

        // WHEN
        userPreferencesViewModel.saveEditAfterSharing(value)

        // THEN
        verify { mockUserRepository.editAfterSharing = value }
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultPrivate(value)

        // THEN
        verify { mockUserRepository.defaultPrivate = value }
    }

    @Test
    fun `WHEN saveDefaultReadLater is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveDefaultReadLater(value)

        // THEN
        verify { mockUserRepository.defaultReadLater = value }
    }

    @Test
    fun `WHEN saveDefaultTags is called THEN repository is updated`() {
        // GIVEN
        val value = mockk<List<Tag>>()

        // WHEN
        userPreferencesViewModel.saveDefaultTags(value)

        // THEN
        verify { mockUserRepository.defaultTags = value }
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
