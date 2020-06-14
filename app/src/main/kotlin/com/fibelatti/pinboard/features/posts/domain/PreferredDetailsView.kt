package com.fibelatti.pinboard.features.posts.domain

sealed class PreferredDetailsView(val value: String) {
    data class InAppBrowser(val markAsReadOnOpen: Boolean) : PreferredDetailsView("IN_APP")
    data class ExternalBrowser(val markAsReadOnOpen: Boolean) : PreferredDetailsView("EXTERNAL")
    object Edit : PreferredDetailsView("EDIT")
}
