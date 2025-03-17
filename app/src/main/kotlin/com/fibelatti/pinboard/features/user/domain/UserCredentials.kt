package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.AppMode

data class UserCredentials(
    val pinboardAuthToken: String?,
    val linkdingInstanceUrl: String?,
    val linkdingAuthToken: String?,
    val appReviewMode: Boolean = false,
) {

    fun getConnectedServices(): Set<AppMode> = buildSet {
        if (appReviewMode) add(AppMode.NO_API)
        if (pinboardAuthToken != null) add(AppMode.PINBOARD)
        if (linkdingAuthToken != null) add(AppMode.LINKDING)
    }

    fun hasAuthToken(): Boolean = getConnectedServices().isNotEmpty()

    fun getPinboardUsername(): String? = pinboardAuthToken?.substringBefore(":")
}
