package com.fibelatti.pinboard.core

import com.fibelatti.bookmarking.core.persistence.UserSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AppModeProvider(
    private val userSharedPreferences: UserSharedPreferences,
) {

    private val _appMode: MutableStateFlow<AppMode> = MutableStateFlow(getValue(reviewMode = false))
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    fun setReviewMode(value: Boolean) {
        _appMode.value = getValue(reviewMode = value)
    }

    fun refresh() {
        _appMode.update { getValue(reviewMode = false) }
    }

    private fun getValue(reviewMode: Boolean): AppMode = when {
        userSharedPreferences.noApiMode || reviewMode -> AppMode.NO_API
        userSharedPreferences.useLinkding -> AppMode.LINKDING
        else -> AppMode.PINBOARD
    }
}
