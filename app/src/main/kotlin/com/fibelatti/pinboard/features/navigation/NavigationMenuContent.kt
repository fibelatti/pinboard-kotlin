package com.fibelatti.pinboard.features.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Backup
import com.fibelatti.pinboard.core.android.icons.Bookmarks
import com.fibelatti.pinboard.core.android.icons.Feedback
import com.fibelatti.pinboard.core.android.icons.Filter
import com.fibelatti.pinboard.core.android.icons.Notes
import com.fibelatti.pinboard.core.android.icons.Person
import com.fibelatti.pinboard.core.android.icons.Preferences
import com.fibelatti.pinboard.core.android.icons.PrivacyPolicy
import com.fibelatti.pinboard.core.android.icons.Rate
import com.fibelatti.pinboard.core.android.icons.Share
import com.fibelatti.pinboard.core.android.icons.Tag
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewAccountSwitcher
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewSavedFilters
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NavigationMenuContent(
    appMode: AppMode,
    onNavOptionClick: (Action) -> Unit,
    onExportClick: () -> Unit,
    onSendFeedbackClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationMenuContent(
        appMode = appMode,
        onAllClick = { onNavOptionClick(All) },
        onRecentClick = { onNavOptionClick(Recent) },
        onPublicClick = { onNavOptionClick(Public) },
        onPrivateClick = { onNavOptionClick(Private) },
        onReadLaterClick = { onNavOptionClick(Unread) },
        onUntaggedClick = { onNavOptionClick(Untagged) },
        onSavedFiltersClick = { onNavOptionClick(ViewSavedFilters) },
        onTagsClick = { onNavOptionClick(ViewTags) },
        onNotesClick = { onNavOptionClick(ViewNotes) },
        onPopularClick = { onNavOptionClick(ViewPopular) },
        onPreferencesClick = { onNavOptionClick(ViewPreferences) },
        onAccountsClick = { onNavOptionClick(ViewAccountSwitcher) },
        onExportClick = onExportClick,
        onSendFeedbackClick = onSendFeedbackClick,
        onWriteReviewClick = onWriteReviewClick,
        onShareClick = onShareClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onLicensesClick = onLicensesClick,
        modifier = modifier,
    )
}

@Composable
private fun NavigationMenuContent(
    appMode: AppMode,
    onAllClick: () -> Unit,
    onRecentClick: () -> Unit,
    onPublicClick: () -> Unit,
    onPrivateClick: () -> Unit,
    onReadLaterClick: () -> Unit,
    onUntaggedClick: () -> Unit,
    onSavedFiltersClick: () -> Unit,
    onTagsClick: () -> Unit,
    onNotesClick: () -> Unit,
    onPopularClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onExportClick: () -> Unit,
    onSendFeedbackClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        val serviceName = remember(appMode) {
            when (appMode) {
                AppMode.PINBOARD -> R.string.pinboard
                AppMode.LINKDING -> R.string.linkding
                else -> null
            }
        }

        if (serviceName != null) {
            Text(
                text = stringResource(id = serviceName),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        MenuItem(
            textRes = R.string.menu_navigation_all,
            onClick = onAllClick,
            icon = AppIcons.Bookmarks,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(2.dp),
                bottomEnd = CornerSize(2.dp),
            ),
        )

        MenuItem(
            textRes = R.string.menu_navigation_recent,
            onClick = onRecentClick,
            icon = AppIcons.Bookmarks,
        )

        if (AppMode.NO_API != appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_public,
                onClick = onPublicClick,
                icon = AppIcons.Bookmarks,
            )

            MenuItem(
                textRes = R.string.menu_navigation_private,
                onClick = onPrivateClick,
                icon = AppIcons.Bookmarks,
            )
        }

        MenuItem(
            textRes = R.string.menu_navigation_unread,
            onClick = onReadLaterClick,
            icon = AppIcons.Bookmarks,
        )

        MenuItem(
            textRes = R.string.menu_navigation_untagged,
            onClick = onUntaggedClick,
            icon = AppIcons.Bookmarks,
            shape = if (AppMode.PINBOARD == appMode) {
                RoundedCornerShape(2.dp)
            } else {
                MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(2.dp),
                    topEnd = CornerSize(2.dp),
                )
            },
        )

        if (AppMode.PINBOARD == appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_popular,
                onClick = onPopularClick,
                icon = AppIcons.Bookmarks,
                shape = MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(2.dp),
                    topEnd = CornerSize(2.dp),
                ),
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        MenuItem(
            textRes = R.string.menu_navigation_saved_filters,
            onClick = onSavedFiltersClick,
            icon = AppIcons.Filter,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(2.dp),
                bottomEnd = CornerSize(2.dp),
            ),
        )

        MenuItem(
            textRes = R.string.menu_navigation_tags,
            onClick = onTagsClick,
            icon = AppIcons.Tag,
            shape = if (AppMode.PINBOARD == appMode) {
                RoundedCornerShape(2.dp)
            } else {
                MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(2.dp),
                    topEnd = CornerSize(2.dp),
                )
            },
        )

        if (AppMode.PINBOARD == appMode) {
            MenuItem(
                textRes = R.string.menu_navigation_notes,
                onClick = onNotesClick,
                icon = AppIcons.Notes,
                shape = MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(2.dp),
                    topEnd = CornerSize(2.dp),
                ),
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        MenuItem(
            textRes = R.string.menu_navigation_preferences,
            onClick = onPreferencesClick,
            icon = AppIcons.Preferences,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(2.dp),
                bottomEnd = CornerSize(2.dp),
            ),
        )

        MenuItem(
            textRes = R.string.menu_navigation_accounts,
            onClick = onAccountsClick,
            icon = AppIcons.Person,
        )

        MenuItem(
            textRes = R.string.menu_navigation_export,
            onClick = onExportClick,
            icon = AppIcons.Backup,
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(2.dp),
                topEnd = CornerSize(2.dp),
            ),
        )

        Spacer(modifier = Modifier.height(30.dp))

        MenuItem(
            textRes = R.string.about_send_feedback,
            onClick = onSendFeedbackClick,
            icon = AppIcons.Feedback,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(2.dp),
                bottomEnd = CornerSize(2.dp),
            ),
        )

        MenuItem(
            textRes = R.string.about_rate,
            onClick = onWriteReviewClick,
            icon = AppIcons.Rate,
        )

        MenuItem(
            textRes = R.string.about_share,
            onClick = onShareClick,
            icon = AppIcons.Share,
        )

        MenuItem(
            textRes = R.string.about_privacy_policy,
            onClick = onPrivacyPolicyClick,
            icon = AppIcons.PrivacyPolicy,
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(2.dp),
                topEnd = CornerSize(2.dp),
            ),
        )

        Spacer(modifier = Modifier.height(30.dp))

        AppVersionDetails(
            onClick = onLicensesClick,
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
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AutoSizeText(
            text = stringResource(id = R.string.about_developer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 1,
        )

        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = stringResource(id = R.string.about_oss_licenses),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MenuItem(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    shape: CornerBasedShape = RoundedCornerShape(2.dp),
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize(),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
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
@PreviewAll
private fun NavigationMenuContentPreview() {
    ExtendedTheme {
        NavigationMenuContent(
            appMode = AppMode.PINBOARD,
            onAllClick = {},
            onRecentClick = {},
            onPublicClick = {},
            onPrivateClick = {},
            onReadLaterClick = {},
            onUntaggedClick = {},
            onSavedFiltersClick = {},
            onTagsClick = {},
            onNotesClick = {},
            onPopularClick = {},
            onPreferencesClick = {},
            onAccountsClick = {},
            onExportClick = {},
            onSendFeedbackClick = {},
            onWriteReviewClick = {},
            onShareClick = {},
            onPrivacyPolicyClick = {},
            onLicensesClick = {},
        )
    }
}
