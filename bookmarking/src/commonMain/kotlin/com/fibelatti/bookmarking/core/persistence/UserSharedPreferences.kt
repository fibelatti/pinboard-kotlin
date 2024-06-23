package com.fibelatti.bookmarking.core.persistence

import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

@Single
public class UserSharedPreferences(private val settings: Settings) {

    private var currentLinkdingInstanceUrl = ""
    private var currentAuthToken: String = ""
    private var currentLastUpdate: String = ""

    public var useLinkding: Boolean
        get() = settings.getBoolean("USE_LINKDING", false)
        set(value) = settings.putBoolean("USE_LINKDING", value)

    public var linkdingInstanceUrl: String
        get() = settings.getString("LINKDING_INSTANCE_URL", currentLinkdingInstanceUrl)
        set(value) = settings.putString("LINKDING_INSTANCE_URL", value).also { currentLinkdingInstanceUrl = value }

    public var authToken: String
        get() = settings.getString(KEY_AUTH_TOKEN, currentAuthToken)
        set(value) = settings.putString(KEY_AUTH_TOKEN, value).also { currentAuthToken = value }

    public var lastUpdate: String
        get() = settings.getString(KEY_LAST_UPDATE, currentLastUpdate)
        set(value) = settings.putString(KEY_LAST_UPDATE, value).also { currentLastUpdate = value }

    public var periodicSync: Long
        get() = settings.getLong(KEY_PERIODIC_SYNC, 24)
        set(value) = settings.putLong(KEY_PERIODIC_SYNC, value)

    public var appearance: String
        get() = settings.getString(KEY_APPEARANCE, "")
        set(value) = settings.putString(KEY_APPEARANCE, value)

    public var applyDynamicColors: Boolean
        get() = settings.getBoolean(KEY_APPLY_DYNAMIC_COLORS, false)
        set(value) = settings.putBoolean(KEY_APPLY_DYNAMIC_COLORS, value)

    public var disableScreenshots: Boolean
        get() = settings.getBoolean(KEY_DISABLE_SCREENSHOTS, false)
        set(value) = settings.putBoolean(KEY_DISABLE_SCREENSHOTS, value)

    public var preferredDateFormat: String
        get() = settings.getString(KEY_PREFERRED_DATE_FORMAT, "")
        set(value) = settings.putString(KEY_PREFERRED_DATE_FORMAT, value)

    public var preferredDetailsView: String
        get() = settings.getString(KEY_PREFERRED_DETAILS_VIEW, "")
        set(value) = settings.putString(KEY_PREFERRED_DETAILS_VIEW, value)

    public var alwaysUseSidePanel: Boolean
        get() = settings.getBoolean(KEY_ALWAYS_USE_SIDE_PANEL, false)
        set(value) = settings.putBoolean(KEY_ALWAYS_USE_SIDE_PANEL, value)

    public var markAsReadOnOpen: Boolean
        get() = settings.getBoolean(KEY_MARK_AS_READ_ON_OPEN, false)
        set(value) = settings.putBoolean(KEY_MARK_AS_READ_ON_OPEN, value)

    public var autoFillDescription: Boolean
        get() = settings.getBoolean(KEY_AUTO_FILL_DESCRIPTION, false)
        set(value) = settings.putBoolean(KEY_AUTO_FILL_DESCRIPTION, value)

    public var showDescriptionInLists: Boolean
        get() = settings.getBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, true)
        set(value) = settings.putBoolean(KEY_SHOW_DESCRIPTION_IN_LISTS, value)

    /**
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    public var defaultPrivate: Boolean?
        get() = settings.getBoolean(KEY_DEFAULT_PRIVATE, false).takeIf { it }
        set(value) = settings.putBoolean(KEY_DEFAULT_PRIVATE, value ?: false)

    /**
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    public var defaultReadLater: Boolean?
        get() = settings.getBoolean(KEY_DEFAULT_READ_LATER, false).takeIf { it }
        set(value) = settings.putBoolean(KEY_DEFAULT_READ_LATER, value ?: false)

    public var editAfterSharing: String
        get() = settings.getString(KEY_NEW_EDIT_AFTER_SHARING, "")
        set(value) = settings.putString(KEY_NEW_EDIT_AFTER_SHARING, value)

    public var defaultTags: List<String>
        get() = settings.getString(KEY_DEFAULT_TAGS, "")
            .takeIf { it.isNotBlank() }?.split(",").orEmpty()
        set(value) = settings.putString(KEY_DEFAULT_TAGS, value.joinToString(separator = ","))

    internal companion object {

        internal const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        internal const val KEY_LAST_UPDATE = "LAST_UPDATE"
        internal const val KEY_PERIODIC_SYNC = "PERIODIC_SYNC"
        internal const val KEY_APPEARANCE = "APPEARANCE"
        internal const val KEY_APPLY_DYNAMIC_COLORS = "APPLY_DYNAMIC_COLORS"
        internal const val KEY_DISABLE_SCREENSHOTS = "DISABLE_SCREENSHOTS"
        internal const val KEY_PREFERRED_DATE_FORMAT = "PREFERRED_DATE_FORMAT"
        internal const val KEY_PREFERRED_DETAILS_VIEW = "PREFERRED_DETAILS_VIEW"
        internal const val KEY_ALWAYS_USE_SIDE_PANEL = "ALWAYS_USE_SIDE_PANEL"
        internal const val KEY_MARK_AS_READ_ON_OPEN = "MARK_AS_READ_ON_OPEN"
        internal const val KEY_AUTO_FILL_DESCRIPTION = "AUTO_FILL_DESCRIPTION"
        internal const val KEY_SHOW_DESCRIPTION_IN_LISTS = "SHOW_DESCRIPTION_IN_LISTS"
        internal const val KEY_DEFAULT_PRIVATE = "DEFAULT_PRIVATE"
        internal const val KEY_DEFAULT_READ_LATER = "DEFAULT_READ_LATER"
        internal const val KEY_NEW_EDIT_AFTER_SHARING = "NEW_EDIT_AFTER_SHARING"
        internal const val KEY_DEFAULT_TAGS = "DEFAULT_TAGS"
    }
}
