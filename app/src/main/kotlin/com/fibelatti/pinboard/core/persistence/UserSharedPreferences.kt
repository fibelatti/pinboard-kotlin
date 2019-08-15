package com.fibelatti.pinboard.core.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.get
import com.fibelatti.core.extension.getSharedPreferences
import com.fibelatti.core.extension.put
import javax.inject.Inject

@VisibleForTesting
const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
@VisibleForTesting
const val KEY_LAST_UPDATE = "LAST_UPDATE"
@VisibleForTesting
const val KEY_APPEARANCE = "APPEARANCE"
@VisibleForTesting
const val KEY_DEFAULT_PRIVATE = "DEFAULT_PRIVATE"
@VisibleForTesting
const val KEY_DEFAULT_READ_LATER = "DEFAULT_READ_LATER"

fun Context.getUserPreferences() = getSharedPreferences("user_preferences")

class UserSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
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
}
