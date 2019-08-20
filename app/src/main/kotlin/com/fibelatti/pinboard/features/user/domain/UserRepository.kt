package com.fibelatti.pinboard.features.user.domain

import androidx.lifecycle.LiveData
import com.fibelatti.pinboard.core.android.Appearance

interface UserRepository {
    fun getLoginState(): LiveData<LoginState>

    suspend fun loginAttempt(authToken: String)

    suspend fun loggedIn()

    suspend fun logout()

    suspend fun forceLogout()

    suspend fun getLastUpdate(): String

    suspend fun setLastUpdate(value: String)

    suspend fun getAppearance(): Appearance

    suspend fun setAppearance(appearance: Appearance)

    suspend fun getAutoFillDescription(): Boolean

    suspend fun setAutoFillDescription(value: Boolean)

    suspend fun getShowDescriptionInLists(): Boolean

    suspend fun setShowDescriptionInLists(value: Boolean)

    suspend fun getShowDescriptionInDetails(): Boolean

    suspend fun setShowDescriptionInDetails(value: Boolean)

    suspend fun getDefaultPrivate(): Boolean?

    suspend fun setDefaultPrivate(value: Boolean)

    suspend fun getDefaultReadLater(): Boolean?

    suspend fun setDefaultReadLater(value: Boolean)

    suspend fun getEditAfterSharing(): Boolean

    suspend fun setEditAfterSharing(value: Boolean)
}
