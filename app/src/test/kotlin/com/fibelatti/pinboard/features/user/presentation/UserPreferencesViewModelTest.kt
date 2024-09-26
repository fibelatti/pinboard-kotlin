package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class UserPreferencesViewModelTest : BaseViewModelTest() {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockPeriodicSyncManager = mockk<PeriodicSyncManager> {
        every { enqueueWork(any()) } just runs
    }

    private val userPreferencesViewModel = UserPreferencesViewModel(
        userRepository = mockUserRepository,
        postsRepository = mockPostsRepository,
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
    fun `WHEN useLinkding is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.useLinkding(value)

        // THEN
        verify {
            mockUserRepository.useLinkding = value
        }
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
    fun `WHEN savePreferredDetailsView is called THEN repository is updated`() {
        // GIVEN
        val preferredDetailsView = mockk<PreferredDetailsView>()

        // WHEN
        userPreferencesViewModel.savePreferredDetailsView(preferredDetailsView)

        // THEN
        verify { mockUserRepository.preferredDetailsView = preferredDetailsView }
    }

    @Test
    fun `WHEN saveAlwaysUseSidePanel is called THEN repository is updated`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userPreferencesViewModel.saveAlwaysUseSidePanel(value)

        // THEN
        verify { mockUserRepository.alwaysUseSidePanel = value }
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
    fun `WHEN saveDefaultTags is called THEN repository is updated`() {
        // GIVEN
        val value = mockk<List<Tag>>()

        // WHEN
        userPreferencesViewModel.saveDefaultTags(value)

        // THEN
        verify { mockUserRepository.defaultTags = value }
    }

    @Test
    fun `GIVEN getSuggestedTags will fail WHEN searchForTag is called THEN suggestedTags should never receive values`() =
        runTest {
            // GIVEN
            coEvery { mockPostsRepository.searchExistingPostTag(any(), any()) } returns Failure(Exception())

            // WHEN
            userPreferencesViewModel.searchForTag(MockDataProvider.mockTagString1, mockk())

            // THEN
            assertThat(userPreferencesViewModel.suggestedTags.isEmpty()).isTrue()
        }

    @Test
    fun `GIVEN getSuggestedTags will succeed WHEN searchForTag is called THEN suggestedTags should receive its response`() =
        runTest {
            // GIVEN
            val result = listOf(MockDataProvider.mockTagString1, MockDataProvider.mockTagString2)
            coEvery {
                mockPostsRepository.searchExistingPostTag(
                    tag = MockDataProvider.mockTagString1,
                    currentTags = emptyList(),
                )
            } returns Success(result)

            // WHEN
            userPreferencesViewModel.searchForTag(
                tag = MockDataProvider.mockTagString1,
                currentTags = emptyList(),
            )

            // THEN
            assertThat(userPreferencesViewModel.suggestedTags.first()).isEqualTo(result)
        }
}
