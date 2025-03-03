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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewSavedFilters
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NavigationMenuScreen(
    onSendFeedbackClicked: () -> Unit,
    onWriteReviewClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    onOptionSelected: () -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by mainViewModel.appState.collectAsStateWithLifecycle()

    NavigationMenuScreen(
        appMode = state.appMode,
        onAllClicked = {
            mainViewModel.runAction(All)
            onOptionSelected()
        },
        onRecentClicked = {
            mainViewModel.runAction(Recent)
            onOptionSelected()
        },
        onPublicClicked = {
            mainViewModel.runAction(Public)
            onOptionSelected()
        },
        onPrivateClicked = {
            mainViewModel.runAction(Private)
            onOptionSelected()
        },
        onReadLaterClicked = {
            mainViewModel.runAction(Unread)
            onOptionSelected()
        },
        onUntaggedClicked = {
            mainViewModel.runAction(Untagged)
            onOptionSelected()
        },
        onSavedFiltersClicked = {
            mainViewModel.runAction(ViewSavedFilters)
            onOptionSelected()
        },
        onTagsClicked = {
            mainViewModel.runAction(ViewTags)
            onOptionSelected()
        },
        onNotesClicked = {
            mainViewModel.runAction(ViewNotes)
            onOptionSelected()
        },
        onPopularClicked = {
            mainViewModel.runAction(ViewPopular)
            onOptionSelected()
        },
        onPreferencesClicked = {
            mainViewModel.runAction(ViewPreferences)
            onOptionSelected()
        },
        onLogoutClicked = {
            authViewModel.logout()
            onOptionSelected()
        },
        onSendFeedbackClicked = {
            onSendFeedbackClicked()
            onOptionSelected()
        },
        onWriteReviewClicked = {
            onWriteReviewClicked()
            onOptionSelected()
        },
        onShareClicked = {
            onShareClicked()
            onOptionSelected()
        },
        onPrivacyPolicyClicked = {
            onPrivacyPolicyClicked()
            onOptionSelected()
        },
        onLicensesClicked = {
            onLicensesClicked()
            onOptionSelected()
        },
        modifier = modifier,
    )
}

@Composable
private fun NavigationMenuScreen(
    appMode: AppMode,
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
    onSendFeedbackClicked: () -> Unit,
    onWriteReviewClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
            textRes = R.string.about_send_feedback,
            onClick = onSendFeedbackClicked,
            iconRes = R.drawable.ic_feedback,
        )

        MenuItem(
            textRes = R.string.about_rate,
            onClick = onWriteReviewClicked,
            iconRes = R.drawable.ic_rate,
        )

        MenuItem(
            textRes = R.string.about_share,
            onClick = onShareClicked,
            iconRes = R.drawable.ic_share,
        )

        MenuItem(
            textRes = R.string.about_privacy_policy,
            onClick = onPrivacyPolicyClicked,
            iconRes = R.drawable.ic_privacy_policy,
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
        )
    }
}

@Composable
@ThemePreviews
private fun NavigationMenuScreenPreview() {
    ExtendedTheme {
        NavigationMenuScreen(
            appMode = AppMode.PINBOARD,
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
            onSendFeedbackClicked = {},
            onWriteReviewClicked = {},
            onShareClicked = {},
            onPrivacyPolicyClicked = {},
            onLicensesClicked = {},
        )
    }
}
