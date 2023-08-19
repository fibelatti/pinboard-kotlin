package com.fibelatti.pinboard.features.user.presentation

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.ui.components.TextWithLinks
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val screenState by authViewModel.screenState.collectAsStateWithLifecycle()

    AuthScreen(
        onAuthRequested = authViewModel::login,
        isLoading = screenState.isLoading,
        error = screenState.apiTokenError,
    )
}

@Composable
private fun AuthScreen(
    onAuthRequested: (token: String) -> Unit,
    isLoading: Boolean,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .padding(horizontal = 32.dp),
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
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(all = 16.dp),
            ) {
                var authToken by remember { mutableStateOf("") }

                Text(
                    text = stringResource(id = R.string.auth_title),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    label = { Text(text = stringResource(id = R.string.auth_token_hint)) },
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions { onAuthRequested(authToken) },
                    singleLine = true,
                    maxLines = 1,
                )

                if (error != null) {
                    Text(
                        text = error,
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
                            onClick = { onAuthRequested(authToken) },
                        ) {
                            Text(text = stringResource(id = R.string.auth_button))
                        }
                    }
                }

                AuthTokenHelp(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun AuthTokenHelp(
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
                Text(
                    text = stringResource(id = R.string.auth_token_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )

                TextWithLinks(
                    text = HtmlCompat.fromHtml(
                        stringResource(R.string.auth_token_description),
                        HtmlCompat.FROM_HTML_MODE_COMPACT,
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    linkColor = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            IconButton(
                onClick = { helpVisible = true },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_help),
                    contentDescription = stringResource(id = R.string.hint_help),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun AuthScreenPreview() {
    ExtendedTheme {
        AuthScreen(
            onAuthRequested = {},
            isLoading = false,
            error = null,
        )
    }
}

@Composable
@ThemePreviews
private fun AuthScreenLoadingPreview() {
    ExtendedTheme {
        AuthScreen(
            onAuthRequested = {},
            isLoading = true,
            error = null,
        )
    }
}

@Composable
@ThemePreviews
private fun AuthScreenErrorPreview() {
    ExtendedTheme {
        AuthScreen(
            onAuthRequested = {},
            isLoading = false,
            error = "Some error happened. Please try again.",
        )
    }
}
