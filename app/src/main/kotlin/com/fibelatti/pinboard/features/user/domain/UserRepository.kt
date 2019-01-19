package com.fibelatti.pinboard.features.user.domain

import androidx.lifecycle.LiveData

interface UserRepository {
    fun getLoginState(): LiveData<LoginState>

    fun login(authToken: String)

    fun loggedIn()

    fun logout()

    fun forceLogout()

    fun getLastUpdate(): LiveData<String>

    fun setLastUpdate(value: String)
}
