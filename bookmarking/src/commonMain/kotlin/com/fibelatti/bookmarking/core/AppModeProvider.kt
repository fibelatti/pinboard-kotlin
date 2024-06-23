package com.fibelatti.bookmarking.core

import com.fibelatti.bookmarking.core.persistence.UserSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
public class AppModeProvider(
    private val userSharedPreferences: UserSharedPreferences,
) {

    private val _appMode: MutableStateFlow<AppMode> = MutableStateFlow(getValue(reviewMode = false))
    public val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    public fun setReviewMode(value: Boolean) {
        _appMode.value = getValue(reviewMode = value)
    }

    public fun refresh() {
        _appMode.update { getValue(reviewMode = false) }
    }

    private fun getValue(reviewMode: Boolean): AppMode = when {
        userSharedPreferences.noApiMode || reviewMode -> AppMode.NO_API
        userSharedPreferences.useLinkding -> AppMode.LINKDING
        else -> AppMode.PINBOARD
    }
}
