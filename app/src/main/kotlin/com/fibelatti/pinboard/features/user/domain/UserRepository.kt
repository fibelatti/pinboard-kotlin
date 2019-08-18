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

    suspend fun getDefaultPrivate(): Boolean?

    suspend fun setDefaultPrivate(value: Boolean)

    suspend fun getDefaultReadLater(): Boolean?

    suspend fun setDefaultReadLater(value: Boolean)
}
