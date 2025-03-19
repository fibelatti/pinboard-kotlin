package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>()

    private val tagManagerStateFlow = MutableStateFlow(TagManagerState())
    private val mockTagManagerRepository = mockk<TagManagerRepository> {
        every { tagManagerState } returns tagManagerStateFlow
    }

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPeriodicSyncManager = mockk<PeriodicSyncManager> {
        justRun { enqueueWork(any()) }
    }

    private val userPreferencesViewModel = UserPreferencesViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        tagManagerRepository = mockTagManagerRepository,
        periodicSyncManager = mockPeriodicSyncManager,
    )

    @Test
    fun `currentPreferences should emit the repository values`() = runTest {
        // GIVEN
        val preferences = mockk<UserPreferences>()
        every { mockUserRepository.currentPreferences } returns MutableStateFlow(preferences)

        // THEN
        assertThat(userPreferencesViewModel.currentPreferences.first()).isEqualTo(preferences)
    }

    @Test
    fun `tag manager emissions should save default tags`() = runTest {
        tagManagerStateFlow.value = TagManagerState(tags = SAMPLE_TAGS)

        verify { mockUserRepository.defaultTags = SAMPLE_TAGS }
    }

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
    }

    @Test
    fun `WHEN saveApplyDynamicColors is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveApplyDynamicColors(value)

        // THEN
        verify { mockUserRepository.applyDynamicColors = value }
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
    fun `WHEN savePreferredSortType is called THEN repository is updated`() {
        // GIVEN
        val mockPreferredSortType = mockk<SortType>()

        // WHEN
        userPreferencesViewModel.savePreferredSortType(mockPreferredSortType)

        // THEN
        verify { mockUserRepository.preferredSortType = mockPreferredSortType }
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
    fun `WHEN saveFollowRedirects is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveFollowRedirects(value)

        // THEN
        verify { mockUserRepository.followRedirects = value }
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
}
