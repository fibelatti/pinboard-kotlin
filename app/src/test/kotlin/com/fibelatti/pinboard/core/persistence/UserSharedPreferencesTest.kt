package com.fibelatti.pinboard.core.persistence

import android.content.SharedPreferences
import com.fibelatti.core.extension.get
import com.fibelatti.core.test.MockSharedPreferencesEditor
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.randomBoolean
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
    fun `GIVEN KEY_AUTH_TOKEN has no value WHEN getAuthToken is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_AUTH_TOKEN, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.getAuthToken()).isEmpty()
    }

    @Test
    fun `GIVEN KEY_AUTH_TOKEN has value WHEN getAuthToken is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_AUTH_TOKEN, "") } returns mockApiToken

        // THEN
        assertThat(userSharedPreferences.getAuthToken()).isEqualTo(mockApiToken)
    }

    @Test
    fun `WHEN setAuthToken is called THEN KEY_AUTH_TOKEN is set`() {
        // WHEN
        userSharedPreferences.setAuthToken(mockApiToken)

        // THEN
        verify { mockEditor.putString(KEY_AUTH_TOKEN, mockApiToken) }
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has no value WHEN getLastUpdate is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_LAST_UPDATE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.getLastUpdate()).isEmpty()
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has value WHEN getLastUpdate is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_LAST_UPDATE, "") } returns mockTime

        // THEN
        assertThat(userSharedPreferences.getLastUpdate()).isEqualTo(mockTime)
    }

    @Test
    fun `WHEN setLastUpdate is called THEN KEY_LAST_UPDATE is set`() {
        // WHEN
        userSharedPreferences.setLastUpdate(mockTime)

        // THEN
        verify { mockEditor.putString(KEY_LAST_UPDATE, mockTime) }
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_APPEARANCE, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.getAppearance()).isEmpty()
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_APPEARANCE, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.getAppearance()).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setAppearance is called THEN KEY_APPEARANCE is set`() {
        // WHEN
        userSharedPreferences.setAppearance("some-value")

        // THEN
        verify { mockEditor.putString(KEY_APPEARANCE, "some-value") }
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "") } returns ""

        // THEN
        assertThat(userSharedPreferences.getPreferredDetailsView()).isEmpty()
    }

    @Test
    fun `GIVEN KEY_PREFERRED_DETAILS_VIEW has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "") } returns "some-value"

        // THEN
        assertThat(userSharedPreferences.getPreferredDetailsView()).isEqualTo("some-value")
    }

    @Test
    fun `WHEN setPreferredDetailsView is called THEN KEY_PREFERRED_DETAILS_VIEW is set`() {
        // WHEN
        userSharedPreferences.setPreferredDetailsView("some-value")

        // THEN
        verify { mockEditor.putString(KEY_PREFERRED_DETAILS_VIEW, "some-value") }
    }

    @Test
    fun `WHEN setMarkAsReadOnOpen is called THEN KEY_MARK_AS_READ_ON_OPEN is set`() {
        val randomBoolean = randomBoolean()

        // WHEN
        userSharedPreferences.setMarkAsReadOnOpen(randomBoolean)

        // THEN
        verify { mockEditor.putBoolean(KEY_MARK_AS_READ_ON_OPEN, randomBoolean) }
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has value WHEN getMarkAsReadOnOpen is called THEN value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false) } returns value

        // THEN
        assertThat(userSharedPreferences.getMarkAsReadOnOpen()).isEqualTo(value)
    }

    @Test
    fun `GIVEN KEY_MARK_AS_READ_ON_OPEN has no value WHEN getMarkAsReadOnOpen is called THEN false is returned`() {
        // GIVEN

        every { mockSharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false) } returns false

        // THEN
        assertThat(userSharedPreferences.getMarkAsReadOnOpen()).isFalse()
    }

    @Test
    fun `WHEN getDescriptionAutoFill is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false) } returns value

        // THEN
        assertThat(userSharedPreferences.getAutoFillDescription()).isEqualTo(value)
    }

    @Test
    fun `WHEN setDescriptionAutoFill is called THEN KEY_DESCRIPTION_AUTO_FILL is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setAutoFillDescription(value)

        // THEN
        verify { mockEditor.putBoolean(KEY_AUTO_FILL_DESCRIPTION, value) }
    }

    @Test
    fun `WHEN getDescriptionVisibleInList is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        every { mockSharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, true) } returns value

        // THEN
        assertThat(userSharedPreferences.getShowDescriptionInLists()).isEqualTo(value)
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_DESCRIPTION_VISIBLE_LIST is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setShowDescriptionInLists(value)

        // THEN
        verify { mockEditor.putBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns false THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false) } returns false

        // THEN
        assertThat(userSharedPreferences.getDefaultPrivate()).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns true THEN true is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false) } returns true

        // THEN
        assertThat(userSharedPreferences.getDefaultPrivate()).isTrue()
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN KEY_DEFAULT_PRIVATE is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.setDefaultPrivate(value)

        // THEN
        verify { mockEditor.putBoolean(KEY_DEFAULT_PRIVATE, value) }
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns false THEN null is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false) } returns false

        // THEN
        assertThat(userSharedPreferences.getDefaultReadLater()).isEqualTo(null)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns true THEN true is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false) } returns true

        // THEN
        assertThat(userSharedPreferences.getDefaultReadLater()).isTrue()
    }

    @Test
    fun `WHEN setDefaultReadLater is called THEN KEY_DEFAULT_READ_LATER is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.setDefaultReadLater(value)

        // THEN
        verify { mockEditor.putBoolean(KEY_DEFAULT_READ_LATER, value) }
    }

    @Test
    fun `WHEN getEditAfterSharing had the legacy value set THEN the correct value is returned`() {
        // GIVEN
        every { mockSharedPreferences.getBoolean(KEY_EDIT_AFTER_SHARING, false) } returns true
        every { mockSharedPreferences.getString(KEY_NEW_EDIT_AFTER_SHARING, "AFTER_SAVING") } returns null

        // THEN
        assertThat(userSharedPreferences.getEditAfterSharing()).isEqualTo(EditAfterSharing.AfterSaving.value)
    }

    @Test
    fun `WHEN getEditAfterSharing had no legacy value set THEN the default value is returned`() {
        // GIVEN
        every { mockSharedPreferences.getBoolean(KEY_EDIT_AFTER_SHARING, false) } returns false
        every { mockSharedPreferences.getString(KEY_NEW_EDIT_AFTER_SHARING, "SKIP_EDIT") } returns null

        // THEN
        assertThat(userSharedPreferences.getEditAfterSharing()).isEqualTo(EditAfterSharing.SkipEdit.value)
    }

    @Test
    fun `WHEN getEditAfterSharing is called THEN its value is returned`() {
        // GIVEN
        every { mockSharedPreferences.get(KEY_EDIT_AFTER_SHARING, false) } returns false
        every {
            mockSharedPreferences.get(
                KEY_NEW_EDIT_AFTER_SHARING,
                "SKIP_EDIT"
            )
        } returns "BEFORE_SAVING"

        // THEN
        assertThat(userSharedPreferences.getEditAfterSharing()).isEqualTo("BEFORE_SAVING")
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_NEW_EDIT_AFTER_SHARING is set`() {
        // WHEN
        userSharedPreferences.setEditAfterSharing("BEFORE_SAVING")

        // THEN
        verify { mockEditor.putString(KEY_NEW_EDIT_AFTER_SHARING, "BEFORE_SAVING") }
    }
}
