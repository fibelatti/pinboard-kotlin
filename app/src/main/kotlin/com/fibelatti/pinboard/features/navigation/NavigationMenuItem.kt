package com.fibelatti.pinboard.features.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Backup
import com.fibelatti.pinboard.core.android.icons.Bookmarks
import com.fibelatti.pinboard.core.android.icons.Download
import com.fibelatti.pinboard.core.android.icons.Feedback
import com.fibelatti.pinboard.core.android.icons.Filter
import com.fibelatti.pinboard.core.android.icons.Notes
import com.fibelatti.pinboard.core.android.icons.Person
import com.fibelatti.pinboard.core.android.icons.Preferences
import com.fibelatti.pinboard.core.android.icons.PrivacyPolicy
import com.fibelatti.pinboard.core.android.icons.Rate
import com.fibelatti.pinboard.core.android.icons.Share
import com.fibelatti.pinboard.core.android.icons.Tag
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.Archived
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.ContentWithHistory
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.appstate.ViewAccountSwitcher
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewOfflineCopies
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewSavedFilters
import com.fibelatti.pinboard.features.appstate.ViewTags

@Immutable
data class NavigationMenuItem(
    @StringRes val textRes: Int,
    val icon: ImageVector,
    val action: NavigationMenuAction,
    /** Whether the item is a primary destination, shown even when the navigation rail is collapsed. */
    val isPrimary: Boolean = false,
)

sealed interface NavigationMenuAction {

    data class Navigate(val action: Action) : NavigationMenuAction

    data object ExportBookmarks : NavigationMenuAction

    data object SendFeedback : NavigationMenuAction

    data object WriteReview : NavigationMenuAction

    data object ShareApp : NavigationMenuAction

    data object ViewPrivacyPolicy : NavigationMenuAction

    data object ViewLicenses : NavigationMenuAction
}

/**
 * Builds the navigation menu entries available in the given [appMode], grouped by how they are
 * presented.
 */
fun navigationMenuGroups(appMode: AppMode): List<List<NavigationMenuItem>> = listOf(
    buildList {
        add(navigationItem(R.string.menu_navigation_all, AppIcons.Bookmarks, All, isPrimary = true))
        add(navigationItem(R.string.menu_navigation_recent, AppIcons.Bookmarks, Recent))

        if (AppMode.NO_API != appMode) {
            add(navigationItem(R.string.menu_navigation_public, AppIcons.Bookmarks, Public))
            add(navigationItem(R.string.menu_navigation_private, AppIcons.Bookmarks, Private))
        }

        add(navigationItem(R.string.menu_navigation_unread, AppIcons.Bookmarks, Unread, isPrimary = true))
        add(navigationItem(R.string.menu_navigation_untagged, AppIcons.Bookmarks, Untagged))

        if (AppMode.LINKDING == appMode) {
            add(navigationItem(R.string.menu_navigation_archived, AppIcons.Bookmarks, Archived, isPrimary = true))
        }

        if (AppMode.PINBOARD == appMode) {
            add(navigationItem(R.string.menu_navigation_popular, AppIcons.Bookmarks, ViewPopular))
        }
    },
    buildList {
        add(
            navigationItem(
                R.string.menu_navigation_offline_copies,
                AppIcons.Download,
                ViewOfflineCopies,
                isPrimary = true,
            ),
        )
        add(navigationItem(R.string.menu_navigation_saved_filters, AppIcons.Filter, ViewSavedFilters, isPrimary = true))
        add(navigationItem(R.string.menu_navigation_tags, AppIcons.Tag, ViewTags, isPrimary = true))

        if (AppMode.PINBOARD == appMode) {
            add(navigationItem(R.string.menu_navigation_notes, AppIcons.Notes, ViewNotes, isPrimary = true))
        }
    },
    listOf(
        navigationItem(R.string.menu_navigation_preferences, AppIcons.Preferences, ViewPreferences, isPrimary = true),
        navigationItem(R.string.menu_navigation_accounts, AppIcons.Person, ViewAccountSwitcher),
        NavigationMenuItem(
            textRes = R.string.menu_navigation_export,
            icon = AppIcons.Backup,
            action = NavigationMenuAction.ExportBookmarks,
        ),
    ),
    listOf(
        NavigationMenuItem(
            textRes = R.string.about_send_feedback,
            icon = AppIcons.Feedback,
            action = NavigationMenuAction.SendFeedback,
        ),
        NavigationMenuItem(
            textRes = R.string.about_rate,
            icon = AppIcons.Rate,
            action = NavigationMenuAction.WriteReview,
        ),
        NavigationMenuItem(
            textRes = R.string.about_share,
            icon = AppIcons.Share,
            action = NavigationMenuAction.ShareApp,
        ),
        NavigationMenuItem(
            textRes = R.string.about_privacy_policy,
            icon = AppIcons.PrivacyPolicy,
            action = NavigationMenuAction.ViewPrivacyPolicy,
        ),
    ),
)

private fun navigationItem(
    @StringRes textRes: Int,
    icon: ImageVector,
    action: Action,
    isPrimary: Boolean = false,
): NavigationMenuItem = NavigationMenuItem(
    textRes = textRes,
    icon = icon,
    action = NavigationMenuAction.Navigate(action),
    isPrimary = isPrimary,
)

/**
 * Resolves the menu destination that leads to this content, so it can be marked as selected.
 */
fun Content.navigationMenuDestination(): Action? = when (this) {
    is PostListContent -> category
    is PopularPostsContent -> ViewPopular
    is OfflineCopyListContent -> ViewOfflineCopies
    is SavedFiltersContent -> ViewSavedFilters
    is TagListContent -> ViewTags
    is NoteListContent -> ViewNotes
    is UserPreferencesContent -> ViewPreferences
    is AccountSwitcherContent -> ViewAccountSwitcher
    is ContentWithHistory -> previousContent.navigationMenuDestination()
    else -> null
}
