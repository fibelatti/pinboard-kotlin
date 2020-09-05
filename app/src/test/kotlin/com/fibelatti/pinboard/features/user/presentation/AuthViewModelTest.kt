package com.fibelatti.pinboard.features.user.presentation

import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.pinboard.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

class AuthViewModelTest : BaseViewModelTest() {

    private val mockLogin = mockk<Login>()
    private val mockUserRepository = mockk<UserRepository>()
    private val mockResourceProvider = mockk<ResourceProvider>()

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
        every { mockUserRepository.getLoginState() } returns mockLoginState

        // THEN
        viewModel.loginState currentValueShouldBe LoginState.LoggedOut
    }

    @Nested
    inner class LoginTest {

        @Test
        fun `GIVEN Login is successful WHEN login is called THEN nothing else happens`() {
            // GIVEN
            coEvery { mockLogin(mockApiToken) } returns Success(Unit)

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            viewModel.error.shouldNeverReceiveValues()
            viewModel.apiTokenError.shouldNeverReceiveValues()
        }

        @Test
        fun `GIVEN Login fails and error code is 401 WHEN login is called THEN apiTokenError should receive a value`() {
            // GIVEN
            val error = HttpException(
                Response.error<GenericResponseDto>(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    "{}".toResponseBody("application/json".toMediaTypeOrNull())
                )
            )

            coEvery { mockLogin(mockApiToken) } returns Failure(error)
            every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            viewModel.error.shouldNeverReceiveValues()
            viewModel.apiTokenError currentEventShouldBe "R.string.auth_token_error"
        }

        @Test
        fun `GIVEN Login fails and error code is 500 WHEN login is called THEN apiTokenError should receive a value`() {
            // GIVEN
            val error = HttpException(
                Response.error<GenericResponseDto>(
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "{}".toResponseBody("application/json".toMediaTypeOrNull())
                )
            )

            coEvery { mockLogin(mockApiToken) } returns Failure(error)
            every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

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
            coEvery { mockLogin(mockApiToken) } returns Failure(error)

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
        coVerify { mockUserRepository.logout() }
    }
}
