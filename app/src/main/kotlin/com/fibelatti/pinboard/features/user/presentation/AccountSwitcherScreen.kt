@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.user.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.ui.preview.DevicePreviews
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun AccountSwitcherScreen(
    modifier: Modifier = Modifier,
    accountSwitcherViewModel: AccountSwitcherViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val credentials by accountSwitcherViewModel.userCredentials.collectAsStateWithLifecycle()

        AccountSwitcherScreen(
            userCredentials = credentials,
            onSelectReviewModeClick = {
                accountSwitcherViewModel.select(appMode = AppMode.NO_API)
            },
            onLogoutReviewModeClick = {
                accountSwitcherViewModel.logout(appMode = AppMode.NO_API)
            },
            onSelectPinboardClick = {
                accountSwitcherViewModel.select(appMode = AppMode.PINBOARD)
            },
            onLogoutPinboardClick = {
                accountSwitcherViewModel.logout(appMode = AppMode.PINBOARD)
            },
            onSelectLinkdingClick = {
                accountSwitcherViewModel.select(appMode = AppMode.LINKDING)
            },
            onLogoutLinkdingClick = {
                accountSwitcherViewModel.logout(appMode = AppMode.LINKDING)
            },
            onAddPinboardAccountClick = {
                accountSwitcherViewModel.addAccount(appMode = AppMode.PINBOARD)
            },
            onAddLinkdingAccountClick = {
                accountSwitcherViewModel.addAccount(appMode = AppMode.LINKDING)
            },
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
        )
    }
}

@Composable
private fun AccountSwitcherScreen(
    userCredentials: UserCredentials,
    onSelectReviewModeClick: () -> Unit,
    onLogoutReviewModeClick: () -> Unit,
    onSelectPinboardClick: () -> Unit,
    onLogoutPinboardClick: () -> Unit,
    onSelectLinkdingClick: () -> Unit,
    onLogoutLinkdingClick: () -> Unit,
    onAddPinboardAccountClick: () -> Unit,
    onAddLinkdingAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (AppMode.NO_API in userCredentials.getConnectedServices()) {
            item(key = "review_mode") {
                AccountItem(
                    title = "App Review Mode",
                    description = "Bookmarks are only saved locally and will be deleted on logout.",
                    onSelectClick = onSelectReviewModeClick,
                    onLogoutClick = onLogoutReviewModeClick,
                    modifier = Modifier.animateItem(),
                )
            }
            return@LazyColumn
        }

        if (AppMode.PINBOARD in userCredentials.getConnectedServices()) {
            item(key = "pinboard-account") {
                AccountItem(
                    title = stringResource(R.string.pinboard),
                    onSelectClick = onSelectPinboardClick,
                    onLogoutClick = onLogoutPinboardClick,
                    description = userCredentials.getPinboardUsername(),
                    modifier = Modifier.animateItem(),
                )
            }
        }

        if (AppMode.LINKDING in userCredentials.getConnectedServices()) {
            item(key = "linkding-account") {
                AccountItem(
                    title = stringResource(R.string.linkding),
                    onSelectClick = onSelectLinkdingClick,
                    onLogoutClick = onLogoutLinkdingClick,
                    description = userCredentials.linkdingInstanceUrl,
                    modifier = Modifier.animateItem(),
                )
            }
        }

        if (AppMode.PINBOARD !in userCredentials.getConnectedServices()) {
            item(key = "add-pinboard-account") {
                FilledTonalButton(
                    onClick = onAddPinboardAccountClick,
                    shapes = ExtendedTheme.defaultButtonShapes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                ) {
                    Text(text = stringResource(R.string.account_switcher_add_pinboard))
                }
            }
        }

        if (AppMode.LINKDING !in userCredentials.getConnectedServices()) {
            item(key = "add-linkding-account") {
                FilledTonalButton(
                    onClick = onAddLinkdingAccountClick,
                    shapes = ExtendedTheme.defaultButtonShapes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                ) {
                    Text(text = stringResource(R.string.account_switcher_add_linkding))
                }
            }
        }
    }
}

@Composable
private fun AccountItem(
    title: String,
    onSelectClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )

        if (description != null) {
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onSelectClick,
                shapes = ExtendedTheme.defaultButtonShapes,
                modifier = Modifier.weight(2f),
            ) {
                Text(text = stringResource(R.string.account_switcher_select))
            }

            TextButton(
                onClick = onLogoutClick,
                shapes = ExtendedTheme.defaultButtonShapes,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(text = stringResource(R.string.account_switcher_logout))
            }
        }
    }
}

// region Previews
@ThemePreviews
@DevicePreviews
@Composable
private fun AccountSwitcherScreenPinboardOnlyPreview() {
    ExtendedTheme {
        AccountSwitcherScreen(
            userCredentials = UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = null,
                linkdingAuthToken = null,
            ),
            onSelectReviewModeClick = {},
            onLogoutReviewModeClick = {},
            onSelectPinboardClick = {},
            onLogoutPinboardClick = {},
            onSelectLinkdingClick = {},
            onLogoutLinkdingClick = {},
            onAddPinboardAccountClick = {},
            onAddLinkdingAccountClick = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun AccountSwitcherScreenLinkdingOnlyPreview() {
    ExtendedTheme {
        AccountSwitcherScreen(
            userCredentials = UserCredentials(
                pinboardAuthToken = null,
                linkdingInstanceUrl = "https://my.linkding.com",
                linkdingAuthToken = "linkding-token",
            ),
            onSelectReviewModeClick = {},
            onLogoutReviewModeClick = {},
            onSelectPinboardClick = {},
            onLogoutPinboardClick = {},
            onSelectLinkdingClick = {},
            onLogoutLinkdingClick = {},
            onAddPinboardAccountClick = {},
            onAddLinkdingAccountClick = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun AccountSwitcherScreenReviewModePreview() {
    ExtendedTheme {
        AccountSwitcherScreen(
            userCredentials = UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = "https://my.linkding.com",
                linkdingAuthToken = "linkding-token",
                appReviewMode = true,
            ),
            onSelectReviewModeClick = {},
            onLogoutReviewModeClick = {},
            onSelectPinboardClick = {},
            onLogoutPinboardClick = {},
            onSelectLinkdingClick = {},
            onLogoutLinkdingClick = {},
            onAddPinboardAccountClick = {},
            onAddLinkdingAccountClick = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun AccountSwitcherScreenPreview() {
    ExtendedTheme {
        AccountSwitcherScreen(
            userCredentials = UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = "https://my.linkding.com",
                linkdingAuthToken = "linkding-token",
            ),
            onSelectReviewModeClick = {},
            onLogoutReviewModeClick = {},
            onSelectPinboardClick = {},
            onLogoutPinboardClick = {},
            onSelectLinkdingClick = {},
            onLogoutLinkdingClick = {},
            onAddPinboardAccountClick = {},
            onAddLinkdingAccountClick = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}
// endregion Previews
