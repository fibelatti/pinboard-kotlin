package com.fibelatti.pinboard.features.user.presentation

import app.cash.turbine.test
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_API_TOKEN
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_INSTANCE_URL
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.user.domain.Login
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

    private val appStateFlow = MutableStateFlow(createAppState(content = LoginContent()))
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }
    private val mockResourceProvider = mockk<ResourceProvider>()

    private val viewModel = AuthViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        loginUseCase = mockLogin,
        resourceProvider = mockResourceProvider,
    )

    @Nested
    inner class ContentTests {

        @Test
        fun `content changes reset the screen state`() = runTest {
            viewModel.screenState.test {
                assertThat(awaitItem()).isEqualTo(
                    AuthViewModel.ScreenState(
                        allowSwitching = true,
                        useLinkding = false,
                    ),
                )

                appStateFlow.value = createAppState(content = createPostListContent())
                appStateFlow.value = createAppState(content = LoginContent(appMode = AppMode.LINKDING))

                assertThat(awaitItem()).isEqualTo(
                    AuthViewModel.ScreenState(
                        allowSwitching = false,
                        useLinkding = true,
                    ),
                )

                appStateFlow.value = createAppState(content = createPostListContent())
                appStateFlow.value = createAppState(content = LoginContent(appMode = AppMode.PINBOARD))

                assertThat(awaitItem()).isEqualTo(
                    AuthViewModel.ScreenState(
                        allowSwitching = false,
                        useLinkding = false,
                    ),
                )
            }
        }
    }

    @Nested
    inner class LoginTests {

        @Test
        fun `GIVEN auth token is empty WHEN login is called THEN error state is emitted`() = runTest {
            // GIVEN
            every { mockResourceProvider.getString(R.string.auth_token_empty) } returns "R.string.auth_token_empty"

            // WHEN
            viewModel.login(
                apiToken = "",
                instanceUrl = "",
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
                viewModel.useLinkding(value = true)

                // WHEN
                viewModel.login(
                    apiToken = SAMPLE_API_TOKEN,
                    instanceUrl = "",
                )

                verify { mockLogin wasNot Called }

                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(
                        useLinkding = true,
                        instanceUrlError = "R.string.auth_linkding_instance_url_error",
                    ),
                )
            }

        @Test
        fun `GIVEN Login is successful WHEN login is called THEN nothing else happens`() = runTest {
            // GIVEN
            coEvery {
                mockLogin(
                    Login.LinkdingParams(
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
                    ),
                )
            } returns Success(Unit)
            viewModel.useLinkding(value = true)

            // WHEN
            viewModel.login(
                apiToken = SAMPLE_API_TOKEN,
                instanceUrl = SAMPLE_INSTANCE_URL,
            )

            // THEN
            coVerify {
                mockLogin(
                    Login.LinkdingParams(
                        authToken = SAMPLE_API_TOKEN,
                        instanceUrl = SAMPLE_INSTANCE_URL,
                    ),
                )
            }

            assertThat(viewModel.error.first()).isNull()
            assertThat(viewModel.screenState.first()).isEqualTo(
                AuthViewModel.ScreenState(
                    useLinkding = true,
                    isLoading = true,
                ),
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

                coEvery { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = SAMPLE_API_TOKEN,
                    instanceUrl = "",
                )

                // THEN
                coVerify { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) }

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

                coEvery { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) } returns Failure(error)
                every { mockResourceProvider.getString(R.string.auth_token_error) } returns "R.string.auth_token_error"

                // WHEN
                viewModel.login(
                    apiToken = SAMPLE_API_TOKEN,
                    instanceUrl = SAMPLE_INSTANCE_URL,
                )

                // THEN
                coVerify { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) }

                assertThat(viewModel.error.first()).isNull()
                assertThat(viewModel.screenState.first()).isEqualTo(
                    AuthViewModel.ScreenState(apiTokenError = "R.string.auth_token_error"),
                )
            }

        @Test
        fun `GIVEN Login fails WHEN login is called THEN error should receive a value`() = runTest {
            // GIVEN
            val error = Exception()
            coEvery { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) } returns Failure(error)

            // WHEN
            viewModel.login(
                apiToken = SAMPLE_API_TOKEN,
                instanceUrl = SAMPLE_INSTANCE_URL,
            )

            // THEN
            coVerify { mockLogin(Login.PinboardParams(authToken = SAMPLE_API_TOKEN)) }

            assertThat(viewModel.error.first()).isEqualTo(error)
            assertThat(viewModel.screenState.first()).isEqualTo(AuthViewModel.ScreenState())
        }
    }
}
