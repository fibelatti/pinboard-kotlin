package com.fibelatti.pinboard.features.user.domain

sealed class LoginState {
    object Authorizing : LoginState()
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    object Unauthorized : LoginState()
}
