package com.fibelatti.pinboard.core.persistence

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.fibelatti.core.android.extension.get
import com.fibelatti.core.android.extension.put
import javax.inject.Inject
import javax.inject.Singleton

// region Constants
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
const val KEY_DATE_INCLUDES_TIME = "DATE_INCLUDES_TIME"

@VisibleForTesting
const val KEY_HIDDEN_POST_QUICK_OPTIONS = "HIDDEN_POST_QUICK_OPTIONS"

@VisibleForTesting
const val KEY_PREFERRED_DETAILS_VIEW = "PREFERRED_DETAILS_VIEW"

@VisibleForTesting
const val KEY_USE_SPLIT_NAV = "USE_SPLIT_NAV"

@VisibleForTesting
const val KEY_MARK_AS_READ_ON_OPEN = "MARK_AS_READ_ON_OPEN"

@VisibleForTesting
const val KEY_FOLLOW_REDIRECTS = "FOLLOW_REDIRECTS"

@VisibleForTesting
const val KEY_AUTO_FILL_DESCRIPTION = "AUTO_FILL_DESCRIPTION"

@VisibleForTesting
const val KEY_USE_BLOCKQUOTE = "USE_BLOCKQUOTE"

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

@VisibleForTesting
const val KEY_REMOVE_UTM_PARAMETERS = "REMOVE_UTM_PARAMETERS"

@VisibleForTesting
const val KEY_REMOVED_URL_PARAMETERS = "REMOVED_URL_PARAMETERS"
// endregion

@Singleton
class UserSharedPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    private var currentLinkdingInstanceUrl: String? = null
    private var currentLinkdingAuthToken: String? = null
    private var currentPinboardAuthToken: String? = null
    private var currentLastUpdate: String = ""

    var appReviewMode: Boolean
        get() = sharedPreferences.getBoolean("APP_REVIEW_MODE", false)
        set(value) = sharedPreferences.put("APP_REVIEW_MODE", value)

    var linkdingInstanceUrl: String?
        get() = sharedPreferences.getString("LINKDING_INSTANCE_URL", currentLinkdingInstanceUrl)
        set(value) = sharedPreferences.put("LINKDING_INSTANCE_URL", value).also { currentLinkdingInstanceUrl = value }

    var linkdingAuthToken: String?
        get() {
            val fallback = currentLinkdingAuthToken?.ifBlank { null }
                ?: sharedPreferences.getString("AUTH_TOKEN", null)
                    .takeIf { sharedPreferences.get("USE_LINKDING", false) }

            return sharedPreferences.getString("LINKDING_AUTH_TOKEN", fallback)
        }
        set(value) {
            currentLinkdingAuthToken = value
            sharedPreferences.edit {
                putString("LINKDING_AUTH_TOKEN", value)

                if (value.isNullOrBlank()) {
                    remove("AUTH_TOKEN")
                }
            }
        }

    var pinboardAuthToken: String?
        get() {
            val fallback = currentPinboardAuthToken?.ifBlank { null }
                ?: sharedPreferences.getString("AUTH_TOKEN", null)
                    .takeUnless { sharedPreferences.getBoolean("USE_LINKDING", false) }

            return sharedPreferences.getString("PINBOARD_AUTH_TOKEN", fallback)
        }
        set(value) {
            currentPinboardAuthToken = value
            sharedPreferences.edit {
                putString("PINBOARD_AUTH_TOKEN", value)

                if (value.isNullOrBlank()) {
                    remove("AUTH_TOKEN")
                }
            }
        }

    var lastUpdate: String
        get() = sharedPreferences.get(KEY_LAST_UPDATE, currentLastUpdate)
        set(value) = sharedPreferences.put(KEY_LAST_UPDATE, value).also { currentLastUpdate = value }

    var periodicSync: Long
        get() = sharedPreferences.get(KEY_PERIODIC_SYNC, 24)
        set(value) = sharedPreferences.put(KEY_PERIODIC_SYNC, value)

    var appearance: String
        get() = sharedPreferences.get(KEY_APPEARANCE, "")
        set(value) = sharedPreferences.put(KEY_APPEARANCE, value)

    var applyDynamicColors: Boolean
        get() = sharedPreferences.get(KEY_APPLY_DYNAMIC_COLORS, false)
        set(value) = sharedPreferences.put(KEY_APPLY_DYNAMIC_COLORS, value)

    var disableScreenshots: Boolean
        get() = sharedPreferences.get(KEY_DISABLE_SCREENSHOTS, false)
        set(value) = sharedPreferences.put(KEY_DISABLE_SCREENSHOTS, value)

    var preferredDateFormat: String
        get() = sharedPreferences.get(KEY_PREFERRED_DATE_FORMAT, "")
        set(value) = sharedPreferences.put(KEY_PREFERRED_DATE_FORMAT, value)

    var dateIncludesTime: Boolean
        get() = sharedPreferences.get(KEY_DATE_INCLUDES_TIME, true)
        set(value) = sharedPreferences.put(KEY_DATE_INCLUDES_TIME, value)

    var preferredSortType: String
        get() = sharedPreferences.get("PREFERRED_SORT_TYPE", "")
        set(value) = sharedPreferences.put("PREFERRED_SORT_TYPE", value)

    var hiddenPostQuickOptions: Set<String>
        get() = sharedPreferences.getStringSet(KEY_HIDDEN_POST_QUICK_OPTIONS, null) ?: emptySet()
        set(value) = sharedPreferences.edit { putStringSet(KEY_HIDDEN_POST_QUICK_OPTIONS, value) }

    var preferredDetailsView: String
        get() = sharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "")
        set(value) = sharedPreferences.put(KEY_PREFERRED_DETAILS_VIEW, value)

    var useSplitNav: Boolean
        get() = sharedPreferences.get(KEY_USE_SPLIT_NAV, defaultValue = true)
        set(value) = sharedPreferences.put(KEY_USE_SPLIT_NAV, value)

    var markAsReadOnOpen: Boolean
        get() = sharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false)
        set(value) = sharedPreferences.put(KEY_MARK_AS_READ_ON_OPEN, value)

    var followRedirects: Boolean
        get() = sharedPreferences.get(KEY_FOLLOW_REDIRECTS, true)
        set(value) = sharedPreferences.put(KEY_FOLLOW_REDIRECTS, value)

    var autoFillDescription: Boolean
        get() = sharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false)
        set(value) = sharedPreferences.put(KEY_AUTO_FILL_DESCRIPTION, value)

    var useBlockquote: Boolean
        get() = sharedPreferences.get(KEY_USE_BLOCKQUOTE, false)
        set(value) = sharedPreferences.put(KEY_USE_BLOCKQUOTE, value)

    var showDescriptionInLists: Boolean
        get() = sharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, true)
        set(value) = sharedPreferences.put(KEY_SHOW_DESCRIPTION_IN_LISTS, value)

    /**
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    var defaultPrivate: Boolean?
        get() = sharedPreferences.get(KEY_DEFAULT_PRIVATE, false).takeIf { it }
        set(value) = sharedPreferences.put(KEY_DEFAULT_PRIVATE, value)

    /**
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

    var removeUtmParameters: Boolean
        get() = sharedPreferences.get(KEY_REMOVE_UTM_PARAMETERS, false)
        set(value) = sharedPreferences.put(KEY_REMOVE_UTM_PARAMETERS, value)

    var removedUrlParameters: Set<String>
        get() = sharedPreferences.getStringSet(KEY_REMOVED_URL_PARAMETERS, null) ?: emptySet()
        set(value) = sharedPreferences.edit { putStringSet(KEY_REMOVED_URL_PARAMETERS, value) }
}
