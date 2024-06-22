package com.fibelatti.pinboard.core.persistence

import androidx.annotation.VisibleForTesting
import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

// region Constants
@VisibleForTesting
const val KEY_AUTH_TOKEN = "AUTH_TOKEN"

@VisibleForTesting
const val KEY_LAST_UPDATE = "LAST_UPDATE"

@VisibleForTesting
const val KEY_PERIODIC_SYNC = "PERIODIC_SYNC"

@VisibleForTesting
const val KEY_APPEARANCE = "APPEARANCE"

@VisibleForTesting
const val KEY_APPLY_DYNAMIC_COLORS = "APPLY_DYNAMIC_COLORS"

@VisibleForTesting
const val KEY_DISABLE_SCREENSHOTS = "DISABLE_SCREENSHOTS"

@VisibleForTesting
const val KEY_PREFERRED_DATE_FORMAT = "PREFERRED_DATE_FORMAT"

@VisibleForTesting
const val KEY_PREFERRED_DETAILS_VIEW = "PREFERRED_DETAILS_VIEW"

@VisibleForTesting
const val KEY_ALWAYS_USE_SIDE_PANEL = "ALWAYS_USE_SIDE_PANEL"

@VisibleForTesting
const val KEY_MARK_AS_READ_ON_OPEN = "MARK_AS_READ_ON_OPEN"

@VisibleForTesting
const val KEY_AUTO_FILL_DESCRIPTION = "AUTO_FILL_DESCRIPTION"

@VisibleForTesting
const val KEY_SHOW_DESCRIPTION_IN_LISTS = "SHOW_DESCRIPTION_IN_LISTS"

@VisibleForTesting
const val KEY_DEFAULT_PRIVATE = "DEFAULT_PRIVATE"

@VisibleForTesting
const val KEY_DEFAULT_READ_LATER = "DEFAULT_READ_LATER"

@VisibleForTesting
const val KEY_NEW_EDIT_AFTER_SHARING = "NEW_EDIT_AFTER_SHARING"

@VisibleForTesting
const val KEY_DEFAULT_TAGS = "DEFAULT_TAGS"
// endregion

@Single
class UserSharedPreferences(private val settings: Settings) {

    private var currentLinkdingInstanceUrl = ""
    private var currentAuthToken: String = ""
    private var currentLastUpdate: String = ""

    var useLinkding: Boolean
        get() = settings.getBoolean("USE_LINKDING", false)
        set(value) = settings.putBoolean("USE_LINKDING", value)

    var linkdingInstanceUrl: String
        get() = settings.getString("LINKDING_INSTANCE_URL", currentLinkdingInstanceUrl)
        set(value) = settings.putString("LINKDING_INSTANCE_URL", value).also { currentLinkdingInstanceUrl = value }

    var authToken: String
        get() = settings.getString(KEY_AUTH_TOKEN, currentAuthToken)
        set(value) = settings.putString(KEY_AUTH_TOKEN, value).also { currentAuthToken = value }

    var lastUpdate: String
        get() = settings.getString(KEY_LAST_UPDATE, currentLastUpdate)
        set(value) = settings.putString(KEY_LAST_UPDATE, value).also { currentLastUpdate = value }

    var periodicSync: Long
        get() = settings.getLong(KEY_PERIODIC_SYNC, 24)
        set(value) = settings.putLong(KEY_PERIODIC_SYNC, value)

    var appearance: String
        get() = settings.getString(KEY_APPEARANCE, "")
        set(value) = settings.putString(KEY_APPEARANCE, value)

    var applyDynamicColors: Boolean
        get() = settings.getBoolean(KEY_APPLY_DYNAMIC_COLORS, false)
        set(value) = settings.putBoolean(KEY_APPLY_DYNAMIC_COLORS, value)

    var disableScreenshots: Boolean
        get() = settings.getBoolean(KEY_DISABLE_SCREENSHOTS, false)
        set(value) = settings.putBoolean(KEY_DISABLE_SCREENSHOTS, value)

    var preferredDateFormat: String
        get() = settings.getString(KEY_PREFERRED_DATE_FORMAT, "")
        set(value) = settings.putString(KEY_PREFERRED_DATE_FORMAT, value)

    var preferredDetailsView: String
        get() = settings.getString(KEY_PREFERRED_DETAILS_VIEW, "")
        set(value) = settings.putString(KEY_PREFERRED_DETAILS_VIEW, value)

    var alwaysUseSidePanel: Boolean
        get() = settings.getBoolean(KEY_ALWAYS_USE_SIDE_PANEL, false)
        set(value) = settings.putBoolean(KEY_ALWAYS_USE_SIDE_PANEL, value)

    var markAsReadOnOpen: Boolean
        get() = settings.getBoolean(KEY_MARK_AS_READ_ON_OPEN, false)
        set(value) = settings.putBoolean(KEY_MARK_AS_READ_ON_OPEN, value)

    var autoFillDescription: Boolean
        get() = settings.getBoolean(KEY_AUTO_FILL_DESCRIPTION, false)
        set(value) = settings.putBoolean(KEY_AUTO_FILL_DESCRIPTION, value)

    var showDescriptionInLists: Boolean
        get() = settings.getBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, true)
        set(value) = settings.putBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, value)

    /**
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    var defaultPrivate: Boolean?
        get() = settings.getBoolean(KEY_DEFAULT_PRIVATE, false).takeIf { it }
        set(value) = settings.putBoolean(KEY_DEFAULT_PRIVATE, value ?: false)

    /**
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    var defaultReadLater: Boolean?
        get() = settings.getBoolean(KEY_DEFAULT_READ_LATER, false).takeIf { it }
        set(value) = settings.putBoolean(KEY_DEFAULT_READ_LATER, value ?: false)

    var editAfterSharing: String
        get() = settings.getString(KEY_NEW_EDIT_AFTER_SHARING, "")
        set(value) = settings.putString(KEY_NEW_EDIT_AFTER_SHARING, value)

    var defaultTags: List<String>
        get() = settings.getString(KEY_DEFAULT_TAGS, "")
            .takeIf { it.isNotBlank() }?.split(",").orEmpty()
        set(value) = settings.putString(KEY_DEFAULT_TAGS, value.joinToString(separator = ","))
}
