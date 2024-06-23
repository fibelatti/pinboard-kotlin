package com.fibelatti.bookmarking.core.persistence

import com.fibelatti.bookmarking.randomBoolean
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class UserSharedPreferencesTest {

    private val mockSettings = mockk<Settings>(relaxUnitFun = true)

    private val userSharedPreferences = UserSharedPreferences(mockSettings)

    @Test
    fun `GIVEN KEY_AUTH_TOKEN has no value WHEN getAuthToken is called THEN empty string is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_AUTH_TOKEN, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.authToken).isEmpty()
    }

    @Test
    fun `GIVEN KEY_AUTH_TOKEN has value WHEN getAuthToken is called THEN value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_AUTH_TOKEN, "") } returns MOCK_API_TOKEN

        // THEN
        assertThat(userSharedPreferences.authToken).isEqualTo(MOCK_API_TOKEN)
    }

    @Test
    fun `WHEN setAuthToken is called THEN KEY_AUTH_TOKEN is set`() {
        // WHEN
        userSharedPreferences.authToken = MOCK_API_TOKEN

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_AUTH_TOKEN, MOCK_API_TOKEN) }
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has no value WHEN getLastUpdate is called THEN empty string is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_LAST_UPDATE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.lastUpdate).isEmpty()
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has value WHEN getLastUpdate is called THEN value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_LAST_UPDATE, "") } returns MOCK_TIME

        // THEN
        assertThat(userSharedPreferences.lastUpdate).isEqualTo(MOCK_TIME)
    }

    @Test
    fun `WHEN setLastUpdate is called THEN KEY_LAST_UPDATE is set`() {
        // WHEN
        userSharedPreferences.lastUpdate = MOCK_TIME

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_LAST_UPDATE, MOCK_TIME) }
    }

    @Test
    fun `GIVEN KEY_PERIODIC_SYNC has no value WHEN periodicSync getter is called THEN 24 is returned`() {
        // GIVEN
        every { mockSettings.getLong(UserSharedPreferences.KEY_PERIODIC_SYNC, 24L) } returns 24L

        // THEN
        assertThat(userSharedPreferences.periodicSync).isEqualTo(24L)
    }

    @Test
    fun `GIVEN KEY_PERIODIC_SYNC has value WHEN periodicSync getter is called THEN the value is returned`() {
        // GIVEN
        every { mockSettings.getLong(UserSharedPreferences.KEY_PERIODIC_SYNC, 24L) } returns 42L

        // THEN
        assertThat(userSharedPreferences.periodicSync).isEqualTo(42L)
    }

    @Test
    fun `WHEN periodicSync setter is called THEN KEY_PERIODIC_SYNC is set`() {
        // WHEN
        userSharedPreferences.periodicSync = 42

        // THEN
        verify { mockSettings.putLong(UserSharedPreferences.KEY_PERIODIC_SYNC, 42) }
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_APPEARANCE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.appearance).isEmpty()
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_APPEARANCE, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.appearance).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setAppearance is called THEN KEY_APPEARANCE is set`() {
        // WHEN
        userSharedPreferences.appearance = "some-value"

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_APPEARANCE, "some-value") }
    }

    @Test
    fun `GIVEN KEY_APPLY_DYNAMIC_COLORS has no value WHEN applyDynamicColors is called THEN false is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_APPLY_DYNAMIC_COLORS, false) } returns false

        // THEN
        assertThat(userSharedPreferences.applyDynamicColors).isFalse()
    }

    @Test
    fun `GIVEN KEY_APPLY_DYNAMIC_COLORS has value WHEN applyDynamicColors is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_APPLY_DYNAMIC_COLORS, false) } returns value

        // THEN
        assertThat(userSharedPreferences.applyDynamicColors).isEqualTo(value)
    }

    @Test
    fun `WHEN applyDynamicColors is called THEN KEY_APPLY_DYNAMIC_COLORS is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.applyDynamicColors = value

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_APPLY_DYNAMIC_COLORS, value) }
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DATE_FORMAT has no value WHEN preferredDateFormat getter is called THEN an empty string is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_PREFERRED_DATE_FORMAT, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.preferredDateFormat).isEmpty()
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DATE_FORMAT has value WHEN preferredDateFormat getter is called THEN the value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_PREFERRED_DATE_FORMAT, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.preferredDateFormat).isEqualTo("some-value")
    }

    @Test
    fun `WHEN preferredDateFormat setter is called THEN KEY_PREFERRED_DATE_FORMAT is set`() {
        // WHEN
        userSharedPreferences.preferredDateFormat = "some-value"

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_PREFERRED_DATE_FORMAT, "some-value") }
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_PREFERRED_DETAILS_VIEW, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.preferredDetailsView).isEmpty()
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_PREFERRED_DETAILS_VIEW, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.preferredDetailsView).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setPreferredDetailsView is called THEN KEY_PREFERRED_DETAILS_VIEW is set`() {
        // WHEN
        userSharedPreferences.preferredDetailsView = "some-value"

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_PREFERRED_DETAILS_VIEW, "some-value") }
    }

    @Test
    fun `WHEN alwaysUseSidePanel is called THEN KEY_ALWAYS_USE_SIDE_PANEL is set`() {
        val randomBoolean = randomBoolean()

        // WHEN
        userSharedPreferences.alwaysUseSidePanel = randomBoolean

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_ALWAYS_USE_SIDE_PANEL, randomBoolean) }
    }

    @Test
    fun `GIVEN KEY_ALWAYS_USE_SIDE_PANEL has value WHEN alwaysUseSidePanel is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_ALWAYS_USE_SIDE_PANEL, false) } returns value

        // THEN
        assertThat(userSharedPreferences.alwaysUseSidePanel).isEqualTo(value)
    }

    @Test
    fun `GIVEN KEY_ALWAYS_USE_SIDE_PANEL has no value WHEN alwaysUseSidePanel is called THEN false is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_ALWAYS_USE_SIDE_PANEL, false) } returns false

        // THEN
        assertThat(userSharedPreferences.alwaysUseSidePanel).isFalse()
    }

    @Test
    fun `WHEN setMarkAsReadOnOpen is called THEN KEY_MARK_AS_READ_ON_OPEN is set`() {
        val randomBoolean = randomBoolean()

        // WHEN
        userSharedPreferences.markAsReadOnOpen = randomBoolean

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_MARK_AS_READ_ON_OPEN, randomBoolean) }
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has value WHEN getMarkAsReadOnOpen is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_MARK_AS_READ_ON_OPEN, false) } returns value

        // THEN
        assertThat(userSharedPreferences.markAsReadOnOpen).isEqualTo(value)
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has no value WHEN getMarkAsReadOnOpen is called THEN false is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_MARK_AS_READ_ON_OPEN, false) } returns false

        // THEN
        assertThat(userSharedPreferences.markAsReadOnOpen).isFalse()
    }

    @Test
    fun `WHEN getDescriptionAutoFill is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_AUTO_FILL_DESCRIPTION, false) } returns value

        // THEN
        assertThat(userSharedPreferences.autoFillDescription).isEqualTo(value)
    }

    @Test
    fun `WHEN setDescriptionAutoFill is called THEN KEY_DESCRIPTION_AUTO_FILL is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.autoFillDescription = value

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_AUTO_FILL_DESCRIPTION, value) }
    }

    @Test
    fun `WHEN getDescriptionVisibleInList is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_SHOW_DESCRIPTION_IN_LISTS, true) } returns value

        // THEN
        assertThat(userSharedPreferences.showDescriptionInLists).isEqualTo(value)
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_DESCRIPTION_VISIBLE_LIST is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.showDescriptionInLists = value

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_SHOW_DESCRIPTION_IN_LISTS, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns false THEN null is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_DEFAULT_PRIVATE, false) } returns false

        // THEN
        assertThat(userSharedPreferences.defaultPrivate).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns true THEN true is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_DEFAULT_PRIVATE, false) } returns true

        // THEN
        assertThat(userSharedPreferences.defaultPrivate).isTrue()
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN KEY_DEFAULT_PRIVATE is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.defaultPrivate = value

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_DEFAULT_PRIVATE, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns false THEN null is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_DEFAULT_READ_LATER, false) } returns false

        // THEN
        assertThat(userSharedPreferences.defaultReadLater).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns true THEN true is returned`() {
        // GIVEN
        every { mockSettings.getBoolean(UserSharedPreferences.KEY_DEFAULT_READ_LATER, false) } returns true

        // THEN
        assertThat(userSharedPreferences.defaultReadLater).isTrue()
    }

    @Test
    fun `WHEN setDefaultReadLater is called THEN KEY_DEFAULT_READ_LATER is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.defaultReadLater = value

        // THEN
        verify { mockSettings.putBoolean(UserSharedPreferences.KEY_DEFAULT_READ_LATER, value) }
    }

    @Test
    fun `WHEN getEditAfterSharing is called THEN its value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_NEW_EDIT_AFTER_SHARING, "") } returns "BEFORE_SAVING"

        // THEN
        assertThat(userSharedPreferences.editAfterSharing).isEqualTo("BEFORE_SAVING")
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_NEW_EDIT_AFTER_SHARING is set`() {
        // WHEN
        userSharedPreferences.editAfterSharing = "BEFORE_SAVING"

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_NEW_EDIT_AFTER_SHARING, "BEFORE_SAVING") }
    }

    @Test
    fun `WHEN getDefaultTags is called AND the value is empty THEN empty list is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_DEFAULT_TAGS, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.defaultTags).isEmpty()
    }

    @Test
    fun `WHEN getDefaultTags is called THEN its value is returned`() {
        // GIVEN
        every { mockSettings.getString(UserSharedPreferences.KEY_DEFAULT_TAGS, "") } returns "test"

        // THEN
        assertThat(userSharedPreferences.defaultTags).isEqualTo(listOf("test"))
    }

    @Test
    fun `WHEN setDefaultTags is called THEN KEY_DEFAULT_TAGS is set`() {
        // GIVEN
        val value = listOf("test", "another-test")

        // WHEN
        userSharedPreferences.defaultTags = value

        // THEN
        verify { mockSettings.putString(UserSharedPreferences.KEY_DEFAULT_TAGS, "test,another-test") }
    }

    private companion object {

        private const val MOCK_API_TOKEN = "user:00000000000"
        private const val MOCK_TIME = "2019-01-10T08:20:10Z"
    }
}
