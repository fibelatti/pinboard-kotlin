package com.fibelatti.pinboard.features.user.domain

import androidx.lifecycle.LiveData

interface UserRepository {
    fun getLoginState(): LiveData<LoginState>

    fun loginAttempt(authToken: String)

    fun loggedIn()

    suspend fun logout()

    suspend fun forceLogout()

    fun getLastUpdate(): String

    fun setLastUpdate(value: String)
}
