package com.fibelatti.pinboard.core.persistence

import android.content.SharedPreferences
import com.fibelatti.core.extension.get
import com.fibelatti.core.test.MockSharedPreferencesEditor
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.randomBoolean
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

internal class UserSharedPreferencesTest {

    private val mockSharedPreferences = mock<SharedPreferences>()
    private val mockEditor = spy(MockSharedPreferencesEditor())

    private val userSharedPreferences = UserSharedPreferences(mockSharedPreferences)

    @BeforeEach
    fun setup() {
        given(mockSharedPreferences.edit())
            .willReturn(mockEditor)
    }

    @Test
    fun `GIVEN KEY_AUTH_TOKEN has no value WHEN getAuthToken is called THEN empty string is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_AUTH_TOKEN, ""))
            .willReturn("")

        // THEN
        userSharedPreferences.getAuthToken() shouldBe ""
    }

    @Test
    fun `GIVEN KEY_AUTH_TOKEN has value WHEN getAuthToken is called THEN value is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_AUTH_TOKEN, ""))
            .willReturn(mockApiToken)

        // THEN
        userSharedPreferences.getAuthToken() shouldBe mockApiToken
    }

    @Test
    fun `WHEN setAuthToken is called THEN KEY_AUTH_TOKEN is set`() {
        // WHEN
        userSharedPreferences.setAuthToken(mockApiToken)

        // THEN
        verify(mockEditor).putString(KEY_AUTH_TOKEN, mockApiToken)
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has no value WHEN getLastUpdate is called THEN empty string is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_LAST_UPDATE, ""))
            .willReturn("")

        // THEN
        userSharedPreferences.getLastUpdate() shouldBe ""
    }

    @Test
    fun `GIVEN KEY_LAST_UPDATE has value WHEN getLastUpdate is called THEN value is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_LAST_UPDATE, ""))
            .willReturn(mockTime)

        // THEN
        userSharedPreferences.getLastUpdate() shouldBe mockTime
    }

    @Test
    fun `WHEN setLastUpdate is called THEN KEY_LAST_UPDATE is set`() {
        // WHEN
        userSharedPreferences.setLastUpdate(mockTime)

        // THEN
        verify(mockEditor).putString(KEY_LAST_UPDATE, mockTime)
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has no value WHEN getAppearance is called THEN empty string is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_APPEARANCE, ""))
            .willReturn("")

        // THEN
        userSharedPreferences.getAppearance() shouldBe ""
    }

    @Test
    fun `GIVEN KEY_APPEARANCE has value WHEN getAppearance is called THEN value is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_APPEARANCE, ""))
            .willReturn("some-value")

        // THEN
        userSharedPreferences.getAppearance() shouldBe "some-value"
    }

    @Test
    fun `WHEN setAppearance is called THEN KEY_APPEARANCE is set`() {
        // WHEN
        userSharedPreferences.setAppearance("some-value")

        // THEN
        verify(mockEditor).putString(KEY_APPEARANCE, "some-value")
    }

    @Test
    fun `WHEN getDescriptionAutoFill is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        given(mockSharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false))
            .willReturn(value)

        // THEN
        userSharedPreferences.getAutoFillDescription() shouldBe value
    }

    @Test
    fun `WHEN setDescriptionAutoFill is called THEN KEY_DESCRIPTION_AUTO_FILL is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setAutoFillDescription(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_AUTO_FILL_DESCRIPTION, value)
    }

    @Test
    fun `WHEN getDescriptionVisibleInList is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        given(mockSharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, false))
            .willReturn(value)

        // THEN
        userSharedPreferences.getShowDescriptionInLists() shouldBe value
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_DESCRIPTION_VISIBLE_LIST is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setShowDescriptionInLists(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, value)
    }

    @Test
    fun `WHEN getDescriptionVisibleInDetail is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        given(mockSharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_DETAILS, false))
            .willReturn(value)

        // THEN
        userSharedPreferences.getShowDescriptionInDetails() shouldBe value
    }

    @Test
    fun `WHEN setDescriptionVisibleInDetail is called THEN KEY_DESCRIPTION_VISIBLE_DETAIL is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setShowDescriptionInDetails(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_SHOW_DESCRIPTION_IN_DETAILS, value)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns false THEN null is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false))
            .willReturn(false)

        // THEN
        userSharedPreferences.getDefaultPrivate() shouldBe null
    }

    @Test
    fun `GIVEN KEY_DEFAULT_PRIVATE returns true THEN true is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_DEFAULT_PRIVATE, false))
            .willReturn(true)

        // THEN
        userSharedPreferences.getDefaultPrivate() shouldBe true
    }

    @Test
    fun `WHEN setDefaultPrivate is called THEN KEY_DEFAULT_PRIVATE is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.setDefaultPrivate(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_DEFAULT_PRIVATE, value)
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns false THEN null is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false))
            .willReturn(false)

        // THEN
        userSharedPreferences.getDefaultReadLater() shouldBe null
    }

    @Test
    fun `GIVEN KEY_DEFAULT_READ_LATER returns true THEN true is returned`() {
        // GIVEN
        given(mockSharedPreferences.get(KEY_DEFAULT_READ_LATER, false))
            .willReturn(true)

        // THEN
        userSharedPreferences.getDefaultReadLater() shouldBe true
    }

    @Test
    fun `WHEN setDefaultReadLater is called THEN KEY_DEFAULT_READ_LATER is set`() {
        // GIVEN
        val value = randomBoolean()

        // WHEN
        userSharedPreferences.setDefaultReadLater(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_DEFAULT_READ_LATER, value)
    }

    @Test
    fun `WHEN getEditAfterSharing is called THEN its value is returned`() {
        // GIVEN
        val value = randomBoolean()
        given(mockSharedPreferences.get(KEY_EDIT_AFTER_SHARING, false))
            .willReturn(value)

        // THEN
        userSharedPreferences.getEditAfterSharing() shouldBe value
    }

    @Test
    fun `WHEN setEditAfterSharing is called THEN KEY_EDIT_AFTER_SHARING is set`() {
        // WHEN
        val value = randomBoolean()
        userSharedPreferences.setEditAfterSharing(value)

        // THEN
        verify(mockEditor).putBoolean(KEY_EDIT_AFTER_SHARING, value)
    }
}
