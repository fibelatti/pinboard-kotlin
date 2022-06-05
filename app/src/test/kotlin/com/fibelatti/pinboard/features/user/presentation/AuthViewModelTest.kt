package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.android.ResourceProvider
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

class AuthViewModelTest : BaseViewModelTest() {

    private val mockLogin = mockk<Login>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxUnitFun = true)
    private val mockResourceProvider = mockk<ResourceProvider>()

    private val viewModel = AuthViewModel(
        mockLogin,
        mockAppStateRepository,
        mockResourceProvider
    )

    @Nested
    inner class LoginTest {

        @Test
        fun `GIVEN Login is successful WHEN login is called THEN nothing else happens`() {
            // GIVEN
            coEvery { mockLogin(mockApiToken) } returns Success(Unit)

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            runBlocking {
                assertThat(viewModel.error.isEmpty()).isTrue()
                assertThat(viewModel.apiTokenError.isEmpty()).isTrue()
            }
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
            runBlocking {
                assertThat(viewModel.error.isEmpty()).isTrue()
                assertThat(viewModel.apiTokenError.first()).isEqualTo("R.string.auth_token_error")
            }
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
            runBlocking {
                assertThat(viewModel.error.isEmpty()).isTrue()
                assertThat(viewModel.apiTokenError.first()).isEqualTo("R.string.auth_token_error")
            }
        }

        @Test
        fun `GIVEN Login fails WHEN login is called THEN error should receive a value`() {
            // GIVEN
            val error = Exception()
            coEvery { mockLogin(mockApiToken) } returns Failure(error)

            // WHEN
            viewModel.login(mockApiToken)

            // THEN
            runBlocking {
                assertThat(viewModel.error.first()).isEqualTo(error)
                assertThat(viewModel.apiTokenError.isEmpty()).isTrue()
            }
        }
    }

    @Test
    fun `WHEN logout is called THEN appStateRepository runs UserLoggedOut`() {
        // WHEN
        viewModel.logout()

        // THEN
        coVerify { mockAppStateRepository.runAction(UserLoggedOut) }
    }
}
