package com.fibelatti.pinboard.core.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.get
import com.fibelatti.core.extension.getSharedPreferences
import com.fibelatti.core.extension.put
import javax.inject.Inject

// region Constants
@VisibleForTesting
const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
@VisibleForTesting
const val KEY_LAST_UPDATE = "LAST_UPDATE"
@VisibleForTesting
const val KEY_APPEARANCE = "APPEARANCE"
@VisibleForTesting
const val KEY_PREFERRED_DETAILS_VIEW = "PREFERRED_DETAILS_VIEW"
@VisibleForTesting
const val KEY_MARK_AS_READ_ON_OPEN = "MARK_AS_READ_ON_OPEN"
@VisibleForTesting
const val KEY_AUTO_FILL_DESCRIPTION = "AUTO_FILL_DESCRIPTION"
@VisibleForTesting
const val KEY_SHOW_DESCRIPTION_IN_LISTS = "SHOW_DESCRIPTION_IN_LISTS"
@VisibleForTesting
const val KEY_SHOW_DESCRIPTION_IN_DETAILS = "SHOW_DESCRIPTION_IN_DETAILS"
@VisibleForTesting
const val KEY_DEFAULT_PRIVATE = "DEFAULT_PRIVATE"
@VisibleForTesting
const val KEY_DEFAULT_READ_LATER = "DEFAULT_READ_LATER"
@VisibleForTesting
const val KEY_EDIT_AFTER_SHARING = "EDIT_AFTER_SHARING"
// endregion

fun Context.getUserPreferences() = getSharedPreferences("user_preferences")

class UserSharedPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {
    fun getAuthToken(): String = sharedPreferences.get(KEY_AUTH_TOKEN, "")

    fun setAuthToken(authToken: String) {
        sharedPreferences.put(KEY_AUTH_TOKEN, authToken)
    }

    fun getLastUpdate(): String = sharedPreferences.get(KEY_LAST_UPDATE, "")

    fun setLastUpdate(value: String) {
        sharedPreferences.put(KEY_LAST_UPDATE, value)
    }

    fun getAppearance(): String = sharedPreferences.get(KEY_APPEARANCE, "")

    fun setAppearance(value: String) {
        sharedPreferences.put(KEY_APPEARANCE, value)
    }

    fun getPreferredDetailsView(): String = sharedPreferences.get(KEY_PREFERRED_DETAILS_VIEW, "")

    fun setPreferredDetailsView(value: String) {
        sharedPreferences.put(KEY_PREFERRED_DETAILS_VIEW, value)
    }

    fun getMarkAsReadOnOpen(): Boolean = sharedPreferences.get(KEY_MARK_AS_READ_ON_OPEN, false)

    fun setMarkAsReadOnOpen(value: Boolean) {
        sharedPreferences.put(KEY_MARK_AS_READ_ON_OPEN, value)
    }

    fun getAutoFillDescription(): Boolean = sharedPreferences.get(KEY_AUTO_FILL_DESCRIPTION, false)

    fun setAutoFillDescription(value: Boolean) {
        sharedPreferences.put(KEY_AUTO_FILL_DESCRIPTION, value)
    }

    fun getShowDescriptionInLists(): Boolean =
        sharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_LISTS, true)

    fun setShowDescriptionInLists(value: Boolean) {
        sharedPreferences.put(KEY_SHOW_DESCRIPTION_IN_LISTS, value)
    }

    fun getShowDescriptionInDetails(): Boolean =
        sharedPreferences.get(KEY_SHOW_DESCRIPTION_IN_DETAILS, true)

    fun setShowDescriptionInDetails(value: Boolean) {
        sharedPreferences.put(KEY_SHOW_DESCRIPTION_IN_DETAILS, value)
    }

    /***
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    fun getDefaultPrivate(): Boolean? =
        sharedPreferences.get(KEY_DEFAULT_PRIVATE, false).takeIf { it }

    fun setDefaultPrivate(value: Boolean) {
        sharedPreferences.put(KEY_DEFAULT_PRIVATE, value)
    }

    /***
     * Returns the user preferred setting only if true, otherwise return null to respect the preferences
     * set on Pinboard.
     *
     * @return the stored preference if true, null otherwise
     */
    fun getDefaultReadLater(): Boolean? =
        sharedPreferences.get(KEY_DEFAULT_READ_LATER, false).takeIf { it }

    fun setDefaultReadLater(value: Boolean) {
        sharedPreferences.put(KEY_DEFAULT_READ_LATER, value)
    }

    fun getEditAfterSharing(): Boolean = sharedPreferences.get(KEY_EDIT_AFTER_SHARING, false)

    fun setEditAfterSharing(value: Boolean) {
        sharedPreferences.put(KEY_EDIT_AFTER_SHARING, value)
    }
}
