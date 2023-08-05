package com.fibelatti.pinboard.features.user.data

import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class UserDataSourceTest {

    private val mockUserSharedPreferences = mockk<UserSharedPreferences>(relaxed = true) {
        every { autoUpdate } returns true
        every { periodicSync } returns 0L
        every { appearance } returns ""
        every { applyDynamicColors } returns false
        every { preferredDateFormat } returns ""
        every { preferredDetailsView } returns ""
        every { alwaysUseSidePanel } returns false
        every { autoFillDescription } returns false
        every { showDescriptionInLists } returns false
        every { defaultPrivate } returns null
        every { defaultReadLater } returns null
        every { editAfterSharing } returns ""
        every { defaultTags } returns emptyList()
    }

    private val defaultPreferences = UserPreferences(
        autoUpdate = true,
        periodicSync = PeriodicSync.Off,
        appearance = Appearance.SystemDefault,
        applyDynamicColors = false,
        preferredDateFormat = PreferredDateFormat.DayMonthYearWithTime,
        preferredDetailsView = PreferredDetailsView.InAppBrowser(markAsReadOnOpen = false),
        alwaysUseSidePanel = false,
        autoFillDescription = false,
        showDescriptionInLists = false,
        defaultPrivate = false,
        defaultReadLater = false,
        editAfterSharing = EditAfterSharing.AfterSaving,
        defaultTags = emptyList(),
    )

    private val userDataSource = UserDataSource(mockUserSharedPreferences, mainVariant = true)

    @Nested
    inner class InitialisationTests {

        @Test
        fun `currentPreferences will contain the initial state`() = runTest {
            assertThat(userDataSource.currentPreferences.first()).isEqualTo(defaultPreferences)
        }
    }

    @Nested
    inner class Methods {

        @BeforeEach
        fun setup() {
            every { mockUserSharedPreferences.authToken } returns mockApiToken
        }

        @Nested
        inner class LastUpdate {

            @Test
            fun `WHEN getLastUpdate is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.lastUpdate } returns mockTime

                // THEN
                assertThat(userDataSource.lastUpdate).isEqualTo(mockTime)
            }

            @Test
            fun `WHEN setLastUpdate is called THEN UserSharedPreferences is set`() {
                // WHEN
                userDataSource.lastUpdate = mockTime

                // THEN
                verify { mockUserSharedPreferences.lastUpdate = mockTime }
            }
        }

        @Nested
        inner class PeriodicSyncTests {

            @Test
            fun `GIVEN the shared preferences has Off value WHEN the getter is called THEN Off is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.periodicSync } returns PeriodicSync.Off.hours

                // THEN
                assertThat(userDataSource.periodicSync).isEqualTo(PeriodicSync.Off)
            }

            @Test
            fun `GIVEN the shared preferences has Every6Hours value WHEN the getter is called THEN Every6Hours is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.periodicSync } returns PeriodicSync.Every6Hours.hours

                // THEN
                assertThat(userDataSource.periodicSync).isEqualTo(PeriodicSync.Every6Hours)
            }

            @Test
            fun `GIVEN the shared preferences has Every12Hours value WHEN the getter is called THEN Every12Hours is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.periodicSync } returns PeriodicSync.Every12Hours.hours

                // THEN
                assertThat(userDataSource.periodicSync).isEqualTo(PeriodicSync.Every12Hours)
            }

            @Test
            fun `GIVEN the shared preferences has Every24Hours value WHEN the getter is called THEN Every24Hours is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.periodicSync } returns PeriodicSync.Every24Hours.hours

                // THEN
                assertThat(userDataSource.periodicSync).isEqualTo(PeriodicSync.Every24Hours)
            }

            @Test
            fun `GIVEN the shared preferences has any other value WHEN the getter is called THEN Off is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.periodicSync } returns 42

                // THEN
                assertThat(userDataSource.periodicSync).isEqualTo(PeriodicSync.Off)
            }

            @Test
            fun `WHEN setter is called THEN the shared preferences is updated`() {
                // GIVEN
                val mockPeriodicSync = mockk<PeriodicSync>()
                every { mockPeriodicSync.hours } returns 24

                // WHEN
                userDataSource.periodicSync = mockPeriodicSync

                // THEN
                verify { mockUserSharedPreferences.periodicSync = 24 }
            }
        }

        @Nested
        inner class AppearanceTests {

            @Test
            fun `GIVEN set appearance is the light theme WHEN getAppearance is called THEN LightTheme is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.appearance } returns Appearance.LightTheme.value

                // THEN
                assertThat(userDataSource.appearance).isEqualTo(Appearance.LightTheme)
            }

            @Test
            fun `GIVEN set appearance is the dark theme WHEN getAppearance is called THEN DarkTheme is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.appearance } returns Appearance.DarkTheme.value

                // THEN
                assertThat(userDataSource.appearance).isEqualTo(Appearance.DarkTheme)
            }

            @Test
            fun `GIVEN set appearance is system default WHEN getAppearance is called THEN SystemDefault is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.appearance } returns Appearance.SystemDefault.value

                // THEN
                assertThat(userDataSource.appearance).isEqualTo(Appearance.SystemDefault)
            }

            @Test
            fun `GIVEN set appearance is not set WHEN getAppearance is called THEN SystemDefault is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.appearance } returns "anything really"

                // THEN
                assertThat(userDataSource.appearance).isEqualTo(Appearance.SystemDefault)
            }

            @Test
            fun `WHEN setAppearance is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockAppearance = mockk<Appearance>()
                every { mockAppearance.value } returns "random-value"

                // WHEN
                userDataSource.appearance = mockAppearance

                // THEN
                verify { mockUserSharedPreferences.appearance = "random-value" }
            }
        }

        @Nested
        inner class PreferredDateFormatTests {

            @Test
            fun `GIVEN the shared preferences has DayMonthYearWithTime value WHEN the getter is called THEN DayMonthYearWithTime is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime.value

                // THEN
                assertThat(userDataSource.preferredDateFormat).isEqualTo(PreferredDateFormat.DayMonthYearWithTime)
            }

            @Test
            fun `GIVEN the shared preferences has MonthDayYearWithTime value WHEN the getter is called THEN MonthDayYearWithTime is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDateFormat } returns PreferredDateFormat.MonthDayYearWithTime.value

                // THEN
                assertThat(userDataSource.preferredDateFormat).isEqualTo(PreferredDateFormat.MonthDayYearWithTime)
            }

            @Test
            fun `GIVEN the shared preferences has ShortYearMonthDayWithTime value WHEN the getter is called THEN YearMonthDayWithTime is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDateFormat } returns PreferredDateFormat.ShortYearMonthDayWithTime.value

                // THEN
                assertThat(userDataSource.preferredDateFormat).isEqualTo(PreferredDateFormat.ShortYearMonthDayWithTime)
            }

            @Test
            fun `GIVEN the shared preferences has YearMonthDayWithTime value WHEN the getter is called THEN YearMonthDayWithTime is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDateFormat } returns PreferredDateFormat.YearMonthDayWithTime.value

                // THEN
                assertThat(userDataSource.preferredDateFormat).isEqualTo(PreferredDateFormat.YearMonthDayWithTime)
            }

            @Test
            fun `GIVEN the shared preferences has any other value WHEN the getter is called THEN DayMonthYearWithTime is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDateFormat } returns "anything really"

                // THEN
                assertThat(userDataSource.preferredDateFormat).isEqualTo(PreferredDateFormat.DayMonthYearWithTime)
            }

            @Test
            fun `WHEN setter is called THEN the shared preferences is updated`() {
                // GIVEN
                val mockPreferredDateFormat = mockk<PreferredDateFormat>()
                every { mockPreferredDateFormat.value } returns "random-value"

                // WHEN
                userDataSource.preferredDateFormat = mockPreferredDateFormat

                // THEN
                verify { mockUserSharedPreferences.preferredDateFormat = "random-value" }
            }
        }

        @Nested
        inner class PreferredDetailsViewTests {

            @Test
            fun `GIVEN set preferred details view is in app browser WHEN getPreferredDetailsView is called THEN InAppBrowser is returned`() {
                // GIVEN
                val randomBoolean = randomBoolean()
                every { mockUserSharedPreferences.preferredDetailsView } returns PreferredDetailsView.InAppBrowser(
                    randomBoolean,
                ).value
                every { mockUserSharedPreferences.markAsReadOnOpen } returns randomBoolean

                // THEN
                assertThat(userDataSource.preferredDetailsView).isEqualTo(
                    PreferredDetailsView.InAppBrowser(randomBoolean),
                )
                verify { mockUserSharedPreferences.markAsReadOnOpen }
            }

            @Test
            fun `GIVEN set preferred details view is external browser WHEN getPreferredDetailsView is called THEN ExternalBrowser is returned`() {
                // GIVEN
                val randomBoolean = randomBoolean()
                every { mockUserSharedPreferences.preferredDetailsView } returns PreferredDetailsView.ExternalBrowser(
                    randomBoolean,
                ).value
                every { mockUserSharedPreferences.markAsReadOnOpen } returns randomBoolean

                // THEN
                assertThat(userDataSource.preferredDetailsView).isEqualTo(
                    PreferredDetailsView.ExternalBrowser(randomBoolean),
                )
                verify { mockUserSharedPreferences.markAsReadOnOpen }
            }

            @Test
            fun `GIVEN set preferred details view is edit WHEN getPreferredDetailsView is called THEN Edit is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.preferredDetailsView } returns PreferredDetailsView.Edit.value

                // THEN
                assertThat(userDataSource.preferredDetailsView).isEqualTo(
                    PreferredDetailsView.Edit,
                )
            }

            @Test
            fun `GIVEN set preferred details view is not specifically handled WHEN getPreferredDetailsView is called THEN InAppBrowser is returned`() {
                // GIVEN
                val randomBoolean = randomBoolean()
                every { mockUserSharedPreferences.preferredDetailsView } returns "anything really"
                every { mockUserSharedPreferences.markAsReadOnOpen } returns randomBoolean

                // THEN
                assertThat(userDataSource.preferredDetailsView).isEqualTo(
                    PreferredDetailsView.InAppBrowser(randomBoolean),
                )
            }

            @Test
            fun `WHEN setPreferredDetailsView is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockPreferredDetailsView = mockk<PreferredDetailsView>()
                every { mockPreferredDetailsView.value } returns "random-value"

                // WHEN
                userDataSource.preferredDetailsView = mockPreferredDetailsView

                // THEN
                verify { mockUserSharedPreferences.preferredDetailsView = "random-value" }
            }
        }

        @Nested
        inner class AlwaysUseSidePanel {

            @Test
            fun `WHEN alwaysUseSidePanel is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.alwaysUseSidePanel } returns value

                // THEN
                assertThat(userDataSource.alwaysUseSidePanel).isEqualTo(value)
            }

            @Test
            fun `WHEN alwaysUseSidePanel is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.alwaysUseSidePanel = value

                // THEN
                verify { mockUserSharedPreferences.alwaysUseSidePanel = value }
            }
        }

        @Nested
        inner class MarkAsReadOnOpen {

            @Test
            fun `WHEN getMarkAsReadOnOpen is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.markAsReadOnOpen } returns value

                // THEN
                assertThat(userDataSource.markAsReadOnOpen).isEqualTo(value)
            }

            @Test
            fun `WHEN setMarkAsReadOnOpen is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.markAsReadOnOpen = value

                // THEN
                verify { mockUserSharedPreferences.markAsReadOnOpen = value }
            }
        }

        @Nested
        inner class AutoFill {

            @Test
            fun `WHEN getAutoFillDescription is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.autoFillDescription } returns value

                // THEN
                assertThat(userDataSource.autoFillDescription).isEqualTo(value)
            }

            @Test
            fun `WHEN setAutoFillDescription is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.autoFillDescription = value

                // THEN
                verify { mockUserSharedPreferences.autoFillDescription = value }
            }
        }

        @Nested
        inner class DescriptionInLists {

            @Test
            fun `WHEN getShowDescriptionInLists is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.showDescriptionInLists } returns value

                // THEN
                assertThat(userDataSource.showDescriptionInLists).isEqualTo(value)
            }

            @Test
            fun `WHEN setShowDescriptionInLists is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.showDescriptionInLists = value

                // THEN
                verify { mockUserSharedPreferences.showDescriptionInLists = value }
            }
        }

        @Nested
        inner class DefaultPrivate {

            @Test
            fun `WHEN getDefaultPrivate is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.defaultPrivate } returns value

                // THEN
                assertThat(userDataSource.defaultPrivate).isEqualTo(value)
            }

            @Test
            fun `WHEN setDefaultPrivate is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.defaultPrivate = value

                // THEN
                verify { mockUserSharedPreferences.defaultPrivate = value }
            }
        }

        @Nested
        inner class DefaultReadLater {

            @Test
            fun `WHEN getDefaultReadLater is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.defaultReadLater } returns value

                // THEN
                assertThat(userDataSource.defaultReadLater).isEqualTo(value)
            }

            @Test
            fun `WHEN setDefaultReadLater is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.defaultReadLater = value

                // THEN
                verify { mockUserSharedPreferences.defaultReadLater = value }
            }
        }

        @Nested
        inner class DefaultTags {

            private val mockTagValues = listOf("test")
            private val mockTags = listOf(Tag("test"))

            @Test
            fun `WHEN getDefaultTags is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.defaultTags } returns mockTagValues

                // THEN
                assertThat(userDataSource.defaultTags).isEqualTo(mockTags)
            }

            @Test
            fun `WHEN setDefaultTags is called THEN UserSharedPreferences is set`() {
                // WHEN
                userDataSource.defaultTags = mockTags

                // THEN
                verify { mockUserSharedPreferences.defaultTags = mockTagValues }
            }
        }

        @Nested
        inner class EditAfterSharingTests {

            @Test
            fun `GIVEN set EditAfterSharing is BeforeSaving value WHEN getEditAfterSharing is called THEN BeforeSaving is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.editAfterSharing } returns EditAfterSharing.BeforeSaving.value

                // THEN
                assertThat(userDataSource.editAfterSharing).isEqualTo(EditAfterSharing.BeforeSaving)
            }

            @Test
            fun `GIVEN set EditAfterSharing is AfterSaving value WHEN getEditAfterSharing is called THEN AfterSaving is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.editAfterSharing } returns EditAfterSharing.AfterSaving.value

                // THEN
                assertThat(userDataSource.editAfterSharing).isEqualTo(EditAfterSharing.AfterSaving)
            }

            @Test
            fun `GIVEN set EditAfterSharing is SkipEdit value WHEN getEditAfterSharing is called THEN SkipEdit is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.editAfterSharing } returns EditAfterSharing.SkipEdit.value

                // THEN
                assertThat(userDataSource.editAfterSharing).isEqualTo(EditAfterSharing.SkipEdit)
            }

            @Test
            fun `GIVEN not EditAfterSharing is set WHEN getEditAfterSharing is called THEN AfterSaving is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.editAfterSharing } returns "anything really"

                // THEN
                assertThat(userDataSource.editAfterSharing).isEqualTo(EditAfterSharing.AfterSaving)
            }

            @Test
            fun `WHEN setEditAfterSharing is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockEditAfterSharing = mockk<EditAfterSharing>()
                every { mockEditAfterSharing.value } returns "random-value"

                // WHEN
                userDataSource.editAfterSharing = mockEditAfterSharing

                // THEN
                verify { mockUserSharedPreferences.editAfterSharing = "random-value" }
            }
        }

        @Nested
        inner class AuthTokenTests {

            private val altVariant = UserDataSource(mockUserSharedPreferences, mainVariant = false)

            @Test
            fun `hasAuthToken returns true if the auth token is not empty`() {
                assertThat(userDataSource.hasAuthToken()).isTrue()
            }

            @Test
            fun `hasAuthToken returns true if the main variant is false`() {
                every { mockUserSharedPreferences.authToken } returns ""

                assertThat(altVariant.hasAuthToken()).isTrue()
            }

            @Test
            fun `setAuthToken saves the auth token`() {
                userDataSource.setAuthToken("some-token")

                verify {
                    mockUserSharedPreferences.authToken = "some-token"
                }
            }

            @Test
            fun `setAuthToken does not save the auth token if it is blank`() {
                clearMocks(mockUserSharedPreferences)

                altVariant.setAuthToken(" ")

                verify { mockUserSharedPreferences wasNot Called }
            }

            @Test
            fun `setAuthToken does not save the auth token if main variant is false`() {
                clearMocks(mockUserSharedPreferences)

                altVariant.setAuthToken("some-token")

                verify { mockUserSharedPreferences wasNot Called }
            }

            @Test
            fun `clearAuthToken clears the auth token and last updated`() {
                userDataSource.clearAuthToken()

                verify {
                    mockUserSharedPreferences.authToken = ""
                    mockUserSharedPreferences.lastUpdate = ""
                }
                assertThat(userDataSource.appReviewMode).isFalse()
            }

            @Test
            fun `clearAuthToken does not clear tokens if main variant is false`() {
                clearMocks(mockUserSharedPreferences)

                altVariant.clearAuthToken()

                verify { mockUserSharedPreferences wasNot Called }
            }
        }
    }
}
