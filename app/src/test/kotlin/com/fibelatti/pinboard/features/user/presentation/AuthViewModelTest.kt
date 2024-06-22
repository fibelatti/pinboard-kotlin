package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockInstanceUrl
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    private val mockUserRepository = mockk<UserRepository> {
        every { useLinkding } returns false
    }
    private val mockResourceProvider = mockk<ResourceProvider>()

    private val viewModel = AuthViewModel(
        loginUseCase = mockLogin,
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        resourceProvider = mockResourceProvider,
    )

    @Nested
    inner class LoginTest {

        @Test
        fun `GIVEN auth token is empty WHEN login is called THEN error state is emitted`() = runTest {
            // GIVEN
            every { mockResourceProvider.getString(R.string.auth_token_empty) } returns "R.string.auth_token_empty"

            // WHEN
            viewModel.login(
                apiToken = "",
                instanceUrl = mockInstanceUrl,
            )

            verify { mockLogin wasNot Called }

            assertThat(viewModel.screenState.first()).isEqualTo(
                AuthViewModel.ScreenState(apiTokenError = "R.string.auth_token_empty"),
            )
        }

        @Test
        fun `GIVEN use linkding is true AND instance url is empty WHEN login is called THEN error state is emitted`() =
            runTest {
                // GIVEN
                every {
                    mockResourceProvider.getString(R.string.auth_linkding_instance_url_error)
                } returns "R.string.auth_linkding_instance_url_error"
                every { mockUserRepository.useLinkding } returns true

                // WHEN
                viewModel.login(
                    apiToken = mockApiToken,
                    instanceUrl = "",
                )

                verify { mockLogin wasNot Called }

                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(instanceUrlError = "R.string.auth_linkding_instance_url_error"),
                )
            }


        @Test
        fun `GIVEN Login is successful WHEN login is called THEN nothing else happens`() = runTest {
            // GIVEN
            coEvery {
                mockLogin(
                    Login.Params(
                        authToken = mockApiToken,
                        instanceUrl = mockInstanceUrl,
                    ),
                )
            } returns Success(Unit)

            // WHEN
            viewModel.login(
                apiToken = mockApiToken,
                instanceUrl = mockInstanceUrl,
            )

            // THEN
            coVerify {
                mockLogin(
                    Login.Params(
                        authToken = mockApiToken,
                        instanceUrl = mockInstanceUrl,
                    ),
                )
            }

            assertThat(viewModel.error.isEmpty()).isTrue()
            assertThat(viewModel.screenState.first()).isEqualTo(
                AuthViewModel.ScreenState(isLoading = true),
            )
        }

        @Test
        fun `GIVEN Login fails and error code is 401 WHEN login is called THEN apiTokenError should receive a value`() =
            runTest {
                // GIVEN
                val error = HttpException(
                    Response.error<GenericResponseDto>(
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        "{}".toResponseBody("application/json".toMediaTypeOrNull()),
                    ),
                )

                coEvery {
                    mockLogin(
                        Login.Params(
                            authToken = mockApiToken,
                            instanceUrl = mockInstanceUrl,
                        ),
                    )
                } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = mockApiToken,
                    instanceUrl = mockInstanceUrl,
                )

                // THEN
                coVerify {
                    mockLogin(
                        Login.Params(
                            authToken = mockApiToken,
                            instanceUrl = mockInstanceUrl,
                        ),
                    )
                }

                assertThat(viewModel.error.isEmpty()).isTrue()
                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(apiTokenError = "R.string.auth_token_error"),
                )
            }

        @Test
        fun `GIVEN Login fails and error code is 500 WHEN login is called THEN apiTokenError should receive a value`() =
            runTest {
                // GIVEN
                val error = HttpException(
                    Response.error<GenericResponseDto>(
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                        "{}".toResponseBody("application/json".toMediaTypeOrNull()),
                    ),
                )

                coEvery {
                    mockLogin(
                        Login.Params(
                            authToken = mockApiToken,
                            instanceUrl = mockInstanceUrl,
                        ),
                    )
                } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = mockApiToken,
                    instanceUrl = mockInstanceUrl,
                )

                // THEN
                coVerify {
                    mockLogin(
                        Login.Params(
                            authToken = mockApiToken,
                            instanceUrl = mockInstanceUrl,
                        ),
                    )
                }

                assertThat(viewModel.error.isEmpty()).isTrue()
                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(apiTokenError = "R.string.auth_token_error"),
                )
            }

        @Test
        fun `GIVEN Login fails WHEN login is called THEN error should receive a value`() = runTest {
            // GIVEN
            val error = Exception()
            coEvery {
                mockLogin(
                    Login.Params(
                        authToken = mockApiToken,
                        instanceUrl = mockInstanceUrl,
                    ),
                )
            } returns Failure(error)

            // WHEN
            viewModel.login(
                apiToken = mockApiToken,
                instanceUrl = mockInstanceUrl,
            )

            // THEN
            coVerify {
                mockLogin(
                    Login.Params(
                        authToken = mockApiToken,
                        instanceUrl = mockInstanceUrl,
                    ),
                )
            }

            assertThat(viewModel.error.first()).isEqualTo(error)
            assertThat(viewModel.screenState.first()).isEqualTo(AuthViewModel.ScreenState())
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
