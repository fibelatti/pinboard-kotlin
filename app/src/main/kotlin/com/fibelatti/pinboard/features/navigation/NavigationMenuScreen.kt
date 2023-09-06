package com.fibelatti.pinboard.features.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NavigationMenuScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    mainVariant: Boolean = true,
    appReviewMode: Boolean = false,
    onShareClicked: () -> Unit,
    onRateClicked: () -> Unit,
    onOptionSelected: () -> Unit,
) {
    NavigationMenuScreen(
        mainVariant = mainVariant,
        appReviewMode = appReviewMode,
        onAllClicked = {
            appStateViewModel.runAction(All)
            onOptionSelected()
        },
        onRecentClicked = {
            appStateViewModel.runAction(Recent)
            onOptionSelected()
        },
        onPublicClicked = {
            appStateViewModel.runAction(Public)
            onOptionSelected()
        },
        onPrivateClicked = {
            appStateViewModel.runAction(Private)
            onOptionSelected()
        },
        onReadLaterClicked = {
            appStateViewModel.runAction(Unread)
            onOptionSelected()
        },
        onUntaggedClicked = {
            appStateViewModel.runAction(Untagged)
            onOptionSelected()
        },
        onTagsClicked = {
            appStateViewModel.runAction(ViewTags)
            onOptionSelected()
        },
        onNotesClicked = {
            appStateViewModel.runAction(ViewNotes)
            onOptionSelected()
        },
        onPopularClicked = {
            appStateViewModel.runAction(ViewPopular)
            onOptionSelected()
        },
        onPreferencesClicked = {
            appStateViewModel.runAction(ViewPreferences)
            onOptionSelected()
        },
        onLogoutClicked = {
            authViewModel.logout()
            onOptionSelected()
        },
        onShareClicked = {
            onShareClicked()
            onOptionSelected()
        },
        onRateClicked = {
            onRateClicked()
            onOptionSelected()
        },
    )
}

@Composable
private fun NavigationMenuScreen(
    mainVariant: Boolean = true,
    appReviewMode: Boolean = false,
    onAllClicked: () -> Unit,
    onRecentClicked: () -> Unit,
    onPublicClicked: () -> Unit,
    onPrivateClicked: () -> Unit,
    onReadLaterClicked: () -> Unit,
    onUntaggedClicked: () -> Unit,
    onTagsClicked: () -> Unit,
    onNotesClicked: () -> Unit,
    onPopularClicked: () -> Unit,
    onPreferencesClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onRateClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 100.dp),
    ) {
        MenuItem(textRes = R.string.menu_navigation_all, onClick = onAllClicked)

        MenuItem(textRes = R.string.menu_navigation_recent, onClick = onRecentClicked)

        if (mainVariant) {
            MenuItem(textRes = R.string.menu_navigation_public, onClick = onPublicClicked)

            MenuItem(textRes = R.string.menu_navigation_private, onClick = onPrivateClicked)
        }

        MenuItem(textRes = R.string.menu_navigation_unread, onClick = onReadLaterClicked)

        MenuItem(textRes = R.string.menu_navigation_untagged, onClick = onUntaggedClicked)

        MenuItem(textRes = R.string.menu_navigation_tags, onClick = onTagsClicked)

        if (mainVariant && !appReviewMode) {
            MenuItem(textRes = R.string.menu_navigation_notes, onClick = onNotesClicked)
        }

        MenuItem(textRes = R.string.menu_navigation_popular, onClick = onPopularClicked)

        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        MenuItem(
            textRes = R.string.menu_navigation_preferences,
            onClick = onPreferencesClicked,
            iconRes = R.drawable.ic_preferences,
        )

        if (mainVariant) {
            MenuItem(
                textRes = R.string.menu_navigation_logout,
                onClick = onLogoutClicked,
                iconRes = R.drawable.ic_person,
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        MenuItem(
            textRes = R.string.about_share,
            onClick = onShareClicked,
            textStyle = MaterialTheme.typography.bodySmall,
        )

        MenuItem(
            textRes = R.string.about_rate,
            onClick = onRateClicked,
            textStyle = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = stringResource(id = R.string.about_developer),
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily(Font(R.font.jetbrainsmono)),
            style = MaterialTheme.typography.bodySmall,
        )

        runCatching {
            val context = LocalContext.current

            @Suppress("DEPRECATION")
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            Text(
                text = stringResource(R.string.about_version, pInfo.versionName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily(Font(R.font.jetbrainsmono)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MenuItem(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    @DrawableRes iconRes: Int? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(all = 0.dp),
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(id = textRes),
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = stringResource(id = textRes),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
        )
    }
}

@Composable
@ThemePreviews
private fun NavigationMenuScreenPreview() {
    ExtendedTheme {
        NavigationMenuScreen(
            onAllClicked = {},
            onRecentClicked = {},
            onPublicClicked = {},
            onPrivateClicked = {},
            onReadLaterClicked = {},
            onUntaggedClicked = {},
            onTagsClicked = {},
            onNotesClicked = {},
            onPopularClicked = {},
            onPreferencesClicked = {},
            onLogoutClicked = {},
            onShareClicked = {},
            onRateClicked = {},
        )
    }
}
