package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import javax.inject.Inject
import javax.inject.Singleton

@Single
@Singleton
class AppModeProvider @Inject constructor(
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

    @Suppress("KotlinConstantConditions")
    private fun getValue(reviewMode: Boolean): AppMode = when {
        BuildConfig.FLAVOR == "noapi" || reviewMode -> AppMode.NO_API
        userSharedPreferences.useLinkding -> AppMode.LINKDING
        else -> AppMode.PINBOARD
    }
}
