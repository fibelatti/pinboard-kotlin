package com.fibelatti.pinboard.features.user.data

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.InstantExecutorExtension
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.randomBoolean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@ExtendWith(InstantExecutorExtension::class)
internal class UserDataSourceTest {

    @Nested
    inner class InitialisationTests {

        private val mockUserSharedPreferences = mock<UserSharedPreferences>()
        private val mockPostsDao = mock<PostsDao>()

        private lateinit var userDataSource: UserDataSource

        @Test
        fun `WHEN getAuthToken is not empty THEN getLoginState will return LoggedIn`() {
            // GIVEN
            given(mockUserSharedPreferences.getAuthToken())
                .willReturn(mockApiToken)

            userDataSource = UserDataSource(mockUserSharedPreferences, mockPostsDao)

            // THEN
            userDataSource.getLoginState().currentValueShouldBe(LoginState.LoggedIn)
        }

        @Test
        fun `WHEN getAuthToken is empty THEN getLoginState will return LoggedOut`() {
            // GIVEN
            given(mockUserSharedPreferences.getAuthToken())
                .willReturn("")

            userDataSource = UserDataSource(mockUserSharedPreferences, mockPostsDao)

            // THEN
            userDataSource.getLoginState().currentValueShouldBe(LoginState.LoggedOut)
        }
    }

    @Nested
    inner class Methods {

        private val mockUserSharedPreferences = mock<UserSharedPreferences>()
        private val mockPostsDao = mock<PostsDao>()

        private lateinit var userDataSource: UserDataSource

        @BeforeEach
        fun setup() {
            given(mockUserSharedPreferences.getAuthToken()).willReturn(mockApiToken)

            userDataSource = UserDataSource(mockUserSharedPreferences, mockPostsDao)
        }

        @Test
        fun `WHEN loginAttempt is called THEN setAuthToken is called and loginState value is updated to Authorizing`() {
            // WHEN
            runBlocking { userDataSource.loginAttempt(mockApiToken) }

            // THEN
            verify(mockUserSharedPreferences).setAuthToken(mockApiToken)
            userDataSource.getLoginState().currentValueShouldBe(LoginState.Authorizing)
        }

        @Test
        fun `WHEN loggedIn is called THEN loginState value is updated to LoggedIn`() {
            // WHEN
            runBlocking { userDataSource.loggedIn() }

            // THEN
            userDataSource.getLoginState().currentValueShouldBe(LoginState.LoggedIn)
        }

        @Test
        fun `WHEN logout is called THEN setAuthToken is set and setLastUpdate is set and loginState value is updated to LoggedOut`() {
            // WHEN
            runBlocking { userDataSource.logout() }

            // THEN
            verify(mockUserSharedPreferences).setAuthToken("")
            verify(mockUserSharedPreferences).setLastUpdate("")
            verify(mockPostsDao).deleteAllPosts()
            userDataSource.getLoginState().currentValueShouldBe(LoginState.LoggedOut)
        }

        @Test
        fun `GIVEN loginState is not LoggedIn WHEN forceLogout is called THEN nothing happens`() {
            // GIVEN
            userDataSource.loginState.value = LoginState.LoggedOut

            // WHEN
            runBlocking { userDataSource.forceLogout() }

            // THEN
            verify(mockUserSharedPreferences, never()).setAuthToken(anyString())
            verify(mockUserSharedPreferences, never()).setLastUpdate(anyString())
            verify(mockPostsDao, never()).deleteAllPosts()
            userDataSource.getLoginState().currentValueShouldBe(LoginState.LoggedOut)
        }

        @Test
        fun `GIVEN loginState is LoggedIn WHEN forceLogout is called THEN setAuthToken is set and setLastUpdate is set and loginState value is updated to Unauthorizerd`() {
            // GIVEN
            userDataSource.loginState.value = LoginState.LoggedIn

            // WHEN
            runBlocking { userDataSource.forceLogout() }

            // THEN
            verify(mockUserSharedPreferences).setAuthToken("")
            verify(mockUserSharedPreferences).setLastUpdate("")
            verify(mockPostsDao).deleteAllPosts()
            userDataSource.getLoginState().currentValueShouldBe(LoginState.Unauthorized)
        }

        @Test
        fun `WHEN getLastUpdate is called THEN UserSharedPreferences is returned`() {
            // GIVEN
            given(mockUserSharedPreferences.getLastUpdate())
                .willReturn(mockTime)

            // THEN
            runBlocking { userDataSource.getLastUpdate() shouldBe mockTime }
        }

        @Test
        fun `WHEN setLastUpdate is called THEN UserSharedPreferences is set`() {
            // WHEN
            runBlocking { userDataSource.setLastUpdate(mockTime) }

            // THEN
            verify(mockUserSharedPreferences).setLastUpdate(mockTime)
        }

        @Test
        fun `WHEN getDefaultPrivate is called THEN UserSharedPreferences is returned`() {
            // GIVEN
            val value = randomBoolean()
            given(mockUserSharedPreferences.getDefaultPrivate())
                .willReturn(value)

            // THEN
            runBlocking { userDataSource.getDefaultPrivate() shouldBe value }
        }

        @Test
        fun `WHEN setDefaultPrivate is called THEN UserSharedPreferences is set`() {
            // GIVEN
            val value = randomBoolean()

            // WHEN
            runBlocking { userDataSource.setDefaultPrivate(value) }

            // THEN
            verify(mockUserSharedPreferences).setDefaultPrivate(value)
        }

        @Test
        fun `WHEN getDefaultReadLater is called THEN UserSharedPreferences is returned`() {
            // GIVEN
            val value = randomBoolean()
            given(mockUserSharedPreferences.getDefaultReadLater())
                .willReturn(value)

            // THEN
            runBlocking { userDataSource.getDefaultReadLater() shouldBe value }
        }

        @Test
        fun `WHEN setDefaultReadLater is called THEN UserSharedPreferences is set`() {
            // GIVEN
            val value = randomBoolean()

            // WHEN
            runBlocking { userDataSource.setDefaultReadLater(value) }

            // THEN
            verify(mockUserSharedPreferences).setDefaultReadLater(value)
        }
    }
}
