package com.fibelatti.pinboard.core.persistence

import android.content.SharedPreferences
import com.fibelatti.core.android.extension.get
import com.fibelatti.core.test.MockSharedPreferencesEditor
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.randomString
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class UserSharedPreferencesTest {

    private val mockEditor = spyk(MockSharedPreferencesEditor())
    private val mockSharedPreferences = mockk<SharedPreferences> {
        every { edit() } returns mockEditor
    }

    private val userSharedPreferences = UserSharedPreferences(mockSharedPreferences)

    @Test
    fun `GIVEN APP_REVIEW_MODE has no value WHEN appReviewMode is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get("APP_REVIEW_MODE", false) } returns false

        // THEN
        assertThat(userSharedPreferences.appReviewMode).isFalse()
    }

    @Test
    fun `GIVEN APP_REVIEW_MODE has value WHEN appReviewMode is called THEN value is returned`() {
        // GIVEN
        val result = randomBoolean()
        every { mockSharedPreferences.get("APP_REVIEW_MODE", false) } returns result

        // THEN
        assertThat(userSharedPreferences.appReviewMode).isEqualTo(result)
    }

    @Test
    fun `GIVEN LINKDING_INSTANCE_URL has no value WHEN linkdingInstanceUrl is called THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.getString("LINKDING_INSTANCE_URL", null) } returns null

        // THEN
        assertThat(userSharedPreferences.linkdingInstanceUrl).isNull()
    }

    @Test
    fun `GIVEN LINKDING_INSTANCE_URL has value WHEN linkdingInstanceUrl is called THEN value is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("LINKDING_INSTANCE_URL", null) } returns result

        // THEN
        assertThat(userSharedPreferences.linkdingInstanceUrl).isEqualTo(result)
    }

    @Test
    fun `GIVEN AUTH_TOKEN had value AND USE_LINKDING was true WHEN linkdingAuthToken is called THEN value is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns result
        every { mockSharedPreferences.get("USE_LINKDING", false) } returns true
        every { mockSharedPreferences.get("LINKDING_AUTH_TOKEN", result) } returns result

        // THEN
        assertThat(userSharedPreferences.linkdingAuthToken).isEqualTo(result)
    }

    @Test
    fun `GIVEN AUTH_TOKEN had value AND USE_LINKDING was false WHEN linkdingAuthToken is called THEN null is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns result
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.getString("LINKDING_AUTH_TOKEN", null) } returns null

        // THEN
        assertThat(userSharedPreferences.linkdingAuthToken).isNull()
    }

    @Test
    fun `GIVEN LINKDING_AUTH_TOKEN has value WHEN linkdingAuthToken is called THEN value is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns null
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.getString("LINKDING_AUTH_TOKEN", null) } returns result

        // THEN
        assertThat(userSharedPreferences.linkdingAuthToken).isEqualTo(result)
    }

    @Test
    fun `GIVEN LINKDING_AUTH_TOKEN has no value WHEN linkdingAuthToken is called THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns null
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.getString("LINKDING_AUTH_TOKEN", null) } returns null

        // THEN
        assertThat(userSharedPreferences.linkdingAuthToken).isNull()
    }

    @Test
    fun `WHEN linkdingAuthToken is set THEN LINKDING_AUTH_TOKEN is set`() {
        // GIVEN
        val result = randomString()

        // WHEN
        userSharedPreferences.linkdingAuthToken = result

        // THEN
        verify { mockEditor.putString("LINKDING_AUTH_TOKEN", result) }
    }

    @Test
    fun `GIVEN AUTH_TOKEN had value AND USE_LINKDING was false WHEN pinboardAuthToken is called THEN value is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns result
        every { mockSharedPreferences.get("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.get("PINBOARD_AUTH_TOKEN", result) } returns result

        // THEN
        assertThat(userSharedPreferences.pinboardAuthToken).isEqualTo(result)
    }

    @Test
    fun `GIVEN AUTH_TOKEN had value AND USE_LINKDING was true WHEN pinboardAuthToken is called THEN null is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns result
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns true
        every { mockSharedPreferences.getString("PINBOARD_AUTH_TOKEN", null) } returns null

        // THEN
        assertThat(userSharedPreferences.pinboardAuthToken).isNull()
    }

    @Test
    fun `GIVEN PINBOARD_AUTH_TOKEN has value WHEN pinboardAuthToken is called THEN value is returned`() {
        // GIVEN
        val result = randomString()
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns null
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.getString("PINBOARD_AUTH_TOKEN", null) } returns result

        // THEN
        assertThat(userSharedPreferences.pinboardAuthToken).isEqualTo(result)
    }

    @Test
    fun `GIVEN PINBOARD_AUTH_TOKEN has no value WHEN pinboardAuthToken is called THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.getString("AUTH_TOKEN", null) } returns null
        every { mockSharedPreferences.getBoolean("USE_LINKDING", false) } returns false
        every { mockSharedPreferences.getString("PINBOARD_AUTH_TOKEN", null) } returns null

        // THEN
        assertThat(userSharedPreferences.pinboardAuthToken).isNull()
    }

    @Test
    fun `WHEN pinboardAuthToken is set THEN PINBOARD_AUTH_TOKEN is set`() {
        // GIVEN
        val result = randomString()

        // WHEN
        userSharedPreferences.pinboardAuthToken = result

        // THEN
        verify { mockEditor.putString("PINBOARD_AUTH_TOKEN", result) }
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has no value WHEN getLastUpdate is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_LAST_UPDATE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.lastUpdate).isEmpty()
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has value WHEN getLastUpdate is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_LAST_UPDATE, "") } returns SAMPLE_DATE_TIME

        // THEN
        assertThat(userSharedPreferences.lastUpdate).isEqualTo(SAMPLE_DATE_TIME)
    }

    @Test
    fun `WHEN setLastUpdate is called THEN KEY_LAST_UPDATE is set`() {
        // WHEN
        userSharedPreferences.lastUpdate = SAMPLE_DATE_TIME

        // THEN
        verify { mockEditor.putString(KEY_LAST_UPDATE, SAMPLE_DATE_TIME) }
    }

    @Test
    fun `GIVEN KEY_PERIODIC_SYNC has no value WHEN periodicSync getter is called THEN 24 is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PERIODIC_SYNC, 24L) } returns 24L

        // THEN
        assertThat(userSharedPreferences.periodicSync).isEqualTo(24L)
    }

    @Test
    fun `GIVEN KEY_PERIODIC_SYNC has value WHEN periodicSync getter is called THEN the value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PERIODIC_SYNC, 24L) } returns 42L

        // THEN
        assertThat(userSharedPreferences.periodicSync).isEqualTo(42L)
    }

    @Test
    fun `WHEN periodicSync setter is called THEN KEY_PERIODIC_SYNC is set`() {
        // WHEN
        userSharedPreferences.periodicSync = 42

        // THEN
        verify { mockEditor.putLong(KEY_PERIODIC_SYNC, 42) }
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_APPEARANCE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.appearance).isEmpty()
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_APPEARANCE, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.appearance).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setAppearance is called THEN KEY_APPEARANCE is set`() {
        // WHEN
        userSharedPreferences.appearance = "some-value"

        // THEN
        verify { mockEditor.putString(KEY_APPEARANCE, "some-value") }
    }

    @Test
    fun `GIVEN KEY_APPLY_DYNAMIC_COLORS has no value WHEN applyDynamicColors is called THEN false is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_APPLY_DYNAMIC_COLORS, false) } returns false

        // THEN
        assertThat(userSharedPreferences.applyDynamicColors).isFalse()
    }

    @Test
    fun `GIVEN KEY_APPLY_DYNAMIC_COLORS has value WHEN applyDynamicColors is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_APPLY_DYNAMIC_COLORS, false) } returns value

        // THEN
        assertThat(userSharedPreferences.applyDynamicColors).isEqualTo(value)
    }

    @Test
    fun `WHEN applyDynamicColors is called THEN KEY_APPLY_DYNAMIC_COLORS is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.applyDynamicColors = value

        // THEN
        verify { mockEditor.putBoolean(KEY_APPLY_DYNAMIC_COLORS, value) }
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DATE_FORMAT has no value WHEN preferredDateFormat getter is called THEN an empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DATE_FORMAT, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.preferredDateFormat).isEmpty()
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DATE_FORMAT has value WHEN preferredDateFormat getter is called THEN the value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DATE_FORMAT, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.preferredDateFormat).isEqualTo("some-value")
    }

    @Test
    fun `WHEN preferredDateFormat setter is called THEN KEY_PREFERRED_DATE_FORMAT is set`() {
        // WHEN
        userSharedPreferences.preferredDateFormat = "some-value"

        // THEN
        verify { mockEditor.putString(KEY_PREFERRED_DATE_FORMAT, "some-value") }
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.preferredDetailsView).isEmpty()
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.preferredDetailsView).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setPreferredDetailsView is called THEN KEY_PREFERRED_DETAILS_VIEW is set`() {
        // WHEN
        userSharedPreferences.preferredDetailsView = "some-value"

        // THEN
        verify { mockEditor.putString(KEY_PREFERRED_DETAILS_VIEW, "some-value") }
    }

    @Test
    fun `WHEN setMarkAsReadOnOpen is called THEN KEY_MARK_AS_READ_ON_OPEN is set`() {
        val randomBoolean = randomBoolean()

        // WHEN
        userSharedPreferences.markAsReadOnOpen = randomBoolean

        // THEN
        verify { mockEditor.putBoolean(KEY_MARK_AS_READ_ON_OPEN, randomBoolean) }
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has value WHEN getMarkAsReadOnOpen is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false) } returns value

        // THEN
        assertThat(userSharedPreferences.markAsReadOnOpen).isEqualTo(value)
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has no value WHEN getMarkAsReadOnOpen is called THEN false is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false) } returns false

        // THEN
        assertThat(userSharedPreferences.markAsReadOnOpen).isFalse()
    }

    @Test
    fun `WHEN followRedirects getter is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_FOLLOW_REDIRECTS, true) } returns value

        // THEN
        assertThat(userSharedPreferences.followRedirects).isEqualTo(value)
    }

    @Test
    fun `WHEN followRedirects setter is called THEN KEY_FOLLOW_REDIRECTS is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.followRedirects = value

        // THEN
        verify { mockEditor.putBoolean(KEY_FOLLOW_REDIRECTS, value) }
    }

    @Test
    fun `WHEN getDescriptionAutoFill is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false) } returns value

        // THEN
        assertThat(userSharedPreferences.autoFillDescription).isEqualTo(value)
    }

    @Test
    fun `WHEN setDescriptionAutoFill is called THEN KEY_DESCRIPTION_AUTO_FILL is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.autoFillDescription = value

        // THEN
        verify { mockEditor.putBoolean(KEY_AUTO_FILL_DESCRIPTION, value) }
    }

    @Test
    fun `WHEN getDescriptionVisibleInList is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, true) } returns value

        // THEN
        assertThat(userSharedPreferences.showDescriptionInLists).isEqualTo(value)
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_DESCRIPTION_VISIBLE_LIST is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.showDescriptionInLists = value

        // THEN
        verify { mockEditor.putBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns false THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false) } returns false

        // THEN
        assertThat(userSharedPreferences.defaultPrivate).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns true THEN true is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false) } returns true

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
        verify { mockEditor.putBoolean(KEY_DEFAULT_PRIVATE, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns false THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false) } returns false

        // THEN
        assertThat(userSharedPreferences.defaultReadLater).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns true THEN true is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false) } returns true

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
        verify { mockEditor.putBoolean(KEY_DEFAULT_READ_LATER, value) }
    }

    @Test
    fun `WHEN getEditAfterSharing is called THEN its value is returned`() {
        // GIVEN
        every {
            mockSharedPreferences.get(KEY_NEW_EDIT_AFTER_SHARING, "")
        } returns "BEFORE_SAVING"

        // THEN
        assertThat(userSharedPreferences.editAfterSharing).isEqualTo("BEFORE_SAVING")
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_NEW_EDIT_AFTER_SHARING is set`() {
        // WHEN
        userSharedPreferences.editAfterSharing = "BEFORE_SAVING"

        // THEN
        verify { mockEditor.putString(KEY_NEW_EDIT_AFTER_SHARING, "BEFORE_SAVING") }
    }

    @Test
    fun `WHEN getDefaultTags is called AND the value is empty THEN empty list is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_TAGS, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.defaultTags).isEmpty()
    }

    @Test
    fun `WHEN getDefaultTags is called THEN its value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_TAGS, "") } returns "test"

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
        verify { mockEditor.putString(KEY_DEFAULT_TAGS, "test,another-test") }
    }
}
