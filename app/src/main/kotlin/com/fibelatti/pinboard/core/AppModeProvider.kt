package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppModeProvider @Inject constructor() {

    private val _appMode: MutableStateFlow<AppMode> = MutableStateFlow(getValue(reviewMode = false))
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    fun setReviewMode(value: Boolean) {
        _appMode.value = getValue(reviewMode = value)
    }

    @Suppress("KotlinConstantConditions")
    private fun getValue(reviewMode: Boolean): AppMode = when {
        BuildConfig.FLAVOR == "noapi" || reviewMode -> AppMode.NO_API
        else -> AppMode.PINBOARD
    }
}
