package com.fibelatti.pinboard.features.user.presentation

import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.test.BaseViewModelTest
import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.pinboard.MockDataProvider.UnauthorizedFailure
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify

class AuthViewModelTest : BaseViewModelTest() {

    private val mockLogin = mock<Login>()
    private val mockUserRepository = mock<UserRepository>()
    private val mockResourceProvider = mock<ResourceProvider>()

    private val viewModel = AuthViewModel(
        mockLogin,
        mockUserRepository,
        mockResourceProvider
    )

    @Test
    fun `GIVEN userRepository getLoginState contains a value THEN viewModel loginState returns that value`() {
        // GIVEN
        val mockLoginState = MutableLiveData<LoginState>().apply {
            value = LoginState.LoggedOut
        }
        given(mockUserRepository.getLoginState())
            .willReturn(mockLoginState)

        // THEN
        viewModel.loginState currentValueShouldBe LoginState.LoggedOut
    }

    @Nested
    inner class LoginTest {
        @Test
        fun `GIVEN Login is successful WHEN login is called THEN nothing else happens`() {
            // GIVEN
            givenSuspend { mockLogin(Login.Params(mockApiToken)) }
                .willReturn(Success(Unit))

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            viewModel.error.shouldNeverReceiveValues()
            viewModel.apiTokenError.shouldNeverReceiveValues()
        }

        @Test
        fun `GIVEN Login fails and error isUnauthorized WHEN login is called THEN apiTokenError should receive a value`() {
            // GIVEN
            givenSuspend { mockLogin(Login.Params(mockApiToken)) }
                .willReturn(UnauthorizedFailure())
            given(mockResourceProvider.getString(R.string.auth_token_error))
                .willReturn("R.string.auth_token_error")

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            viewModel.error.shouldNeverReceiveValues()
            viewModel.apiTokenError currentEventShouldBe "R.string.auth_token_error"
        }

        @Test
        fun `GIVEN Login fails WHEN login is called THEN error should receive a value`() {
            // GIVEN
            val error = Exception()
            givenSuspend { mockLogin(Login.Params(mockApiToken)) }
                .willReturn(Failure(error))

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            viewModel.error currentValueShouldBe error
            viewModel.apiTokenError.shouldNeverReceiveValues()
        }
    }

    @Test
    fun `WHEN logout is called THEN userRepository logout is called`() {
        // WHEN
        viewModel.logout()

        // THEN
        verify(mockUserRepository).logout()
    }
}
