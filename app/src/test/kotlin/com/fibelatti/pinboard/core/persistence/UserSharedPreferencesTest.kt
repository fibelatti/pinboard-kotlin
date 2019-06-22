package com.fibelatti.pinboard.core.persistence

import android.content.SharedPreferences
import com.fibelatti.core.extension.get
import com.fibelatti.core.test.MockSharedPreferencesEditor
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
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
}
