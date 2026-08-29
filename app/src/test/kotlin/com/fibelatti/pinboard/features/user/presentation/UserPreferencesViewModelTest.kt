package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockOfflineCopyRepository = mockk<OfflineCopyRepository>(relaxed = true)

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
    }

    private val tagManagerStateFlow = MutableStateFlow(TagManagerState())
    private val mockTagManagerRepository = mockk<TagManagerRepository> {
        every { tagManagerState } returns tagManagerStateFlow
    }

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPeriodicSyncManager = mockk<PeriodicSyncManager> {
        justRun { enqueueWork(any()) }
    }

    private val userPreferencesViewModel = UserPreferencesViewModel(
        dispatcher = dispatcher,
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        tagManagerRepository = mockTagManagerRepository,
        periodicSyncManager = mockPeriodicSyncManager,
        offlineCopyRepository = mockOfflineCopyRepository,
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
    fun `tag manager emissions should save default tags if the content is UserPreferencesContent`() = runTest {
        appStateFlow.value = createAppState(content = mockk<UserPreferencesContent>())

        tagManagerStateFlow.value = TagManagerState(tags = SAMPLE_TAGS)

        verify { mockUserRepository.defaultTags = SAMPLE_TAGS }
    }

    @Test
    fun `tag manager emissions should not save default tags if the content is not UserPreferencesContent`() = runTest {
        appStateFlow.value = createAppState(content = mockk())

        tagManagerStateFlow.value = TagManagerState(tags = SAMPLE_TAGS)

        verify(exactly = 0) { mockUserRepository.defaultTags = any() }
    }

    @Test
    fun `WHEN the content is UserPreferencesContent THEN the offline copies size is read`() = runTest {
        // GIVEN
        coEvery { mockOfflineCopyRepository.totalSizeOnDisk() } returns 2_048L

        // WHEN
        appStateFlow.value = createAppState(content = mockk<UserPreferencesContent>())

        // THEN
        assertThat(userPreferencesViewModel.offlineCopiesSize.first()).isEqualTo(2_048L)
    }

    @Test
    fun `WHEN the content is not UserPreferencesContent THEN the offline copies size is not read`() = runTest {
        // WHEN
        appStateFlow.value = createAppState(content = mockk())

        // THEN
        coVerify(exactly = 0) { mockOfflineCopyRepository.totalSizeOnDisk() }
        assertThat(userPreferencesViewModel.offlineCopiesSize.first()).isEqualTo(0L)
    }

    @Test
    fun `WHEN the preferences are revisited THEN the offline copies size is read again`() = runTest {
        // GIVEN
        coEvery { mockOfflineCopyRepository.totalSizeOnDisk() } returnsMany listOf(2_048L, 4_096L)

        // WHEN
        appStateFlow.value = createAppState(content = mockk<UserPreferencesContent>())
        appStateFlow.value = createAppState(content = mockk())
        appStateFlow.value = createAppState(content = mockk<UserPreferencesContent>())

        // THEN a copy saved elsewhere in the app is reflected without restarting
        assertThat(userPreferencesViewModel.offlineCopiesSize.first()).isEqualTo(4_096L)
    }

    @Test
    fun `WHEN clearOfflineCopies is called THEN every copy is deleted AND the size is read again`() = runTest {
        // GIVEN
        coEvery { mockOfflineCopyRepository.totalSizeOnDisk() } returnsMany listOf(4_096L, 0L)
        appStateFlow.value = createAppState(content = mockk<UserPreferencesContent>())

        assertThat(userPreferencesViewModel.offlineCopiesSize.first()).isEqualTo(4_096L)

        // WHEN
        userPreferencesViewModel.clearOfflineCopies()

        // THEN the size is re-read after the deletion, not before — otherwise the screen would keep
        // showing the old total until it was revisited.
        coVerifyOrder {
            mockOfflineCopyRepository.deleteEverything()
            mockOfflineCopyRepository.totalSizeOnDisk()
        }
        assertThat(userPreferencesViewModel.offlineCopiesSize.first()).isEqualTo(0L)
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

    @ParameterizedTest
    @MethodSource("preferredDateFormats")
    fun `WHEN savePreferredDateFormat is called THEN repository is updated`(
        testCase: PreferredDateFormat,
    ) {
        // GIVEN
        val randomBoolean = randomBoolean()

        // WHEN
        userPreferencesViewModel.savePreferredDateFormat(
            preferredDateFormat = testCase,
            includeTime = randomBoolean,
        )

        // THEN
        verify {
            mockUserRepository.preferredDateFormat = when (testCase) {
                is PreferredDateFormat.DayMonthYearWithTime -> testCase.copy(includeTime = randomBoolean)
                is PreferredDateFormat.MonthDayYearWithTime -> testCase.copy(includeTime = randomBoolean)
                is PreferredDateFormat.ShortYearMonthDayWithTime -> testCase.copy(includeTime = randomBoolean)
                is PreferredDateFormat.YearMonthDayWithTime -> testCase.copy(includeTime = randomBoolean)
                is PreferredDateFormat.NoDate -> testCase
            }
        }
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

    @Test
    fun `WHEN saveUseBackgroundShareReceiver is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveUseBackgroundShareReceiver(value)

        // THEN
        verify { mockUserRepository.useBackgroundShareReceiver = value }
    }

    companion object {

        @JvmStatic
        fun preferredDateFormats(): List<PreferredDateFormat> {
            return listOf(
                PreferredDateFormat.DayMonthYearWithTime(),
                PreferredDateFormat.MonthDayYearWithTime(),
                PreferredDateFormat.ShortYearMonthDayWithTime(),
                PreferredDateFormat.YearMonthDayWithTime(),
                PreferredDateFormat.NoDate,
            )
        }
    }
}
