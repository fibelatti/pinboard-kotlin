package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_API_TOKEN
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_INSTANCE_URL
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthViewModelTest : BaseViewModelTest() {

    private val mockLogin = mockk<Login>()

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }
    private val mockUserRepository = mockk<UserRepository> {
        every { useLinkding } returns false
    }
    private val mockResourceProvider = mockk<ResourceProvider>()

    private val viewModel = AuthViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
        loginUseCase = mockLogin,
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
                instanceUrl = SAMPLE_INSTANCE_URL,
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
                    apiToken = SAMPLE_API_TOKEN,
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
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
                    ),
                )
            } returns Success(Unit)

            // WHEN
            viewModel.login(
                apiToken = SAMPLE_API_TOKEN,
                instanceUrl = SAMPLE_INSTANCE_URL,
            )

            // THEN
            coVerify {
                mockLogin(
                    Login.Params(
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
                    ),
                )
            }

            assertThat(viewModel.error.first()).isNull()
            assertThat(viewModel.screenState.first()).isEqualTo(
                AuthViewModel.ScreenState(isLoading = true),
            )
        }

        @Test
        fun `GIVEN Login fails and error code is 401 WHEN login is called THEN apiTokenError should receive a value`() =
            runTest {
                // GIVEN
                val error = mockk<ResponseException> {
                    every { response } returns mockk<HttpResponse> {
                        every { status } returns mockk {
                            every { value } returns 401
                        }
                    }
                }

                coEvery {
                    mockLogin(
                        Login.Params(
                            authToken = SAMPLE_API_TOKEN,
                            instanceUrl = SAMPLE_INSTANCE_URL,
                        ),
                    )
                } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = SAMPLE_API_TOKEN,
                    instanceUrl = SAMPLE_INSTANCE_URL,
                )

                // THEN
                coVerify {
                    mockLogin(
                        Login.Params(
                            authToken = SAMPLE_API_TOKEN,
                            instanceUrl = SAMPLE_INSTANCE_URL,
                        ),
                    )
                }

                assertThat(viewModel.error.first()).isNull()
                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(apiTokenError = "R.string.auth_token_error"),
                )
            }

        @Test
        fun `GIVEN Login fails and error code is 500 WHEN login is called THEN apiTokenError should receive a value`() =
            runTest {
                // GIVEN
                val error = mockk<ResponseException> {
                    every { response } returns mockk<HttpResponse> {
                        every { status } returns mockk {
                            every { value } returns 500
                        }
                    }
                }

                coEvery {
                    mockLogin(
                        Login.Params(
                            authToken = SAMPLE_API_TOKEN,
                            instanceUrl = SAMPLE_INSTANCE_URL,
                        ),
                    )
                } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = SAMPLE_API_TOKEN,
                    instanceUrl = SAMPLE_INSTANCE_URL,
                )

                // THEN
                coVerify {
                    mockLogin(
                        Login.Params(
                            authToken = SAMPLE_API_TOKEN,
                            instanceUrl = SAMPLE_INSTANCE_URL,
                        ),
                    )
                }

                assertThat(viewModel.error.first()).isNull()
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
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
                    ),
                )
            } returns Failure(error)

            // WHEN
            viewModel.login(
                apiToken = SAMPLE_API_TOKEN,
                instanceUrl = SAMPLE_INSTANCE_URL,
            )

            // THEN
            coVerify {
                mockLogin(
                    Login.Params(
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
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
