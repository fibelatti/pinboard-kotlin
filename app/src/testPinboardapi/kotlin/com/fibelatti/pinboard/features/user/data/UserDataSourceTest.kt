package com.fibelatti.pinboard.features.user.data

import com.fibelatti.pinboard.InstantExecutorExtension
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class UserDataSourceTest {

    @Nested
    inner class InitialisationTests {

        private val mockUserSharedPreferences = mockk<UserSharedPreferences>(relaxed = true)

        private lateinit var userDataSource: UserDataSource

        @Test
        fun `WHEN getAuthToken is not empty THEN getLoginState will return LoggedIn`() {
            // GIVEN
            every { mockUserSharedPreferences.getAuthToken() } returns mockApiToken

            userDataSource = UserDataSource(mockUserSharedPreferences)

            // THEN
            runBlocking {
                assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.LoggedIn)
            }
        }

        @Test
        fun `WHEN getAuthToken is empty THEN getLoginState will return LoggedOut`() {
            // GIVEN
            every { mockUserSharedPreferences.getAuthToken() } returns ""

            userDataSource = UserDataSource(mockUserSharedPreferences)

            // THEN
            runBlocking {
                assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.LoggedOut)
            }
        }
    }

    @Nested
    inner class Methods {

        private val mockUserSharedPreferences = mockk<UserSharedPreferences>(relaxed = true)

        private lateinit var userDataSource: UserDataSource

        @BeforeEach
        fun setup() {
            every { mockUserSharedPreferences.getAuthToken() } returns mockApiToken

            userDataSource = UserDataSource(mockUserSharedPreferences)
        }

        @Test
        fun `WHEN loginAttempt is called THEN setAuthToken is called and loginState value is updated to Authorizing`() {
            // WHEN
            userDataSource.loginAttempt(mockApiToken)

            // THEN
            verify { mockUserSharedPreferences.setAuthToken(mockApiToken) }
            runBlocking {
                assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.Authorizing)
            }
        }

        @Test
        fun `WHEN loggedIn is called THEN loginState value is updated to LoggedIn`() {
            // WHEN
            userDataSource.loggedIn()

            // THEN
            runBlocking {
                assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.LoggedIn)
            }
        }

        @Test
        fun `WHEN logout is called THEN setAuthToken is set and setLastUpdate is set and loginState value is updated to LoggedOut`() {
            // WHEN
            userDataSource.logout()

            // THEN
            verify { mockUserSharedPreferences.setAuthToken("") }
            verify { mockUserSharedPreferences.setLastUpdate("") }
            runBlocking {
                assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.LoggedOut)
            }
        }

        @Nested
        inner class LoginStateTests {

            @Test
            fun `GIVEN loginState is not LoggedIn WHEN forceLogout is called THEN nothing happens`() {
                // GIVEN
                userDataSource.loginState.value = LoginState.LoggedOut

                // WHEN
                userDataSource.forceLogout()

                // THEN
                verify(exactly = 0) { mockUserSharedPreferences.setAuthToken(any()) }
                verify(exactly = 0) { mockUserSharedPreferences.setLastUpdate(any()) }
                runBlocking {
                    assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.LoggedOut)
                }
            }

            @Test
            fun `GIVEN loginState is LoggedIn WHEN forceLogout is called THEN setAuthToken is set and setLastUpdate is set and loginState value is updated to Unauthorizerd`() {
                // GIVEN
                userDataSource.loginState.value = LoginState.LoggedIn

                // WHEN
                userDataSource.forceLogout()

                // THEN
                verify { mockUserSharedPreferences.setAuthToken("") }
                verify { mockUserSharedPreferences.setLastUpdate("") }
                runBlocking {
                    assertThat(userDataSource.getLoginState().first()).isEqualTo(LoginState.Unauthorized)
                }
            }
        }

        @Nested
        inner class LastUpdate {

            @Test
            fun `WHEN getLastUpdate is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getLastUpdate() } returns mockTime

                // THEN
                assertThat(userDataSource.getLastUpdate()).isEqualTo(mockTime)
            }

            @Test
            fun `WHEN setLastUpdate is called THEN UserSharedPreferences is set`() {
                // WHEN
                userDataSource.setLastUpdate(mockTime)

                // THEN
                verify { mockUserSharedPreferences.setLastUpdate(mockTime) }
            }
        }

        @Nested
        inner class AppearanceTests {

            @Test
            fun `GIVEN set appearance is the light theme WHEN getAppearance is called THEN LightTheme is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getAppearance() } returns Appearance.LightTheme.value

                // THEN
                assertThat(userDataSource.getAppearance()).isEqualTo(Appearance.LightTheme)
            }

            @Test
            fun `GIVEN set appearance is the dark theme WHEN getAppearance is called THEN DarkTheme is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getAppearance() } returns Appearance.LightTheme.value

                // THEN
                assertThat(userDataSource.getAppearance()).isEqualTo(Appearance.LightTheme)
            }

            @Test
            fun `GIVEN set appearance is not set WHEN getAppearance is called THEN SystemDefault is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getAppearance() } returns "anything really"

                // THEN
                assertThat(userDataSource.getAppearance()).isEqualTo(Appearance.SystemDefault)
            }

            @Test
            fun `WHEN setAppearance is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockAppearance = mockk<Appearance>()
                every { mockAppearance.value } returns "random-value"

                // WHEN
                userDataSource.setAppearance(mockAppearance)

                // THEN
                verify { mockUserSharedPreferences.setAppearance("random-value") }
            }
        }

        @Nested
        inner class PreferredDetailsViewTests {

            @Test
            fun `GIVEN set preferred details view is external browser WHEN getPreferredDetailsView is called THEN ExternalBrowser is returned`() {
                // GIVEN
                val randomBoolean = randomBoolean()
                every { mockUserSharedPreferences.getPreferredDetailsView() } returns PreferredDetailsView.ExternalBrowser(
                    randomBoolean
                ).value
                every { mockUserSharedPreferences.getMarkAsReadOnOpen() } returns randomBoolean

                // THEN
                assertThat(userDataSource.getPreferredDetailsView()).isEqualTo(
                    PreferredDetailsView.ExternalBrowser(randomBoolean)
                )
                verify { mockUserSharedPreferences.getMarkAsReadOnOpen() }
            }

            @Test
            fun `GIVEN set preferred details view is edit WHEN getPreferredDetailsView is called THEN Edit is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getPreferredDetailsView() } returns PreferredDetailsView.Edit.value

                // THEN
                assertThat(userDataSource.getPreferredDetailsView()).isEqualTo(
                    PreferredDetailsView.Edit
                )
            }

            @Test
            fun `GIVEN set preferred details view is not specifically handled WHEN getPreferredDetailsView is called THEN InAppBrowser is returned`() {
                // GIVEN
                val randomBoolean = randomBoolean()
                every { mockUserSharedPreferences.getPreferredDetailsView() } returns "anything really"
                every { mockUserSharedPreferences.getMarkAsReadOnOpen() } returns randomBoolean

                // THEN
                assertThat(userDataSource.getPreferredDetailsView()).isEqualTo(
                    PreferredDetailsView.InAppBrowser(randomBoolean)
                )
                verify(exactly = 2) { mockUserSharedPreferences.getMarkAsReadOnOpen() }
            }

            @Test
            fun `WHEN setPreferredDetailsView is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockPreferredDetailsView = mockk<PreferredDetailsView>()
                every { mockPreferredDetailsView.value } returns "random-value"

                // WHEN
                userDataSource.setPreferredDetailsView(mockPreferredDetailsView)

                // THEN
                verify { mockUserSharedPreferences.setPreferredDetailsView("random-value") }
            }
        }

        @Nested
        inner class MarkAsReadOnOpen {

            @Test
            fun `WHEN getMarkAsReadOnOpen is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.getMarkAsReadOnOpen() } returns value

                // THEN
                assertThat(userDataSource.getMarkAsReadOnOpen()).isEqualTo(value)
            }

            @Test
            fun `WHEN setMarkAsReadOnOpen is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.setMarkAsReadOnOpen(value)

                // THEN
                verify { mockUserSharedPreferences.setMarkAsReadOnOpen(value) }
            }
        }

        @Nested
        inner class AutoFill {

            @Test
            fun `WHEN getAutoFillDescription is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.getAutoFillDescription() } returns value

                // THEN
                assertThat(userDataSource.getAutoFillDescription()).isEqualTo(value)
            }

            @Test
            fun `WHEN setAutoFillDescription is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.setAutoFillDescription(value)

                // THEN
                verify { mockUserSharedPreferences.setAutoFillDescription(value) }
            }
        }

        @Nested
        inner class DescriptionInLists {

            @Test
            fun `WHEN getShowDescriptionInLists is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.getShowDescriptionInLists() } returns value

                // THEN
                assertThat(userDataSource.getShowDescriptionInLists()).isEqualTo(value)
            }

            @Test
            fun `WHEN setShowDescriptionInLists is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.setShowDescriptionInLists(value)

                // THEN
                verify { mockUserSharedPreferences.setShowDescriptionInLists(value) }
            }
        }

        @Nested
        inner class DefaultPrivate {

            @Test
            fun `WHEN getDefaultPrivate is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.getDefaultPrivate() } returns value

                // THEN
                assertThat(userDataSource.getDefaultPrivate()).isEqualTo(value)
            }

            @Test
            fun `WHEN setDefaultPrivate is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.setDefaultPrivate(value)

                // THEN
                verify { mockUserSharedPreferences.setDefaultPrivate(value) }
            }
        }

        @Nested
        inner class DefaultReadLater {

            @Test
            fun `WHEN getDefaultReadLater is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                val value = randomBoolean()
                every { mockUserSharedPreferences.getDefaultReadLater() } returns value

                // THEN
                assertThat(userDataSource.getDefaultReadLater()).isEqualTo(value)
            }

            @Test
            fun `WHEN setDefaultReadLater is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val value = randomBoolean()

                // WHEN
                userDataSource.setDefaultReadLater(value)

                // THEN
                verify { mockUserSharedPreferences.setDefaultReadLater(value) }
            }
        }

        @Nested
        inner class DefaultTags {

            private val mockTagValues = listOf("test")
            private val mockTags = listOf(Tag("test"))

            @Test
            fun `WHEN getDefaultTags is called THEN UserSharedPreferences is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getDefaultTags() } returns mockTagValues

                // THEN
                assertThat(userDataSource.getDefaultTags()).isEqualTo(mockTags)
            }

            @Test
            fun `WHEN setDefaultTags is called THEN UserSharedPreferences is set`() {
                // WHEN
                userDataSource.setDefaultTags(mockTags)

                // THEN
                verify { mockUserSharedPreferences.setDefaultTags(mockTagValues) }
            }
        }

        @Nested
        inner class EditAfterSharingTests {

            @Test
            fun `GIVEN set EditAfterSharing is the BeforeSaving value WHEN getEditAfterSharing is called THEN BeforeSaving is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getEditAfterSharing() } returns EditAfterSharing.BeforeSaving.value

                // THEN
                assertThat(userDataSource.getEditAfterSharing()).isEqualTo(EditAfterSharing.BeforeSaving)
            }

            @Test
            fun `GIVEN set EditAfterSharing is the AfterSaving value WHEN getEditAfterSharing is called THEN AfterSaving is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getEditAfterSharing() } returns EditAfterSharing.AfterSaving.value

                // THEN
                assertThat(userDataSource.getEditAfterSharing()).isEqualTo(EditAfterSharing.AfterSaving)
            }

            @Test
            fun `GIVEN not EditAfterSharing is set WHEN getEditAfterSharing is called THEN SkipEdit is returned`() {
                // GIVEN
                every { mockUserSharedPreferences.getEditAfterSharing() } returns "anything really"

                // THEN
                assertThat(userDataSource.getEditAfterSharing()).isEqualTo(EditAfterSharing.SkipEdit)
            }

            @Test
            fun `WHEN setEditAfterSharing is called THEN UserSharedPreferences is set`() {
                // GIVEN
                val mockEditAfterSharing = mockk<EditAfterSharing>()
                every { mockEditAfterSharing.value } returns "random-value"

                // WHEN
                userDataSource.setEditAfterSharing(mockEditAfterSharing)

                // THEN
                verify { mockUserSharedPreferences.setEditAfterSharing("random-value") }
            }
        }
    }
}
