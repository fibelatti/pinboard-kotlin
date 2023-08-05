package com.fibelatti.pinboard.core.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.get
import com.fibelatti.core.extension.getSharedPreferences
import com.fibelatti.core.extension.put
import javax.inject.Inject
import javax.inject.Singleton

// region Constants
@VisibleForTesting
const val KEY_AUTH_TOKEN = "AUTH_TOKEN"

@VisibleForTesting
const val KEY_LAST_UPDATE = "LAST_UPDATE"

@VisibleForTesting
const val KEY_AUTO_UPDATE = "AUTO_UPDATE"

@VisibleForTesting
const val KEY_PERIODIC_SYNC = "PERIODIC_SYNC"

@VisibleForTesting
const val KEY_APPEARANCE = "APPEARANCE"

@VisibleForTesting
const val KEY_APPLY_DYNAMIC_COLORS = "APPLY_DYNAMIC_COLORS"

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

fun Context.getUserPreferences() = getSharedPreferences("user_preferences")

@Singleton
class UserSharedPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    private var currentAuthToken: String = ""
    private var currentLastUpdate: String = ""

    var authToken: String
        get() = sharedPreferences.get(KEY_AUTH_TOKEN, currentAuthToken)
        set(value) = sharedPreferences.put(KEY_AUTH_TOKEN, value).also { currentAuthToken = value }

    var lastUpdate: String
        get() = sharedPreferences.get(KEY_LAST_UPDATE, currentLastUpdate)
        set(value) = sharedPreferences.put(KEY_LAST_UPDATE, value).also { currentLastUpdate = value }

    var autoUpdate: Boolean
        get() = sharedPreferences.get(KEY_AUTO_UPDATE, true)
        set(value) = sharedPreferences.put(KEY_AUTO_UPDATE, value)

    var periodicSync: Long
        get() = sharedPreferences.get(KEY_PERIODIC_SYNC, 24)
        set(value) = sharedPreferences.put(KEY_PERIODIC_SYNC, value)

    var appearance: String
        get() = sharedPreferences.get(KEY_APPEARANCE, "")
        set(value) = sharedPreferences.put(KEY_APPEARANCE, value)

    var applyDynamicColors: Boolean
        get() = sharedPreferences.get(KEY_APPLY_DYNAMIC_COLORS, false)
        set(value) = sharedPreferences.put(KEY_APPLY_DYNAMIC_COLORS, value)

    var preferredDateFormat: String
        get() = sharedPreferences.get(KEY_PREFERRED_DATE_FORMAT, "")
        set(value) = sharedPreferences.put(KEY_PREFERRED_DATE_FORMAT, value)

    var preferredDetailsView: String
        get() = sharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "")
        set(value) = sharedPreferences.put(KEY_PREFERRED_DETAILS_VIEW, value)

    var alwaysUseSidePanel: Boolean
        get() = sharedPreferences.get(KEY_ALWAYS_USE_SIDE_PANEL, false)
        set(value) = sharedPreferences.put(KEY_ALWAYS_USE_SIDE_PANEL, value)

    var markAsReadOnOpen: Boolean
        get() = sharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false)
        set(value) = sharedPreferences.put(KEY_MARK_AS_READ_ON_OPEN, value)

    var autoFillDescription: Boolean
        get() = sharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false)
        set(value) = sharedPreferences.put(KEY_AUTO_FILL_DESCRIPTION, value)

    var showDescriptionInLists: Boolean
        get() = sharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, true)
        set(value) = sharedPreferences.put(KEY_SHOW_DESCRIPTION_IN_LISTS, value)

    /***
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    var defaultPrivate: Boolean?
        get() = sharedPreferences.get(KEY_DEFAULT_PRIVATE, false).takeIf { it }
        set(value) = sharedPreferences.put(KEY_DEFAULT_PRIVATE, value)

    /***
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    var defaultReadLater: Boolean?
        get() = sharedPreferences.get(KEY_DEFAULT_READ_LATER, false).takeIf { it }
        set(value) = sharedPreferences.put(KEY_DEFAULT_READ_LATER, value)

    var editAfterSharing: String
        get() = sharedPreferences.get(KEY_NEW_EDIT_AFTER_SHARING, "")
        set(value) = sharedPreferences.put(KEY_NEW_EDIT_AFTER_SHARING, value)

    var defaultTags: List<String>
        get() = sharedPreferences.get(KEY_DEFAULT_TAGS, "")
            .takeIf { it.isNotBlank() }?.split(",").orEmpty()
        set(value) = sharedPreferences.put(KEY_DEFAULT_TAGS, value.joinToString(separator = ","))
}
