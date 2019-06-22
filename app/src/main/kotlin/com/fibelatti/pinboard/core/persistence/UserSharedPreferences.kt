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
}
