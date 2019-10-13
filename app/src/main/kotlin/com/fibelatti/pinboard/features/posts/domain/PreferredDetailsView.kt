package com.fibelatti.pinboard.features.posts.domain

sealed class PreferredDetailsView(val value: String) {
    object InAppBrowser : PreferredDetailsView("IN_APP")
    object ExternalBrowser : PreferredDetailsView("EXTERNAL")
    object Edit : PreferredDetailsView("EDIT")
}
