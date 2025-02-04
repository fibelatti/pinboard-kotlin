package com.fibelatti.pinboard.features.user.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.ui.components.TextWithLinks
import com.fibelatti.ui.preview.DevicePreviews
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    val screenState by authViewModel.screenState.collectAsStateWithLifecycle()
    val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()

    LaunchedErrorHandlerEffect(viewModel = authViewModel)

    AuthScreen(
        useLinkding = userPreferences.useLinkding,
        linkdingInstanceUrl = userPreferences.linkdingInstanceUrl,
        onUseLinkdingChanged = userPreferencesViewModel::useLinkding,
        onAuthRequested = authViewModel::login,
        isLoading = screenState.isLoading,
        apiTokenError = screenState.apiTokenError,
        instanceUrlError = screenState.instanceUrlError,
    )
}

@Composable
private fun AuthScreen(
    useLinkding: Boolean,
    linkdingInstanceUrl: String,
    onUseLinkdingChanged: (Boolean) -> Unit,
    onAuthRequested: (token: String, instanceUrl: String) -> Unit,
    isLoading: Boolean,
    apiTokenError: String?,
    instanceUrlError: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pin),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 40.dp, bottom = 20.dp)
                .size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Surface(
            modifier = Modifier.sizeIn(maxWidth = 600.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .animateContentSize(),
            ) {
                var authToken by remember { mutableStateOf("") }
                var instanceUrl by remember { mutableStateOf(linkdingInstanceUrl) }
                val focusManager = LocalFocusManager.current

                Text(
                    text = stringResource(
                        id = if (useLinkding) R.string.auth_title_linkding else R.string.auth_title_pinboard,
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )

                AnimatedVisibility(visible = useLinkding) {
                    Column {
                        OutlinedTextField(
                            value = instanceUrl,
                            onValueChange = { instanceUrl = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            label = { Text(text = stringResource(id = R.string.auth_linkding_instance_url)) },
                            isError = instanceUrlError != null,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Uri,
                            ),
                            keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Next) },
                            singleLine = true,
                            maxLines = 1,
                        )

                        if (instanceUrlError != null) {
                            Text(
                                text = instanceUrlError,
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                var authTokenVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    visualTransformation = if (authTokenVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    label = { Text(text = stringResource(id = R.string.auth_token_hint)) },
                    trailingIcon = {
                        IconButton(
                            onClick = { authTokenVisible = !authTokenVisible },
                        ) {
                            AnimatedContent(
                                targetState = authTokenVisible,
                                label = "AuthTokenIconVisibility",
                            ) { visible ->
                                Icon(
                                    painter = painterResource(
                                        id = if (visible) R.drawable.ic_eye else R.drawable.ic_eye_slash,
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                    },
                    isError = apiTokenError != null,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions { onAuthRequested(authToken, instanceUrl) },
                    singleLine = true,
                    maxLines = 1,
                )

                if (apiTokenError != null) {
                    Text(
                        text = apiTokenError,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                AnimatedContent(
                    targetState = isLoading,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                    label = "button-progress",
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Button(
                            onClick = { onAuthRequested(authToken, instanceUrl) },
                        ) {
                            Text(text = stringResource(id = R.string.auth_button))
                        }
                    }
                }

                TextButton(
                    onClick = { onUseLinkdingChanged(!useLinkding) },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (useLinkding) R.string.auth_switch_to_pinboard else R.string.auth_switch_to_linkding,
                        ),
                    )
                }

                AuthTokenHelp(
                    useLinkding = useLinkding,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AuthTokenHelp(
    useLinkding: Boolean,
    modifier: Modifier = Modifier,
) {
    var helpVisible by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = helpVisible,
        modifier = modifier,
        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + scaleOut() },
        label = "help-icon",
    ) { visible ->
        if (visible) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextWithLinks(
                    text = HtmlCompat.fromHtml(
                        stringResource(
                            if (useLinkding) R.string.auth_linkding_description else R.string.auth_token_description,
                        ),
                        HtmlCompat.FROM_HTML_MODE_COMPACT,
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    linkColor = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LongClickIconButton(
                painter = painterResource(id = R.drawable.ic_help),
                description = stringResource(id = R.string.hint_help),
                onClick = { helpVisible = true },
            )
        }
    }
}

// region Previews
@Composable
@ThemePreviews
@DevicePreviews
private fun AuthScreenPreview() {
    ExtendedTheme {
        AuthScreen(
            useLinkding = false,
            linkdingInstanceUrl = "",
            onUseLinkdingChanged = {},
            onAuthRequested = { _, _ -> },
            isLoading = false,
            apiTokenError = null,
            instanceUrlError = null,
        )
    }
}

@Composable
@ThemePreviews
@DevicePreviews
private fun AuthScreenLinkdingPreview() {
    ExtendedTheme {
        AuthScreen(
            useLinkding = true,
            linkdingInstanceUrl = "",
            onUseLinkdingChanged = {},
            onAuthRequested = { _, _ -> },
            isLoading = false,
            apiTokenError = null,
            instanceUrlError = null,
        )
    }
}

@Composable
@ThemePreviews
@DevicePreviews
private fun AuthScreenLoadingPreview() {
    ExtendedTheme {
        AuthScreen(
            useLinkding = false,
            linkdingInstanceUrl = "",
            onUseLinkdingChanged = {},
            onAuthRequested = { _, _ -> },
            isLoading = true,
            apiTokenError = null,
            instanceUrlError = null,
        )
    }
}

@Composable
@ThemePreviews
@DevicePreviews
private fun AuthScreenErrorPreview() {
    ExtendedTheme {
        AuthScreen(
            useLinkding = false,
            linkdingInstanceUrl = "",
            onUseLinkdingChanged = {},
            onAuthRequested = { _, _ -> },
            isLoading = false,
            apiTokenError = "Some error happened. Please try again.",
            instanceUrlError = null,
        )
    }
}
// endregion Previews
