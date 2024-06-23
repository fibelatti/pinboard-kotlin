package com.fibelatti.bookmarking.features.posts.domain

public sealed class PreferredDetailsView(public val value: String) {
    public data class InAppBrowser(val markAsReadOnOpen: Boolean) : PreferredDetailsView("IN_APP")
    public data class ExternalBrowser(val markAsReadOnOpen: Boolean) : PreferredDetailsView("EXTERNAL")
    public data object Edit : PreferredDetailsView("EDIT")
}
