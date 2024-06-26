package com.fibelatti.pinboard.features.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.features.appstate.All
import com.fibelatti.bookmarking.features.appstate.AppStateViewModel
import com.fibelatti.bookmarking.features.appstate.Private
import com.fibelatti.bookmarking.features.appstate.Public
import com.fibelatti.bookmarking.features.appstate.Recent
import com.fibelatti.bookmarking.features.appstate.Unread
import com.fibelatti.bookmarking.features.appstate.Untagged
import com.fibelatti.bookmarking.features.appstate.ViewNotes
import com.fibelatti.bookmarking.features.appstate.ViewPopular
import com.fibelatti.bookmarking.features.appstate.ViewPreferences
import com.fibelatti.bookmarking.features.appstate.ViewSavedFilters
import com.fibelatti.bookmarking.features.appstate.ViewTags
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationMenuScreen(
    appStateViewModel: AppStateViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    onShareClicked: () -> Unit,
    onRateClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    onOptionSelected: () -> Unit,
) {
    val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()

    NavigationMenuScreen(
        appMode = appMode,
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
        onSavedFiltersClicked = {
            appStateViewModel.runAction(ViewSavedFilters)
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
        onLicensesClicked = {
            onLicensesClicked()
            onOptionSelected()
        },
    )
}

@Composable
private fun NavigationMenuScreen(
    appMode: AppMode = AppMode.PINBOARD,
    onAllClicked: () -> Unit,
    onRecentClicked: () -> Unit,
    onPublicClicked: () -> Unit,
    onPrivateClicked: () -> Unit,
    onReadLaterClicked: () -> Unit,
    onUntaggedClicked: () -> Unit,
    onSavedFiltersClicked: () -> Unit,
    onTagsClicked: () -> Unit,
    onNotesClicked: () -> Unit,
    onPopularClicked: () -> Unit,
    onPreferencesClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onRateClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp, bottom = 64.dp),
    ) {
        MenuItem(
            textRes = R.string.menu_navigation_all,
            onClick = onAllClicked,
            iconRes = R.drawable.ic_bookmarks,
        )

        MenuItem(
            textRes = R.string.menu_navigation_recent,
            onClick = onRecentClicked,
            iconRes = R.drawable.ic_bookmarks,
        )

        if (AppMode.NO_API != appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_public,
                onClick = onPublicClicked,
                iconRes = R.drawable.ic_bookmarks,
            )

            MenuItem(
                textRes = R.string.menu_navigation_private,
                onClick = onPrivateClicked,
                iconRes = R.drawable.ic_bookmarks,
            )
        }

        MenuItem(
            textRes = R.string.menu_navigation_unread,
            onClick = onReadLaterClicked,
            iconRes = R.drawable.ic_bookmarks,
        )

        MenuItem(
            textRes = R.string.menu_navigation_untagged,
            onClick = onUntaggedClicked,
            iconRes = R.drawable.ic_bookmarks,
        )

        MenuItem(
            textRes = R.string.menu_navigation_saved_filters,
            onClick = onSavedFiltersClicked,
            iconRes = R.drawable.ic_filter,
        )

        MenuItem(
            textRes = R.string.menu_navigation_tags,
            onClick = onTagsClicked,
            iconRes = R.drawable.ic_tag,
        )

        if (AppMode.PINBOARD == appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_notes,
                onClick = onNotesClicked,
                iconRes = R.drawable.ic_notes,
            )

            MenuItem(
                textRes = R.string.menu_navigation_popular,
                onClick = onPopularClicked,
                iconRes = R.drawable.ic_bookmarks,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(all = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        MenuItem(
            textRes = R.string.menu_navigation_preferences,
            onClick = onPreferencesClicked,
            iconRes = R.drawable.ic_preferences,
        )

        if (AppMode.NO_API != appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_logout,
                onClick = onLogoutClicked,
                iconRes = R.drawable.ic_person,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(all = 16.dp),
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

        AppVersionDetails(
            onClick = onLicensesClicked,
            modifier = Modifier.padding(start = 16.dp, top = 32.dp, end = 16.dp),
        )
    }
}

@Composable
private fun AppVersionDetails(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button),
    ) {
        Text(
            text = stringResource(id = R.string.about_developer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily(Font(R.font.jetbrainsmono)),
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily(Font(R.font.jetbrainsmono)),
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = stringResource(id = R.string.about_oss_licenses),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily(Font(R.font.jetbrainsmono)),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MenuItem(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    @DrawableRes iconRes: Int? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(id = textRes),
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp),
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
            onSavedFiltersClicked = {},
            onTagsClicked = {},
            onNotesClicked = {},
            onPopularClicked = {},
            onPreferencesClicked = {},
            onLogoutClicked = {},
            onShareClicked = {},
            onRateClicked = {},
            onLicensesClicked = {},
        )
    }
}
